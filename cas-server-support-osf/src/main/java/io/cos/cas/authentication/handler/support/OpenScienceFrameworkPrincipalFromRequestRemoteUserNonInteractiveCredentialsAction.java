/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.cos.cas.authentication.handler.support;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.cos.cas.authentication.OpenScienceFrameworkCredential;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.web.flow.AbstractNonInteractiveCredentialsAction;
import org.jasig.cas.web.support.WebUtils;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.RequestContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;

/**
 * Implementation of the NonInteractiveCredentialsAction that looks for a remote
 * user that is set in the <code>HttpServletRequest</code> and attempts to
 * construct a Principal (and thus a PrincipalBearingCredential). If it doesn't
 * find one, this class returns and error event which tells the web flow it
 * could not find any credentials.
 *
 * @author Michael Haselton
 * @since 4.1.1
 */
public final class OpenScienceFrameworkPrincipalFromRequestRemoteUserNonInteractiveCredentialsAction
            extends AbstractNonInteractiveCredentialsAction {

    private static final String REMOTE_USER = "REMOTE_USER";

    private static final String ATTRIBUTE_PREFIX = "AUTH-";

    private static final String SHIBBOLETH_COOKIE_PREFIX = "_shibsession_";

    private static final int SIXTY_SECONDS = 60 * 1000;

    @NotNull
    private String institutionsAuthUrl;

    @NotNull
    private String institutionsAuthJweSecret;

    @NotNull
    private String institutionsAuthJwtSecret;

    @NotNull
    private String institutionsAuthXslLocation;

    private Transformer institutionsAuthTransformer;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void afterPropertiesSet() throws Exception {
        final File xslFile = ResourceUtils.getFile(this.institutionsAuthXslLocation);
        final StreamSource xslStreamSource = new StreamSource(xslFile);
        final TransformerFactory tFactory = TransformerFactory.newInstance();
        this.institutionsAuthTransformer = tFactory.newTransformer(xslStreamSource);

        super.afterPropertiesSet();
    }

    @Override
    protected Credential constructCredentialsFromRequest(
            final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final OpenScienceFrameworkCredential credential = (OpenScienceFrameworkCredential) context.getFlowScope().get("credential");

        // Clear the shibboleth session cookie, allows the user to logout of our system and login as a different user.
        // Assumes we would redirect the user to the proper (custom) Shibboleth logout endpoint from OSF.
        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
        for (final Cookie cookie : request.getCookies()) {
            if (cookie.getName().startsWith(SHIBBOLETH_COOKIE_PREFIX)) {
                final Cookie shibbolethCookie = new Cookie(cookie.getName(), null);
                shibbolethCookie.setMaxAge(0);
                response.addCookie(shibbolethCookie);
            }
        }

        // WARNING: Do not assume this works w/o acceptance testing in a Production environment.
        // The call is made to trust these headers only because we assume the Apache Shibboleth Service Provider module
        // rejects any conflicting / forged headers.
        final String remoteUser = request.getHeader(REMOTE_USER);
        if (StringUtils.hasText(remoteUser)) {
            logger.debug("Remote  User [{}] found in HttpServletRequest", remoteUser);
            credential.setRemotePrincipal(Boolean.TRUE);
            credential.setUsername(remoteUser);

            for (final String headerName : Collections.list(request.getHeaderNames())) {
                if (headerName.startsWith(ATTRIBUTE_PREFIX)) {
                    credential.getAuthenticationHeaders().put(
                            headerName.substring(ATTRIBUTE_PREFIX.length()),
                            request.getHeader(headerName)
                    );
                }
            }

            // Notify the OSF of the remote principal authentication.
            remotePrincipalAuthenticated(credential);

            return credential;
        }

        logger.debug("Remote User not found in HttpServletRequest.");
        return null;
    }

    /**
     * Securely notify the OSF of a Remote Principal Authentication credential. Allows the OSF the opportunity
     * to create a verified user account and/or assign institutional affiliation to the user's account.
     *
     * @param credential the credential object bearing the username, fullname, etc...
     */
    private void remotePrincipalAuthenticated(final OpenScienceFrameworkCredential credential) {
        try {
            final String payload = this.normalizeRemotePrincipal(credential);

            // Build a JWT and wrap it with JWE for secure transport to the OSF API.
            final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(credential.getUsername())
                    .claim("data", payload)
                    .expirationTime(new Date(new Date().getTime() + SIXTY_SECONDS))
                    .build();

            final SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

            final JWSSigner signer = new MACSigner(this.institutionsAuthJwtSecret.getBytes());
            signedJWT.sign(signer);

            final JWEObject jweObject = new JWEObject(
                    new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
                            .contentType("JWT")
                            .build(),
                    new Payload(signedJWT));
            jweObject.encrypt(new DirectEncrypter(this.institutionsAuthJweSecret.getBytes()));
            final String jweString = jweObject.serialize();

            // A call is made to the OSF CAS Institution Login Endpoint to create a registered user (if
            // one does not already exist) and apply institutional affiliation.
            final HttpResponse httpResponse = Request.Post(this.institutionsAuthUrl)
                    .addHeader(new BasicHeader("Content-Type", "text/plain"))
                    .bodyString(jweString, ContentType.APPLICATION_JSON)
                    .execute()
                    .returnResponse();
            logger.info(
                    "Remote Principal Authenticate (OSF API) Response: <{}> Status Code {}",
                    credential.getUsername(),
                    httpResponse.getStatusLine().getStatusCode()
            );
        } catch (final JOSEException | IOException | ParserConfigurationException | TransformerException e) {
            // log the error and return the user to the login flow
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Normalize the Remote Principal credential.
     *
     * @param credential the credential object bearing the username, password, etc...
     * @return the json serialized authorization for the OSF API
     * @throws ParserConfigurationException a parser configuration exception
     * @throws TransformerException a transformer exception
     */
    private String normalizeRemotePrincipal(final OpenScienceFrameworkCredential credential)
            throws ParserConfigurationException, TransformerException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final Document document = builder.newDocument();
        final Element rootElement = document.createElement("auth");
        document.appendChild(rootElement);

        for (final String key : credential.getAuthenticationHeaders().keySet()) {
            final org.w3c.dom.Element attribute = document.createElement("attribute");
            attribute.setAttribute("name", key);
            attribute.setAttribute("value", credential.getAuthenticationHeaders().get(key));
            rootElement.appendChild(attribute);
        }

        // run the auth document through the transformer
        final DOMSource source = new DOMSource(document);
        final StringWriter writer = new StringWriter();
        final StreamResult result = new StreamResult(writer);
        this.institutionsAuthTransformer.transform(source, result);

        // convert transformed xml to json
        final JSONObject xmlJSONObj = XML.toJSONObject(writer.getBuffer().toString());
        return xmlJSONObj.toString();
    }

    public void setInstitutionsAuthUrl(final String institutionsAuthUrl) {
        this.institutionsAuthUrl = institutionsAuthUrl;
    }

    public void setInstitutionsAuthJweSecret(final String institutionsAuthJweSecret) {
        this.institutionsAuthJweSecret = institutionsAuthJweSecret;
    }

    public void setInstitutionsAuthJwtSecret(final String institutionsAuthJwtSecret) {
        this.institutionsAuthJwtSecret = institutionsAuthJwtSecret;
    }

    public void setInstitutionsAuthXslLocation(final String institutionsAuthXslLocation) {
        this.institutionsAuthXslLocation = institutionsAuthXslLocation;
    }
}
