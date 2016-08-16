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
import io.cos.cas.authentication.RemoteUserFailedLoginException;
import io.cos.cas.web.flow.OpenScienceFrameworkLoginThroughOrcid;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicHeader;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.support.WebUtils;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.security.auth.login.AccountException;
import javax.servlet.http.HttpServletRequest;
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
import java.util.LinkedHashMap;
import java.util.Map;


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
            extends AbstractAction {
    /**
     * Principal Authentication Result.
     */
    public static class PrincipalAuthenticationResult {
        private String username;
        private String institutionId;

        /**
         * Creates a new instance with the given parameters.
         * @param username the username
         * @param institutionId the institution id
         */
        public PrincipalAuthenticationResult(final String username, final String institutionId) {
            this.username = username;
            this.institutionId = institutionId;
        }

        /**
         * @return the username.
         */
        public String getUsername() {
            return username;
        }

        /**
         * @return the institutionId.
         */
        public String getInstitutionId() {
            return institutionId;
        }
    }

    /** Authentication failure result. */
    public static final String AUTHENTICATION_FAILURE = "authenticationFailure";

    private static final String REMOTE_USER = "REMOTE_USER";

    private static final String ATTRIBUTE_PREFIX = "AUTH-";

    private static final String SHIBBOLETH_SESSION_HEADER = ATTRIBUTE_PREFIX + "Shib-Session-ID";

    private static final int SIXTY_SECONDS = 60 * 1000;

    /** The logger instance. */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The Principal factory.
     */
    protected PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    @NotNull
    private String institutionsAuthUrl;

    @NotNull
    private String institutionsAuthJweSecret;

    @NotNull
    private String institutionsAuthJwtSecret;

    @NotNull
    private String institutionsAuthXslLocation;

    private Transformer institutionsAuthTransformer;

    /** Instance of CentralAuthenticationService. */
    @NotNull
    private CentralAuthenticationService centralAuthenticationService;

    /**
     * Checks if is renew present.
     *
     * @param context the context
     * @return true, if  renew present
     */
    protected boolean isRenewPresent(final RequestContext context) {
        return StringUtils.hasText(context.getRequestParameters().get("renew"));
    }

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {
        final Credential credential;
        try {
            credential = constructCredentialsFromRequest(context);
        } catch (final AccountException e) {
            final Map<String, Class<? extends Exception>> failures = new LinkedHashMap<>();
            failures.put(e.getClass().getSimpleName(), e.getClass());
            return getEventFactorySupport().event(
                    this,
                    AUTHENTICATION_FAILURE,
                    new LocalAttributeMap<Object>("error", new AuthenticationException(failures))
            );
        }

        if (credential == null) {
            final String campaign = context.getRequestParameters().get("campaign");
            final String code = context.getRequestParameters().get("code");
            String service_url = context.getRequestParameters().get("service");

            if ("orcid".equals(campaign)) {
                // Authorization response from ORCID
                if (code != null) {
                    return new Event(this, "orcidExchange");
                // Authorization request to ORCID
                } else {
                    if (service_url == null)
                        service_url = "";
                    context.getFlowScope().put("state", service_url);
                    return new Event(this, "orcidAuthorize");
                }
            }

            return error();
        }

        final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        final Service service = WebUtils.getService(context);

        if (isRenewPresent(context)
                && ticketGrantingTicketId != null
                && service != null) {

            try {
                final ServiceTicket serviceTicketId = this.centralAuthenticationService
                        .grantServiceTicket(ticketGrantingTicketId,
                                service,
                                credential);
                WebUtils.putServiceTicketInRequestScope(context, serviceTicketId);
                return result("warn");
            } catch (final AuthenticationException e) {
                onError(context, credential);
                return error();
            } catch (final TicketException e) {
                this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicketId);
                logger.debug("Attempted to generate a ServiceTicket using renew=true with different credential", e);
            }
        }

        try {
            WebUtils.putTicketGrantingTicketInScopes(
                    context,
                    this.centralAuthenticationService
                            .createTicketGrantingTicket(credential));
            onSuccess(context, credential);
            return success();
        } catch (final AuthenticationException e) {
            return getEventFactorySupport().event(
                    this,
                    AUTHENTICATION_FAILURE,
                    new LocalAttributeMap<Object>("error", e)
            );
        } catch (final Exception e) {
            onError(context, credential);
            return error();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final File xslFile = ResourceUtils.getFile(this.institutionsAuthXslLocation);
        final StreamSource xslStreamSource = new StreamSource(xslFile);
        final TransformerFactory tFactory = TransformerFactory.newInstance();
        this.institutionsAuthTransformer = tFactory.newTransformer(xslStreamSource);

        super.afterPropertiesSet();
    }

    /**
     * Abstract method to implement to construct the credential from the
     * request object.
     *
     * @param context the context for this request.
     * @return the constructed credential or null if none could be constructed
     * from the request.
     * @throws AccountException a account exception
     */
    protected Credential constructCredentialsFromRequest(final RequestContext context) throws AccountException {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final OpenScienceFrameworkCredential credential = (OpenScienceFrameworkCredential) context.getFlowScope().get("credential");

        // WARNING: Do not assume this works w/o acceptance testing in a Production environment.
        // The call is made to trust these headers only because we assume the Apache Shibboleth Service Provider module
        // rejects any conflicting / forged headers.
        final String shibbolethSession = request.getHeader(SHIBBOLETH_SESSION_HEADER);
        if (StringUtils.hasText(shibbolethSession)) {
            final String remoteUser = request.getHeader(REMOTE_USER);
            if (StringUtils.isEmpty(remoteUser)) {
                logger.error("Invalid Remote User specified as Empty");
                throw new RemoteUserFailedLoginException("Invalid Remote User specified as Empty");
            }

            logger.info("Remote User from HttpServletRequest '{}'", remoteUser);
            credential.setRemotePrincipal(Boolean.TRUE);

            for (final String headerName : Collections.list(request.getHeaderNames())) {
                if (headerName.startsWith(ATTRIBUTE_PREFIX)) {
                    final String headerValue = request.getHeader(headerName);
                    logger.debug("Remote User [{}] Auth Header '{}': '{}'", remoteUser, headerName, headerValue);

                    credential.getAuthenticationHeaders().put(
                            headerName.substring(ATTRIBUTE_PREFIX.length()),
                            headerValue
                    );
                }
            }

            // Notify the OSF of the remote principal authentication.
            final PrincipalAuthenticationResult remoteUserInfo = notifyRemotePrincipalAuthenticated(credential);
            credential.setUsername(remoteUserInfo.getUsername());
            credential.setInstitutionId(remoteUserInfo.getInstitutionId());

            return credential;
        } else if (request.getParameter("username") != null && request.getParameter("verification_key") != null) {
            // Construct credential if presented with (username, verification_key)
            // This is used when:
            //      1. User creates an account
            //      2. User resets the password through forgot_password
            //      3. User sets password when added as an unregistered contribution
            // Note: Two-factor sign in works and remain unchanged
            credential.setUsername(request.getParameter("username"));
            credential.setVerificationKey(request.getParameter("verification_key"));
            return credential;
        }

        return null;
    }

    /**
     * Securely notify the OSF of a Remote Principal Authentication credential. Allows the OSF the opportunity
     * to create a verified user account and/or assign institutional affiliation to the user's account.
     *
     * @param credential the credential object bearing the authentication headers from the idp
     * @return the username from the idp and setup on the OSF
     * @throws AccountException a account exception
     */
    private PrincipalAuthenticationResult notifyRemotePrincipalAuthenticated(final OpenScienceFrameworkCredential credential)
            throws AccountException {
        try {
            final JSONObject normalized = this.normalizeRemotePrincipal(credential);
            final JSONObject provider = normalized.getJSONObject("provider");
            final String institutionId = provider.getString("id");
            final String username = provider.getJSONObject("user").getString("username");
            final String payload = normalized.toString();

            logger.info("Notify Remote Principal Authenticated: username={}, institution={}", username, institutionId);
            logger.debug("Notify Remote Principal Authenticated [{}, {}] Normalized Payload '{}'", username, institutionId, payload);

            // Build a JWT and wrap it with JWE for secure transport to the OSF API.
            final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(username)
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
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            logger.info(
                    "Notify Remote Principal Authenticated [OSF API] Response: <{}> Status Code {}",
                    username,
                    statusCode
            );
            // The institutional authentication endpoint should always respond with a 204 No Content when successful.
            if (statusCode != HttpStatus.SC_NO_CONTENT) {
                final String responseString = new BasicResponseHandler().handleResponse(httpResponse);
                logger.error("Notify Remote Principal Authenticated [OSF API] Response Body: '{}'", responseString);
                throw new RemoteUserFailedLoginException("Invalid Status Code from OSF API Endpoint");
            }

            // return the username for the credential build.
            return new PrincipalAuthenticationResult(username, institutionId);
        } catch (final JOSEException | IOException | ParserConfigurationException | TransformerException e) {
            logger.error("Notify Remote Principal Authenticated Exception: {}", e.getMessage());
            logger.trace("Notify Remote Principal Authenticated Exception: {}", e);
            throw new RemoteUserFailedLoginException("Unable to Build Message for OSF API Endpoint");
        }
    }

    /**
     * Normalize the Remote Principal credential.
     *
     * @param credential the credential object bearing the username, password, etc...
     * @return the json object to serialize for authorization with the OSF API
     * @throws ParserConfigurationException a parser configuration exception
     * @throws TransformerException a transformer exception
     */
    private JSONObject normalizeRemotePrincipal(final OpenScienceFrameworkCredential credential)
            throws ParserConfigurationException, TransformerException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final Document document = builder.newDocument();
        final Element rootElement = document.createElement("auth");
        document.appendChild(rootElement);

        for (final String key : credential.getAuthenticationHeaders().keySet()) {
            final Element attribute = document.createElement("attribute");
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
        return XML.toJSONObject(writer.getBuffer().toString());
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

    /**
     * AbstractNonInteractiveCredentialsAction.
     * @return the central authentication service
     */
    public CentralAuthenticationService getCentralAuthenticationService() {
        return centralAuthenticationService;
    }

    public void setCentralAuthenticationService(
            final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    /**
     * Sets principal factory to create principal objects.
     *
     * @param principalFactory the principal factory
     */
    public void setPrincipalFactory(final PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }

    /**
     * Hook method to allow for additional processing of the response before
     * returning an error event.
     *
     * @param context the context for this specific request.
     * @param credential the credential for this request.
     */
    protected void onError(final RequestContext context,
                           final Credential credential) {
        // default implementation does nothing
    }

    /**
     * Hook method to allow for additional processing of the response before
     * returning a success event.
     *
     * @param context the context for this specific request.
     * @param credential the credential for this request.
     */
    protected void onSuccess(final RequestContext context,
                             final Credential credential) {
        // default implementation does nothing
    }
}
