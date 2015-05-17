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
package org.jasig.cas.support.oauth.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthTokenUtils;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.util.CipherExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This controller handles requests for grant type authorization code,
 * returning an access token which is the CAS service ticket and a refresh token
 * which is the CAS granting ticket according to the service and code (service ticket) given.
 *
 * @author Jerome Leleu
 * @author Michael Haselton
 * @since 4.1.0
 */
public final class OAuth20TokenAuthorizationCodeController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20TokenAuthorizationCodeController.class);

    private final ServicesManager servicesManager;

    private final TicketRegistry ticketRegistry;

    private final CentralAuthenticationService centralAuthenticationService;

    private final CipherExecutor cipherExecutor;

    private final long timeout;

    /**
     * Instantiates a new o auth20 grant type authorization code controller.
     *
     * @param servicesManager the services manager
     * @param ticketRegistry the ticket registry
     * @param centralAuthenticationService the central authentication service
     * @param timeout the timeout
     */
    public OAuth20TokenAuthorizationCodeController(final ServicesManager servicesManager, final TicketRegistry ticketRegistry,
                                                   final CentralAuthenticationService centralAuthenticationService,
                                                   final CipherExecutor cipherExecutor, final long timeout) {
        this.servicesManager = servicesManager;
        this.ticketRegistry = ticketRegistry;
        this.centralAuthenticationService = centralAuthenticationService;
        this.cipherExecutor = cipherExecutor;
        this.timeout = timeout;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final HttpSession session = request.getSession();

        final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
        LOGGER.debug("{} : {}", OAuthConstants.REDIRECT_URI, redirectUri);

        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_ID, clientId);

        final String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);

        final String code = request.getParameter(OAuthConstants.CODE);
        LOGGER.debug("{} : {}", OAuthConstants.CODE, code);

        final String serviceTicketId = cipherExecutor.decode(code);
        LOGGER.debug("Service Ticket : {}", serviceTicketId);

        final boolean isVerified = verifyRequest(redirectUri, clientId, clientSecret, code);
        if (!isVerified) {
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_BAD_REQUEST);
        }

        final ServiceTicket serviceTicket = (ServiceTicket) ticketRegistry.getTicket(serviceTicketId);
        // service ticket should be valid
        if (serviceTicket == null || serviceTicket.isExpired()) {
            LOGGER.error("{} (Service Ticket) expired : {}", OAuthConstants.CODE, serviceTicketId);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT, HttpStatus.SC_BAD_REQUEST);
        }

        final TicketGrantingTicket ticketGrantingTicket = serviceTicket.getGrantingTicket();
        // remove service ticket
        ticketRegistry.deleteTicket(serviceTicket.getId());

        final Principal loginPrincipal = ticketGrantingTicket.getAuthentication().getPrincipal();

        // do we have an existing refresh token? (determined in the authorization callback controller)
        final String refreshTokenId = (String) session.getAttribute(OAuthConstants.OAUTH20_REFRESH_TOKEN_ID);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_REFRESH_TOKEN_ID, refreshTokenId);
        session.removeAttribute(OAuthConstants.OAUTH20_REFRESH_TOKEN_ID);

        final TicketGrantingTicket refreshTicket;
        if (!StringUtils.isBlank(refreshTokenId)) {
            refreshTicket = (TicketGrantingTicket) ticketRegistry.getTicket(refreshTokenId);
        } else {
            refreshTicket = OAuthTokenUtils.fetchRefreshTicket(centralAuthenticationService, clientId, loginPrincipal);
        }
        if (refreshTicket == null) {
            LOGGER.error("Could not fetch refresh ticket [{}]", loginPrincipal);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT, HttpStatus.SC_BAD_REQUEST);
        }

        final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(servicesManager, clientId);
        if (registeredService == null) {
            LOGGER.error("Could not find registered service for client id [{}]", clientId);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT, HttpStatus.SC_BAD_REQUEST);
        }

        final Service service = new SimpleWebApplicationServiceImpl(registeredService.getServiceId());
        final ServiceTicket accessTicket = OAuthTokenUtils.fetchAccessTicket(centralAuthenticationService, refreshTicket, service);

        final int expires = (int) (timeout - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - accessTicket.getCreationTime()));

        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, Object> map = new HashMap<>();
        map.put(OAuthConstants.ACCESS_TOKEN, OAuthTokenUtils.getJsonWebToken(cipherExecutor, accessTicket));
        map.put(OAuthConstants.REFRESH_TOKEN, OAuthTokenUtils.getJsonWebToken(cipherExecutor, refreshTicket, service));
        map.put(OAuthConstants.EXPIRES_IN, expires);
        map.put(OAuthConstants.TOKEN_TYPE, OAuthConstants.BEARER_TOKEN);

        final String result = mapper.writeValueAsString(map);
        LOGGER.debug("result : {}", result);

        response.setContentType("application/json; charset=UTF-8");
        return OAuthUtils.writeText(response, result, HttpStatus.SC_OK);
    }

    /**
     * Verify the request by reviewing the values of client id, redirect uri, client secret, code, etc.
     *
     * @param redirectUri the redirect uri
     * @param clientId the client id
     * @param clientSecret the client secret
     * @param code the code
     * @return true, if successful
     */
    private boolean verifyRequest(final String redirectUri, final String clientId, final String clientSecret,
                                  final String code) {
        // clientId is required
        if (StringUtils.isBlank(clientId)) {
            LOGGER.error("Missing {}", OAuthConstants.CLIENT_ID);
            return false;
        }
        // clientSecret is required
        if (StringUtils.isBlank(clientSecret)) {
            LOGGER.error("Missing {}", OAuthConstants.CLIENT_SECRET);
            return false;
        }
        // code is required
        if (StringUtils.isBlank(code)) {
            LOGGER.error("Missing {}", OAuthConstants.CODE);
            return false;
        }
        // redirectUri is required
        if (StringUtils.isBlank(redirectUri)) {
            LOGGER.error("Missing {}", OAuthConstants.REDIRECT_URI);
            return false;
        }

        final OAuthRegisteredService service = OAuthUtils.getRegisteredOAuthService(servicesManager, clientId);
        if (service == null) {
            LOGGER.error("Unknown {} : {}", OAuthConstants.CLIENT_ID, clientId);
            return false;
        }
        if (!StringUtils.equals(service.getClientSecret(), clientSecret)) {
            LOGGER.error("Wrong client secret for service {}", service);
            return false;
        }
        final String serviceId = service.getServiceId();
        if (!redirectUri.matches(serviceId)) {
            LOGGER.error("Unsupported {} : {} for serviceId : {}", OAuthConstants.REDIRECT_URI, redirectUri, serviceId);
            return false;
        }

        return true;
    }
}
