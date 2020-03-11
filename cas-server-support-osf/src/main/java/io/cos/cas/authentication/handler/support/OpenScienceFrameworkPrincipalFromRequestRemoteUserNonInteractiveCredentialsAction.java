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
import io.cos.cas.authentication.exceptions.DelegatedLoginException;
import io.cos.cas.authentication.exceptions.InstitutionLoginFailedAttributesMissingException;
import io.cos.cas.authentication.exceptions.InstitutionLoginFailedAttributesParsingException;
import io.cos.cas.authentication.exceptions.InstitutionLoginFailedOsfApiException;
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
import javax.security.auth.login.FailedLoginException;
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
 * TODO: rewrite this outdated JavaDoc along with refactoring {@link InstitutionLoginFailedException}
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
     * Construct the OSF credential from the request context if any of the following non-interactive authenticated
     * states is found. Return NULL otherwise if not found or if invalid.
     *
     * 1. Institution login via SAML / Shibboleth
     * 2. ORCiD login via OAuth / Pac4j
     * 3. Institution login via CAS / Pac4j
     * 4. Username and one-time verification key login
     *
     * Note: The order matters here. For example, if an Shibboleth session is found, none of the other cases will be
     *       handled. In addition, they are mutually exclusive. For example, if one is detected but failed, the method
     *       either returns NULL or raises an exception without checking others.
     *
     * @param context a {@link RequestContext} object which stores the current request context
     * @return the constructed {@link OpenScienceFrameworkCredential} object or <code>null</code> if neither a valid
     * TGT nor a Shibboleth session is found
     * @throws AccountException if errors happen during credential construction.
     */
    protected OpenScienceFrameworkCredential constructCredential(
            final RequestContext context
    ) throws AccountException, FailedLoginException {

        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);

        // WARN: Do not use `WebUtils.getCredential(RequestContext context)`, it will make the credential `null`.
        // TODO: Check both `FlowScope` and `RequestScope`. Write a `.getCredential(RequestContext context)` which
        //       will be compatible with `OpenScienceFrameworkCredential`.
        final OpenScienceFrameworkCredential credential
                = (OpenScienceFrameworkCredential) context.getFlowScope().get(CONST_CREDENTIAL);

        final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);

        // WARN: Do not assume this works w/o acceptance testing in a Production environment.
        // The call is made to trust these headers only because we assume the Apache Shibboleth Service Provider module
        // rejects any conflicting / forged headers.
        final String shibbolethSession = request.getHeader(SHIBBOLETH_SESSION_HEADER);

        if (StringUtils.hasText(shibbolethSession)) {

            // AUTH TYPE 1: Institution login via Shibboleth with SAML
            credential.setDelegationProtocol(DelegationProtocol.SAML_SHIB);
            credential.setRemotePrincipal(Boolean.TRUE);

            // Remove the Shibboleth cookie since: 1) we can not rely on the Shibboleth server to remove this cookie,
            // which only works for normal web flow; and 2) CAS takes over after successful SAML authentication and the
            // cookie is no longer needed anyway.
            removeShibbolethSessionCookie(context);

            // The header "REMOTE_USER" is still REQUIRED as an identifier for institution users although: 1) OSF only
            // relies on `username` (email) for identification purpose; and 2) `notifyRemotePrincipalAuthenticated()`
            // guarantees that `username` is provided. The reason for keeping this extra check in place is that email
            // and identity should be separated though institutional emails are usually unique and are not reusable.
            // The header "REMOTE_USER" is defined as `REMOTE_USER="eppn persistent-id targeted-id uid upn"`, in which
            // each can be considered as a user's institution identity. In short, CAS requires at least one of them
            // in addition to attributes that are mapped to `username` and `fullname`. Please refer to the Shibboleth
            // server's configuration for detailed attribute mapping. The following link provides the best practice
            // of using "REMOTE_USER": https://wiki.shibboleth.net/confluence/display/SHIB2/NativeSPAttributeAccess
            final String remoteUser = request.getHeader(REMOTE_USER);
            if (StringUtils.isEmpty(remoteUser)) {
                logger.warn("[SAML Shibboleth] Missing or empty Shibboleth header: {}", REMOTE_USER);
                // TODO: Re-enable non-empty `REMOTE_USER` requirement
                // TODO: Delay throwing the exception until we know which the institution is and who the user is.
                // NOTE: Current warn instead of error due to the fact that some institutions have issue with releasing
                //       the identification attribute correctly.
                // throw new InstitutionLoginFailedException("Missing user's institutional identity");
            } else {
                logger.info("[SAML Shibboleth] User's institutional identity: '{}'", remoteUser);
            }

            // Retrieve all attributes from the Shibboleth request headers.
            for (final String headerName : Collections.list(request.getHeaderNames())) {
                if (headerName.startsWith(ATTRIBUTE_PREFIX)) {
                    final String headerValue = request.getHeader(headerName);
                    logger.debug(
                            "[SAML Shibboleth] User's institutional identity '{}' - auth header '{}': '{}'",
                            remoteUser,
                            headerName,
                            headerValue
                    );
                    credential.getDelegationAttributes().put(
                            headerName.substring(ATTRIBUTE_PREFIX.length()),
                            headerValue
                    );
                }
            }

            // Parse the attributes and notify OSF API of the remote principal authentication
            final PrincipalAuthenticationResult remoteUserInfo = notifyRemotePrincipalAuthenticated(credential);

            // Build and return the OSF-specific credential
            credential.setUsername(remoteUserInfo.getUsername());
            credential.setInstitutionId(remoteUserInfo.getInstitutionId());
            if (StringUtils.isEmpty(remoteUser)) {
                // This is a necessary duplicate warning log for missing user's institutional identity since it is not
                // until now / here that OSF CAS finally knows the user's email and the institution ID.
                logger.warn(
                        "[SAML Shibboleth] Missing user's institutional identity: username={}, institutionId={}",
                        remoteUserInfo.getUsername(),
                        remoteUserInfo.getInstitutionId()
                );
            }

            // Return the OSF credential
            return credential;
        } else if (ticketGrantingTicketId != null) {
            final TicketGrantingTicket ticketGrantingTicket;
            try {
                ticketGrantingTicket = centralAuthenticationService.getTicket(
                        ticketGrantingTicketId,
                        TicketGrantingTicket.class
                );
            } catch (final InvalidTicketException e) {
                // Return NULL (no credential) for invalid (expired or deleted) TGT.
                logger.warn("Invalid ticket granting ticket");
                return null;
            }

            // AUTH TYPE 2: Delegated login via non-SAML protocols implemented by PAC4J
            final Authentication authentication;
            final Principal principal;
            try {
                authentication = ticketGrantingTicket.getAuthentication();
                principal = authentication.getPrincipal();
            } catch (final NullPointerException e) {
                logger.error("[PAC4J Delegation] Failed to retrieve authentication principal");
                throw new DelegatedLoginException("Failed to retrieve authentication principal");
            }

            String clientName = null;
            if (authentication.getAttributes().containsKey("clientName")) {
                clientName = (String) authentication.getAttributes().get("clientName");
                // TODO: Log (info-level) the client name and principal ID here
            }

            // AUTH TYPE 2.1: ORCiD login via the OAuth protocol
            if (OrcidClient.class.getSimpleName().equals(clientName)) {
                credential.setDelegationProtocol(DelegationProtocol.OAUTH_PAC4J);
                credential.setRemotePrincipal(Boolean.TRUE);
                return credential;
            }

            // AUTH TYPE 2.2: Institution login via the CAS protocol
            // TODO: Add a check here to ensure that 1) `clientName` is not null and that 2) `clientName` matches one
            //       of the configured institutions that uses the CAS protocol for delegation
            credential.setDelegationProtocol(DelegationProtocol.CAS_PAC4J);
            credential.setRemotePrincipal(Boolean.TRUE);
            credential.getDelegationAttributes().put("Cas-Identity-Provider", clientName);
            if (principal.getAttributes().size() > 0) {
                for (final Map.Entry<String, Object> entry : principal.getAttributes().entrySet()) {
                    logger.debug(
                            "[CAS PAC4J] User's institutional identity '{}' - auth header '{}': '{}'",
                            principal.getId(),
                            entry.getKey(),
                            entry.getValue()
                    );
                    credential.getDelegationAttributes().put(entry.getKey(), (String) entry.getValue());
                }
            } else {
                // CAS IdP servers must provide required attributes such as user's email and full name.
                logger.error("[CAS PAC4J] No attributes for user '{} with client '{}'", principal.getId(), clientName);
            }

            // Parse the attributes and notify OSF API of the remote principal authentication
            final PrincipalAuthenticationResult remoteUserInfo = notifyRemotePrincipalAuthenticated(credential);

            credential.setUsername(remoteUserInfo.getUsername());
            credential.setInstitutionId(remoteUserInfo.getInstitutionId());

            // OSF CAS will create a new TGT with the to-be-returned OSF credential; and thus the existing TGT granted
            // from the successful PAC4J authentication must be cleared.
            this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicketId);

            // Return the OSF credential
            return credential;
        } else if (request.getParameter("username") != null && request.getParameter("verification_key") != null) {
            // AUTH TYPE 3: Username and one-time verification key login
            credential.setUsername(request.getParameter("username"));
            credential.setVerificationKey(request.getParameter("verification_key"));
            // Return the OSF credential
            return credential;
        }

        // Return NULL (no credential) if neither Shibboleth session nor a valid TGT exists.
        return null;
    }

    /**
     * Securely notify OSF API of a successful institution login between OSF CAS and an external identity provider.
     * Allows OSF API the opportunity to create a verified user account and assign institutional affiliation to the
     * user's OSF account. Refer to the following code for the latest behavior of how OSF API handles the request.
     *
     * OSF API: https://github.com/CenterForOpenScience/osf.io/blob/develop/api/institutions/authentication.py
     *
     * @param credential the credential object bearing the authentication headers from the IdP
     * @return {@link PrincipalAuthenticationResult} an object that stores the institution's ID and the user's username
     * @throws AccountException if there is an issue with the attributes or the API request
     */
    protected PrincipalAuthenticationResult notifyRemotePrincipalAuthenticated(
            final OpenScienceFrameworkCredential credential
    ) throws AccountException {

        // Step 1 - Normalize the remote principal
        final JSONObject normalizedPayload;
        try {
            normalizedPayload = this.normalizeRemotePrincipal(credential);
        } catch (final ParserConfigurationException | TransformerException e) {
            logger.error("[CAS XSLT] Failed to normalize attributes in the credential: {}", e.getMessage());
            throw new InstitutionLoginFailedAttributesParsingException("Attribute normalization failure");
        }

        // Step 2 - Verify institution and user info in the normalized payload
        final JSONObject provider = normalizedPayload.optJSONObject("provider");
        if (provider == null) {
            logger.error("[CAS XSLT] Missing identity provider.");
            throw new InstitutionLoginFailedAttributesMissingException("Missing identity provider");
        }
        final String institutionId = provider.optString("id").trim();
        if (institutionId.isEmpty()) {
            logger.error("[CAS XSLT] Empty identity provider");
            throw new InstitutionLoginFailedAttributesMissingException("Empty identity provider");
        }
        final JSONObject user = provider.optJSONObject("user");
        if (user == null) {
            logger.error("[CAS XSLT] Missing institutional user");
            throw new InstitutionLoginFailedAttributesMissingException("Missing institutional user");
        }
        final String username = user.optString("username").trim();
        final String fullname = user.optString("fullname").trim();
        final String givenName = user.optString("givenName").trim();
        final String familyName = user.optString("familyName").trim();
        if (username.isEmpty()) {
            logger.error("[CAS XSLT] Missing email (username) for user at institution '{}'", institutionId);
            throw new InstitutionLoginFailedAttributesMissingException("Missing email (username)");
        }
        if (fullname.isEmpty() && (givenName.isEmpty() || familyName.isEmpty())) {
            logger.error("[CAS XSLT] Missing names: username={}, institution={}", username, institutionId);
            throw new InstitutionLoginFailedAttributesMissingException("Missing user's names");
        }
        final String payload = normalizedPayload.toString();
        logger.info("[CAS XSLT] All attributes checked: username={}, institution={}", username, institutionId);
        logger.debug(
                "[CAS XSLT] All attributes checked: username={}, institution={}, normalizedPayload={}",
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
            logger.error(
                    "[OSF API] Notify Remote Principal Authenticated Failed: Payload Error - {}",
                    e.getMessage()
            );
            throw new InstitutionLoginFailedOsfApiException("OSF CAS failed to build JWT / JWE payload for OSF API");
        }

        // Step 4 - Make the OSF API request with the encrypted payload.
        try {
            final HttpResponse httpResponse = Request.Post(this.institutionsAuthUrl)
                    .addHeader(new BasicHeader("Content-Type", "text/plain"))
                    .bodyString(jweString, ContentType.APPLICATION_JSON)
                    .execute()
                    .returnResponse();
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            logger.info(
                    "[OSF API] Notify Remote Principal Authenticated Response: username={} statusCode={}",
                    username,
                    statusCode
            );
            // The OSF API institution authentication endpoint always returns the HTTP 204 No Content if successful.
            if (statusCode != HttpStatus.SC_NO_CONTENT) {
                final String responseString = new BasicResponseHandler().handleResponse(httpResponse);
                logger.error(
                        "[OSF API] Notify Remote Principal Authenticated Failed: statusCode={}, body={}",
                        statusCode,
                        responseString
                );
                throw new InstitutionLoginFailedOsfApiException("OSF API failed to process CAS request");
            }

            // Return user's username and the institution ID to build the OSF credential
            return new PrincipalAuthenticationResult(username, institutionId);
        } catch (final IOException e) {
            logger.error(
                    "[OSF API] Notify Remote Principal Authenticated Failed: Communication Error - {}",
                    e.getMessage()
            );
            throw new InstitutionLoginFailedOsfApiException("Communication Error between OSF CAS and OSF API");
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
