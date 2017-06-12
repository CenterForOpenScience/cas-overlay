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

package io.cos.cas.web.flow;

import io.cos.cas.api.handler.ApiEndpointHandler;
import io.cos.cas.api.type.ApiEndpoint;
import io.cos.cas.api.util.AbstractApiEndpointUtils;
import io.cos.cas.authentication.OpenScienceFrameworkCredential;
import io.cos.cas.authentication.exceptions.RemoteUserFailedLoginException;
import io.cos.cas.types.DelegationProtocol;
import io.cos.cas.web.util.AbstractFlowUtils;
import io.cos.pac4j.oauth.client.OrcidClient;

import org.apache.http.HttpStatus;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
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
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
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
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public class OpenScienceFrameworkPrincipalFromRequestRemoteUserNonInteractiveCredentialsAction
            extends AbstractAction {
    /**
     * The Principal Authentication Result.
     *
     * @author Longze Chen
     * @since 4.1.5
     */
    public static class PrincipalAuthenticationResult {

        private String username;
        private String institutionProvider;
        private String externalIdProvider;
        private String externalId;

        /**
         * Instantiate Principal Authentication Result.
         *
         * @param username the username
         * @param institutionProvider the institution provider id
         * @param externalIdProvider the external provider id
         * @param externalId the external id
         */
        public PrincipalAuthenticationResult(
                final String username,
                final String institutionProvider,
                final String externalIdProvider,
                final String externalId
        ) {
            this.username = username;
            this.institutionProvider = institutionProvider;
            this.externalIdProvider = externalIdProvider;
            this.externalId = externalId;
        }

        public String getUsername() {
            return username;
        }

        public String getInstitutionProvider() {
            return institutionProvider;
        }

        public String getExternalIdProvider() {
            return externalIdProvider;
        }

        public String getExternalId() {
            return externalId;
        }
    }

    private static final String CONST_CREDENTIAL = "credential";

    private static final String AUTHENTICATION_FAILURE = "authenticationFailure";

    private static final String REMOTE_USER = "REMOTE_USER";

    private static final String ATTRIBUTE_PREFIX = "AUTH-";

    private static final String SHIBBOLETH_SESSION_HEADER = ATTRIBUTE_PREFIX + "Shib-Session-ID";

    private static final String SHIBBOLETH_COOKIE_PREFIX = "_shibsession_";

    /** The Logger Instance. */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The Principal factory. */
    protected PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    @NotNull
    private ApiEndpointHandler apiEndpointHandler;

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

        // For external IdPs such as ORCiD which use OAUTH_PAC4J and do not release email, credential.isRemotePrincipal()
        // is false. Redirect to account flow for user to submit their email.
        if (DelegationProtocol.OAUTH_PAC4J.equals(credential.getDelegationProtocol()) && !credential.isRemotePrincipal()) {
            final String originalServiceUrl = ((SimpleWebApplicationServiceImpl) context.getFlowScope().get("service")).getOriginalUrl();
            final String createOrLinkOsfAccountUrl = apiEndpointHandler.getCasCreateOrLinkOsfAccountUrl()
                    + "service="
                    + originalServiceUrl;

            AbstractFlowUtils.clearAndPutCredentialToSessionScope(context, credential);
            context.getFlowScope().put("createOrLinkOsfAccountUrl", createOrLinkOsfAccountUrl);
            return new Event(this, "createOrLink");
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
        // Note 1:  Do not use `WebUtils.getCredential(RequestContext context)`, it will make the credential`null`.
        // Note 2:  Should check both FlowScope and RequestScope? And write an `.getCredential(RequestContext context)`
        //          which are compatible with OpenScienceFrameworkCredential
        final OpenScienceFrameworkCredential credential
                = (OpenScienceFrameworkCredential) context.getFlowScope().get(CONST_CREDENTIAL);
        final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);

        // WARNING: Do not assume this works w/o acceptance testing in a Production environment.
        // The call is made to trust these headers only because we assume the Apache Shibboleth Service Provider module
        // rejects any conflicting / forged headers.
        final String shibbolethSession = request.getHeader(SHIBBOLETH_SESSION_HEADER);
        if (StringUtils.hasText(shibbolethSession)) {
            credential.setDelegationProtocol(DelegationProtocol.SAML_SHIB);
            credential.setRemotePrincipal(Boolean.TRUE);

            // remove the shibboleth cookie
            // do not rely on the Shibboleth server to remove this cookie, which only works only for normal flow
            removeShibbolethSessionCookie(context);

            // SAML-Shibboleth based institution login
            final String remoteUser = request.getHeader(REMOTE_USER);
            if (StringUtils.isEmpty(remoteUser)) {
                logger.error("Invalid Remote User specified as Empty");
                throw new RemoteUserFailedLoginException("Invalid Remote User specified as Empty");
            }
            logger.info("Remote User from HttpServletRequest '{}'", remoteUser);

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

            // Notify the OSF of the remote principal authentication.
            final PrincipalAuthenticationResult remoteUserInfo
                    = notifyRemotePrincipalAuthenticated(credential);
            credential.setUsername(remoteUserInfo.getUsername());
            credential.setInstitutionId(remoteUserInfo.getInstitutionProvider());
            return credential;
        } else if (ticketGrantingTicketId != null) {
            // pac4j login
            final TicketGrantingTicket ticketGrantingTicket;
            try {
                ticketGrantingTicket = centralAuthenticationService.getTicket(
                        ticketGrantingTicketId,
                        TicketGrantingTicket.class
                );
            } catch (final InvalidTicketException e) {
                logger.error("Invalid Ticket Granting Ticket");
                throw new RemoteUserFailedLoginException("Invalid Ticket Granting Ticket");
            }

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

            if (OrcidClient.class.getSimpleName().equals(clientName)) {
                credential.setDelegationProtocol(DelegationProtocol.OAUTH_PAC4J);
                credential.setNonInstitutionExternalId(principal.getId());
                for (final Map.Entry<String, Object> entry : principal.getAttributes().entrySet()) {
                    credential.getDelegationAttributes().put(entry.getKey(), (String) entry.getValue().toString());
                }
                final PrincipalAuthenticationResult remoteUserInfo
                        = notifyNonInstitutionRemotePrincipalAuthenticated(credential);
                if (remoteUserInfo != null) {
                    credential.setUsername(remoteUserInfo.getUsername());
                    credential.setRemotePrincipal(Boolean.TRUE);
                } else {
                    credential.setNonInstitutionExternalIdProvider("ORCID");
                    credential.setNonInstitutionExternalId(credential.getNonInstitutionExternalId().split("#")[1].trim());
                    credential.setRemotePrincipal(Boolean.FALSE);
                }
                // clean up the tgt from pac4j
                this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicketId);
                return credential;
            }

            // CAS pac4j institution login.
            credential.setDelegationProtocol(DelegationProtocol.CAS_PAC4J);
            credential.setRemotePrincipal(Boolean.TRUE);
            credential.getDelegationAttributes().put("Cas-Identity-Provider", clientName);
            for (final Map.Entry<String, Object> entry : principal.getAttributes().entrySet()) {
                logger.debug("Remote User [{}] Auth Header '{}': '{}'", principal.getId(), entry.getKey(), entry.getValue());
                credential.getDelegationAttributes().put(entry.getKey(), (String) entry.getValue());
            }

            // Notify the OSF of the remote principal authentication.
            final PrincipalAuthenticationResult remoteUserInfo
                    = notifyRemotePrincipalAuthenticated(credential);

            credential.setUsername(remoteUserInfo.getUsername());
            credential.setInstitutionId(remoteUserInfo.getInstitutionProvider());

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
     * Securely notify the OSF API of a successful authentication from a Non-institution External ID Provider. Ask API
     * whether a user with this external identity exists.
     *
     * @param credential the credential object bearing the authentication attributes from the external idp
     * @return if user found, return the username and external ID, otherwise return null
     * @throws AccountException if API request fails
     */
    private PrincipalAuthenticationResult notifyNonInstitutionRemotePrincipalAuthenticated(
            final OpenScienceFrameworkCredential credential
    ) throws AccountException {

        final JSONObject user = new JSONObject();
        final JSONObject data = new JSONObject();

        user.put("externalIdWithProvider", credential.getNonInstitutionExternalId());
        data.put("type", "EXTERNAL_AUTHENTICATE");
        data.put("user", user);

        final JSONObject response = apiEndpointHandler.handle(
                ApiEndpoint.AUTH_EXTERNAL,
                apiEndpointHandler.encryptPayload("data", data.toString())
        );

        if (response != null) {
            final int statusCode = response.getInt("status");
            if (statusCode == HttpStatus.SC_OK) {
                final JSONObject responseBody = response.getJSONObject("body");
                if (responseBody != null && responseBody.has("username")) {
                    final String username = responseBody.getString("username");
                    logger.info(
                            "External Identity Found in OSF: username = {}, externalIdWithProvider = {}",
                            username,
                            credential.getNonInstitutionExternalId()
                    );
                    return new PrincipalAuthenticationResult(
                            username,
                            null,
                            credential.getNonInstitutionExternalIdProvider(),
                            credential.getNonInstitutionExternalId()
                    );
                }
            } else if (statusCode == HttpStatus.SC_FORBIDDEN || statusCode == HttpStatus.SC_UNAUTHORIZED) {
                final String errorDetail = apiEndpointHandler.getErrorMessageFromResponseBody(response.getJSONObject("body"));
                if (AbstractApiEndpointUtils.ACCOUNT_NOT_FOUND.equals(errorDetail)) {
                    logger.info(
                            "External Identity Not Found in OSF: externalIdWithProvider = {}",
                            credential.getNonInstitutionExternalId()
                    );
                    return null;
                }
            }
        }
        logger.error(
                "Notify Non-institution Remote Authenticated Exception: externalIdWithProvider = {}",
                credential.getNonInstitutionExternalId()
        );
        throw new RemoteUserFailedLoginException("Notify Non-institution Remote Authenticated Exception");
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
         try {
             final JSONObject normalizedPayload = this.normalizeRemotePrincipal(credential);
             normalizedPayload.put("type", "INSTITUTION_AUTHENTICATE");

             final JSONObject provider = normalizedPayload.getJSONObject("provider");
             final String institutionId = provider.getString("id");
             final String username = provider.getJSONObject("user").getString("username");
             logger.info("Notify Remote Principal Authenticated: username={}, institution={}", username, institutionId);

             final String encryptedPayload
                     = apiEndpointHandler.encryptPayload("data", normalizedPayload.toString());
             final JSONObject response
                     = apiEndpointHandler.handle(ApiEndpoint.AUTH_INSTITUTION, encryptedPayload);

             if (response != null) {
                 final int statusCode = response.getInt("status");
                 logger.info(
                         "Notify Remote Principal Authenticated [OSF API] Response: <{}> Status Code {}",
                         username,
                         statusCode
                 );
                 if (statusCode == HttpStatus.SC_NO_CONTENT) {
                     return new PrincipalAuthenticationResult(username, institutionId, null, null);
                 }
             }
             throw new RemoteUserFailedLoginException("Invalid Status Code from OSF API Endpoint");
         } catch (final ParserConfigurationException | TransformerException e) {
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

    public void setApiEndpointHandler(final ApiEndpointHandler apiEndpointHandler) {
        this.apiEndpointHandler = apiEndpointHandler;
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
