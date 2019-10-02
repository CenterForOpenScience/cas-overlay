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
package org.jasig.cas.support.pac4j.web.flow;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang3.StringUtils;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.support.pac4j.authentication.principal.ClientCredential;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.web.support.WebUtils;

import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.Mechanism;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

/**
 * The Client Action.
 *
 * This class takes care of the role of CAS who serves as an client of a given auth protocol.
 *
 * This class represents an action to put at the beginning of the web flow. Before any authentication, redirection
 * urls are computed for the different clients defined as well as the theme, locale, method and service are saved
 * into the web session. After authentication, appropriate information are expected on this callback url to finish
 * the authentication process with the provider.
 *
 * @author Jerome Leleu
 * @author Longze Chen
 * @since 4.1.5
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class ClientAction extends AbstractAction {

    /** Constant for the service parameter. */
    public static final String SERVICE = "service";

    /** Constant for the theme parameter. */
    public static final String THEME = "theme";

    /** Constant for the locale parameter. */
    public static final String LOCALE = "locale";

    /** Constant for the method parameter. */
    public static final String METHOD = "method";

    /** Supported protocols. */
    private static final Set<Mechanism> SUPPORTED_PROTOCOLS = ImmutableSet.of(
            Mechanism.CAS_PROTOCOL,
            Mechanism.OAUTH_PROTOCOL,
            Mechanism.OPENID_PROTOCOL,
            Mechanism.SAML_PROTOCOL,
            Mechanism.OPENID_CONNECT_PROTOCOL
    );

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(ClientAction.class);

    /** The clients used for authentication. */
    @NotNull
    private final Clients clients;

    /** The service for CAS authentication. */
    @NotNull
    private final CentralAuthenticationService centralAuthenticationService;

    /**
     * Instantiate a new {@link ClientAction}.
     *
     * @param theCentralAuthenticationService the service for CAS authentication
     * @param theClients the clients for authentication
     */
    public ClientAction(
            final CentralAuthenticationService theCentralAuthenticationService,
            final Clients theClients
    ) {
        this.centralAuthenticationService = theCentralAuthenticationService;
        this.clients = theClients;
        ProfileHelper.setKeepRawData(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event doExecute(final RequestContext context) throws Exception {

        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
        final HttpSession session = request.getSession();

        // Create a new web context
        final WebContext webContext = new J2EContext(request, response);

        // Get the client name
        final String clientName = request.getParameter(this.clients.getClientNameParameter());
        logger.debug("clientName: {}", clientName);

        // If `client_name` found in the request parameter, do the authentication / authorization (auth).
        if (StringUtils.isNotBlank(clientName)) {

            // 1. Retrieve the client
            final BaseClient<Credentials, CommonProfile> client
                    = (BaseClient<Credentials, CommonProfile>) this.clients.findClient(clientName);
            logger.debug("client: {}", client);

            // 2. Check supported protocols
            final Mechanism mechanism = client.getMechanism();
            if (!SUPPORTED_PROTOCOLS.contains(mechanism)) {
                throw new TechnicalException("Only CAS, OAuth, OpenID and SAML protocols are supported: " + client);
            }

            // 3. Retrieve credentials
            final Credentials credentials;
            try {
                credentials = client.getCredentials(webContext);
                logger.debug("credentials: {}", credentials);
            } catch (final RequiresHttpAction e) {
                // Abort auth if fails to get credentials
                logger.debug("requires http action: {}", e.toString());
                response.flushBuffer();
                final ExternalContext externalContext = ExternalContextHolder.getExternalContext();
                externalContext.recordResponseComplete();
                return new Event(this, "stop");
            }

            // 4. Retrieve saved parameters from the web session
            final Service service = (Service) session.getAttribute(SERVICE);
            context.getFlowScope().put(SERVICE, service);
            logger.debug("retrieve service: {}", service);
            if (service != null) {
                request.setAttribute(SERVICE, service.getId());
            }
            restoreRequestAttribute(request, session, THEME);
            restoreRequestAttribute(request, session, LOCALE);
            restoreRequestAttribute(request, session, METHOD);

            // 5. Attempt to authenticate if the credential is not null
            if (credentials != null) {
                final TicketGrantingTicket tgt =
                        this.centralAuthenticationService.createTicketGrantingTicket(new ClientCredential(credentials));
                WebUtils.putTicketGrantingTicketInScopes(context, tgt);
                return success();
            }
        }

        // If `client_name` is not in the request params - no auth, or if `credentials == null` - aborted auth, simply
        // prepare the login context with clients info and then go to the original start point of the CAS login web
        // flow: refer to "cas-server-webapp/src/main/webapp/WEB-INF/webflow/login/login-webflow.xml" for detail.
        prepareForLoginPage(context);
        return error();
    }

    /**
     * Prepare the data for the login page.
     *
     * @param context the current web flow context
     */
    protected void prepareForLoginPage(final RequestContext context) {

        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
        final HttpSession session = request.getSession();

        // Create a new web context.
        final WebContext webContext = new J2EContext(request, response);

        // Save service and other parameters in the web session.
        final WebApplicationService service = WebUtils.getService(context);
        logger.debug("save service: {}", service);
        session.setAttribute(SERVICE, service);
        saveRequestParameter(request, session, THEME);
        saveRequestParameter(request, session, LOCALE);
        saveRequestParameter(request, session, METHOD);

        // Generate redirection (login) URLs for all clients and store them in the flow scope of the web context.
        for (final Client client : this.clients.findAllClients()) {
            final String key = client.getName() + "Url";
            final BaseClient baseClient = (BaseClient) client;
            final String redirectionUrl = baseClient.getRedirectionUrl(webContext);
            logger.debug("{} -> {}", key, redirectionUrl);
            context.getFlowScope().put(key, redirectionUrl);
        }
    }

    /**
     * Restore an attribute in web session as an attribute in request.
     *
     * @param request the HTTP request
     * @param session the HTTP session
     * @param name the name of the parameter
     */
    private void restoreRequestAttribute(
            final HttpServletRequest request,
            final HttpSession session,
            final String name
    ) {
        final String value = (String) session.getAttribute(name);
        request.setAttribute(name, value);
    }

    /**
     * Save a request parameter in the web session.
     *
     * @param request the HTTP request
     * @param session the HTTP session
     * @param name the name of the parameter
     */
    private void saveRequestParameter(
            final HttpServletRequest request,
            final HttpSession session,
            final String name
    ) {
        final String value = request.getParameter(name);
        if (value != null) {
            session.setAttribute(name, value);
        }
    }
}
