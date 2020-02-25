/*
 * Copyright (c) 2015. Center for Open Science
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cos.cas.authentication.handler.support;

import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.cos.cas.adaptors.postgres.types.DelegationProtocol;
import io.cos.cas.authentication.exceptions.RemoteUserFailedLoginException;
import io.cos.cas.authentication.OpenScienceFrameworkCredential;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicHeader;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.web.support.WebUtils;

import org.json.JSONObject;
import org.json.XML;

import org.pac4j.oauth.client.OrcidClient;

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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * Implementation of the NonInteractiveCredentialsAction that looks for a remote
 * user that is set in the <code>HttpServletRequest</code> and attempts to
 * construct a Principal (and thus a PrincipalBearingCredential). If it doesn't
 * find one, this class returns and error event which tells the web flow it
 * could not find any credentials.
 *
 * Since 4.1.5, the functionality of this Action has been expanded to
 *  1.  Institution login using SAML with implementation from Shibboleth
 *  2.  Institution login Using CAS with implementation from pac4j
 *  3.  Normal login with username and verification key
 *
 * TODO: rewrite this outdated JavaDoc along with refactoring {@link RemoteUserFailedLoginException}
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 19.3.0
 */
public class OpenScienceFrameworkPrincipalFromRequestRemoteUserNonInteractiveCredentialsAction extends AbstractAction {
    /**
     * The Principal Authentication Result.
     *
     * @author Longze Chen
     * @since 19.3.0
     */
    public static class PrincipalAuthenticationResult {

        private String username;
        private String institutionId;

        /**
         * Creates a new instance with the given parameters.
         *
         * @param username The username
         * @param institutionId The institution id
         */
        public PrincipalAuthenticationResult(final String username, final String institutionId) {
            this.username = username;
            this.institutionId = institutionId;
        }

        public String getUsername() {
            return username;
        }

        public String getInstitutionId() {
            return institutionId;
        }
    }

    private static final String CONST_CREDENTIAL = "credential";

    private static final String AUTHENTICATION_FAILURE = "authenticationFailure";

    private static final String REMOTE_USER = "REMOTE_USER";

    private static final String ATTRIBUTE_PREFIX = "AUTH-";

    private static final String SHIBBOLETH_SESSION_HEADER = ATTRIBUTE_PREFIX + "Shib-Session-ID";

    private static final String SHIBBOLETH_COOKIE_PREFIX = "_shibsession_";

    private static final int SIXTY_SECONDS = 60 * 1000;

    /** The Logger Instance. */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The Principal factory. */
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
     * @return true, if renew present
     */
    protected boolean isRenewPresent(final RequestContext context) {
        return StringUtils.hasText(context.getRequestParameters().get("renew"));
    }

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {
        final OpenScienceFrameworkCredential credential;
        try {
            credential = constructCredential(context);
        } catch (final AccountException e) {
            final Map<String, Class<? extends Exception>> failures = new LinkedHashMap<>();
            failures.put(e.getClass().getSimpleName(), e.getClass());
            return getEventFactorySupport().event(
                    this,
                    AUTHENTICATION_FAILURE,
                    new LocalAttributeMap<Object>(
                            "error",
                            new AuthenticationException(failures)
                    )
            );
        }

        if (credential == null) {
            return error();
        }

        // PAC4J OAuth needs to retain existing credential/tgt for special case login w/ email request on OSF side,
        // send tgt on success is the most appropriate next step.
        if (DelegationProtocol.OAUTH_PAC4J.equals(credential.getDelegationProtocol())) {
            return success();
        }

        final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        final Service service = WebUtils.getService(context);

        if (isRenewPresent(context) && ticketGrantingTicketId != null && service != null) {
            try {
                final ServiceTicket serviceTicketId = this.centralAuthenticationService.grantServiceTicket(
                        ticketGrantingTicketId,
                        service,
                        credential
                );
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
                    this.centralAuthenticationService.createTicketGrantingTicket(credential)
            );
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
     * Construct the credential from the web flow.
     *
     * @param context the context for this request.
     * @return the constructed credential or null if none could be constructed from the request.
     * @throws AccountException an account exception
     */
    protected OpenScienceFrameworkCredential constructCredential(final RequestContext context) throws AccountException {

        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);

        // WARNING: Do not use ``WebUtils.getCredential(RequestContext context)``, it will make the credential ``null``.
        // _TO_DO_: Check both ``FlowScope`` and ``RequestScope``. Write a ``.getCredential(RequestContext context)``
        //          which are compatible with ``OpenScienceFrameworkCredential``.
        final OpenScienceFrameworkCredential credential
                = (OpenScienceFrameworkCredential) context.getFlowScope().get(CONST_CREDENTIAL);

        final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);

        // WARNING: Do not assume this works w/o acceptance testing in a Production environment.
        // The call is made to trust these headers only because we assume the Apache Shibboleth Service Provider module
        // rejects any conflicting / forged headers.
        final String shibbolethSession = request.getHeader(SHIBBOLETH_SESSION_HEADER);

        if (StringUtils.hasText(shibbolethSession)) {

            // Auth type 1: Institution login via Shibboleth with SAML2
            credential.setDelegationProtocol(DelegationProtocol.SAML_SHIB);
            credential.setRemotePrincipal(Boolean.TRUE);

            // Remove the shibboleth cookie. 1) Do not rely on the Shibboleth server to remove this cookie, which only
            // works only for normal flow. 2) CAS takes over and the cookie is no longer needed.
            removeShibbolethSessionCookie(context);

            // The header "REMOTE_USER" is still REQUIRED as an identifier for institution users although 1) OSF and CAS
            // already rely on ``username`` for identification purpose and 2) ``notifyRemotePrincipalAuthenticated()``
            // guarantees that ``username`` is provided. The reason for keeping this extra check in place is that
            // ``username`` is not always the identifier since it can be ``eppn``, ``mail`` or other attributes. The
            // header "REMOTE_USER" is defined as ``REMOTE_USER="eppn uid persistent-id targeted-id"``, in which every
            // attribute can be considered as the users' institution identity. In short, CAS requires at least one of
            // the four ID attributes ``eppn``, ``uid``, ``persistent-id``, ``targeted-id`` in addition to attributes
            // that are mapped to ``username`` and ``fullname``. Please see our Shibboleth server's configuration for
            // detailed mapping for attributes. The following link provides the best practice of using "REMOTE_USER":
            // https://wiki.shibboleth.net/confluence/display/SHIB2/NativeSPAttributeAccess
            final String remoteUser = request.getHeader(REMOTE_USER);
            if (StringUtils.isEmpty(remoteUser)) {
                logger.error("Invalid Remote User Specified as Empty");
                // _TO_DO_: Re-enable non-empty remote user requirement
                // _TO_DO_: Delay the exception until we know which the institution is and who the user is.
                // throw new RemoteUserFailedLoginException("Invalid Remote User specified as Empty");
            } else {
                logger.info("Remote User from HttpServletRequest '{}'", remoteUser);
            }

            // Retrieve all attributes from the headers
            for (final String headerName : Collections.list(request.getHeaderNames())) {
                if (headerName.startsWith(ATTRIBUTE_PREFIX)) {
                    final String headerValue = request.getHeader(headerName);
                    logger.debug("Remote User [{}] Auth Header '{}': '{}'", remoteUser, headerName, headerValue);
                    credential.getDelegationAttributes().put(
                            headerName.substring(ATTRIBUTE_PREFIX.length()),
                            headerValue
                    );
                }
            }

            // Parse the attributes and notify OSF of the remote principal authentication
            final PrincipalAuthenticationResult remoteUserInfo
                    = notifyRemotePrincipalAuthenticated(credential);

            // Build and return the credential
            credential.setUsername(remoteUserInfo.getUsername());
            credential.setInstitutionId(remoteUserInfo.getInstitutionId());
            if (StringUtils.isEmpty(remoteUser)) {
                logger.error(
                        "Empty Remote User: username={}, institutionId={}",
                        remoteUserInfo.getUsername(),
                        remoteUserInfo.getInstitutionId()
                );
            }

            return credential;
        } else if (ticketGrantingTicketId != null) {
            final TicketGrantingTicket ticketGrantingTicket;
            try {
                ticketGrantingTicket = centralAuthenticationService.getTicket(
                        ticketGrantingTicketId,
                        TicketGrantingTicket.class
                );
            } catch (final InvalidTicketException e) {
                // The TGT associated with the tgtId has been expired or removed. Return `null`.
                logger.warn("Invalid Ticket Granting Ticket");
                return null;
            }

            // Auth type 2: pac4j-based auth delegation login
            final Authentication authentication;
            final Principal principal;
            try {
                authentication = ticketGrantingTicket.getAuthentication();
                principal = authentication.getPrincipal();
            } catch (final NullPointerException e) {
                logger.error("Cannot Retrieve Authentication Principal");
                throw new RemoteUserFailedLoginException("Cannot Retrieve Authentication Principal");
            }

            String clientName = null;
            if (authentication.getAttributes().containsKey("clientName")) {
                clientName = (String) authentication.getAttributes().get("clientName");
            }

            // Auth type 2.1: ORCiD login via OAuth
            if (OrcidClient.class.getSimpleName().equals(clientName)) {
                credential.setDelegationProtocol(DelegationProtocol.OAUTH_PAC4J);
                credential.setRemotePrincipal(Boolean.TRUE);
                return credential;
            }

            // Auth type 2.2: Institution login via CAS
            // _TODO_: Please add a check here to ensure that 1) `clientName` is not null and that 2) `clientName`
            //         matches one of the configured institutions that uses CAS for delegation
            credential.setDelegationProtocol(DelegationProtocol.CAS_PAC4J);
            credential.setRemotePrincipal(Boolean.TRUE);
            credential.getDelegationAttributes().put("Cas-Identity-Provider", clientName);
            if (principal.getAttributes().size() > 0) {
                for (final Map.Entry<String, Object> entry : principal.getAttributes().entrySet()) {
                    logger.debug("Remote User [{}] Auth Header '{}': '{}'", principal.getId(), entry.getKey(), entry.getValue());
                    credential.getDelegationAttributes().put(entry.getKey(), (String) entry.getValue());
                }
            } else {
                // CAS servers must provide required attributes such as user's email and one valid type of name
                logger.error("Empty Attributes Map for Remote User [{}] with Client [{}]", principal.getId(), clientName);
            }

            // Notify the OSF of the remote principal authentication.
            final PrincipalAuthenticationResult remoteUserInfo
                    = notifyRemotePrincipalAuthenticated(credential);

            credential.setUsername(remoteUserInfo.getUsername());
            credential.setInstitutionId(remoteUserInfo.getInstitutionId());

            // We create a new tgt w/ the osf specific credential, cleanup the existing one from pac4j.
            this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicketId);
            return credential;
        } else if (request.getParameter("username") != null && request.getParameter("verification_key") != null) {
            // Authentication through username and verification key.
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
    protected PrincipalAuthenticationResult notifyRemotePrincipalAuthenticated(
            final OpenScienceFrameworkCredential credential
    ) throws AccountException {

        // Step 1 - Normalize the remote principal
        final JSONObject normalizedPayload;
        try {
            normalizedPayload = this.normalizeRemotePrincipal(credential);
        } catch (final ParserConfigurationException | TransformerException e) {
            logger.error("Failed to normalize remote principal: {}", e.getMessage());
            logger.trace("Failed to normalize remote principal: {}", e);
            throw new RemoteUserFailedLoginException("Failed to normalize remote principal.");
        }

        // Step 2 - Verify institution and user info in the normalized payload
        final JSONObject provider = normalizedPayload.optJSONObject("provider");
        if (provider == null) {
            logger.error("Invalid remote principal: provider is required");
            throw new RemoteUserFailedLoginException("Invalid remote principal: missing provider.");
        }
        final String institutionId = provider.optString("id").trim();
        if (institutionId.isEmpty()) {
            logger.error("Invalid remote principal: institution provider ID is required");
            throw new RemoteUserFailedLoginException("Invalid remote principal: missing institution.");
        }
        final JSONObject user = provider.optJSONObject("user");
        if (user == null) {
            logger.error("Invalid remote principal: user is required");
            throw new RemoteUserFailedLoginException("Invalid remote principal: missing user.");
        }
        final String username = user.optString("username").trim();
        final String fullname = user.optString("fullname").trim();
        final String givenName = user.optString("givenName").trim();
        final String familyName = user.optString("familyName").trim();
        if (username.isEmpty()) {
            logger.error("Invalid remote principal: username (email) is required for institution '{}'", institutionId);
            throw new RemoteUserFailedLoginException("Invalid remote principal: missing username.");
        }
        if (fullname.isEmpty() && (givenName.isEmpty() || familyName.isEmpty())) {
            logger.error("Invalid remote principal: fullname or (givenNaame, familyName) is required for institution '{}'", institutionId);
            throw new RemoteUserFailedLoginException("Invalid remote principal: missing names.");
        }
        final String payload = normalizedPayload.toString();
        logger.info("Notify Remote Principal Authenticated: username={}, institution={}", username, institutionId);
        logger.debug(
                "Notify Remote Principal Authenticated: username={}, institution={}, normalizedPayload = {}",
                username,
                institutionId,
                payload
        );

        // Step 3 - Encrypt the payload
        final String jweString;
        try {
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
            jweString = jweObject.serialize();
        } catch (final JOSEException e) {
            logger.error("Failed to build JWE payload: {}", e.getMessage());
            logger.trace("Failed to build JWE payload: {}", e);
            throw new RemoteUserFailedLoginException("Failed to build JWE payload for institution authentication");
        }

        // Step 4 - Make the API request with encrypted payload.
        try {
            // A call is made to the OSF CAS institution login endpoint to create a registered user (if one does not
            // already exist) and apply institutional affiliation (if the user is not already registered).
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

            // Return the username for the credential build.
            return new PrincipalAuthenticationResult(username, institutionId);
        } catch (final IOException e) {
            logger.error("Notify Remote Principal Authenticated Exception: {}", e.getMessage());
            logger.trace("Notify Remote Principal Authenticated Exception: {}", e);
            throw new RemoteUserFailedLoginException("Failed to communicate with OSF API endpoint.");
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
    protected JSONObject normalizeRemotePrincipal(final OpenScienceFrameworkCredential credential)
            throws ParserConfigurationException, TransformerException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.newDocument();
        final Element rootElement = document.createElement("auth");

        document.appendChild(rootElement);

        // Add a custom attribute for Delegation-Protocol to the transformation
        final Element delegationProtocolAttr = document.createElement("attribute");
        delegationProtocolAttr.setAttribute("name", "Delegation-Protocol");
        delegationProtocolAttr.setAttribute("value", credential.getDelegationProtocol().getId());
        rootElement.appendChild(delegationProtocolAttr);

        // Add delegated attributes to the transformation
        for (final String key : credential.getDelegationAttributes().keySet()) {
            final Element attribute = document.createElement("attribute");
            attribute.setAttribute("name", key);
            attribute.setAttribute("value", credential.getDelegationAttributes().get(key));
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

    public CentralAuthenticationService getCentralAuthenticationService() {
        return centralAuthenticationService;
    }

    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    public void setPrincipalFactory(final PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }

    /**
     * Hook method to allow for additional processing of the response before returning an error event.
     *
     * @param context the context for this specific request.
     * @param credential the credential for this request.
     */
    protected void onError(final RequestContext context, final Credential credential) {
        // default implementation does nothing
    }

    /**
     * Hook method to allow for additional processing of the response before returning a success event.
     *
     * @param context the context for this specific request.
     * @param credential the credential for this request.
     */
    protected void onSuccess(final RequestContext context, final Credential credential) {
        // default implementation does nothing
    }

    /**
     * Remove Shibboleth Session Cookie.
     *
     * In normal institution logout flow, the shibboleth logout endpoint clears this cookie, which happens when
     * institution-authenticated user clicks the "Log out" button on OSF. However, CAS's built in exception handler
     * does not clear this cookie when an exception happens.
     *
     * @param context   the Request Context
     */
    private void removeShibbolethSessionCookie(final RequestContext context) {

        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
            for (final Cookie cookie : cookies) {
                if (cookie.getName().startsWith(SHIBBOLETH_COOKIE_PREFIX)) {
                    final Cookie shibbolethCookie = new Cookie(cookie.getName(), null);
                    shibbolethCookie.setMaxAge(0);
                    response.addCookie(shibbolethCookie);
                }
            }
        }
    }
}
