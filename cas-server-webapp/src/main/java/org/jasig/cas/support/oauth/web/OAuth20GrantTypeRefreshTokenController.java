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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * This controller handles requests for grant type refresh token,
 * returning an access token which is the CAS service ticket according
 * to the service and refresh token (granting ticket) given.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public final class OAuth20GrantTypeRefreshTokenController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20GrantTypeRefreshTokenController.class);

    private final ServicesManager servicesManager;

    private final TicketRegistry ticketRegistry;

    private final CentralAuthenticationService centralAuthenticationService;

    private final long timeout;

    /**
     * Instantiates a new o auth20 grant type refresh token controller.
     *
     * @param servicesManager the services manager
     * @param ticketRegistry the ticket registry
     * @param centralAuthenticationService the central authentication service
     * @param timeout the timeout
     */
    public OAuth20GrantTypeRefreshTokenController(final ServicesManager servicesManager, final TicketRegistry ticketRegistry,
                                                       CentralAuthenticationService centralAuthenticationService, final long timeout) {
        this.servicesManager = servicesManager;
        this.ticketRegistry = ticketRegistry;
        this.centralAuthenticationService = centralAuthenticationService;
        this.timeout = timeout;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_ID, clientId);

        final String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);

        final String refreshTokenId = request.getParameter(OAuthConstants.REFRESH_TOKEN);
        LOGGER.debug("{} : {}", OAuthConstants.REFRESH_TOKEN, refreshTokenId);

        final boolean isVerified = verifyRequest(clientId, clientSecret, refreshTokenId);
        if (!isVerified) {
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_BAD_REQUEST);
        }

        final TicketGrantingTicket refreshToken = (TicketGrantingTicket) ticketRegistry.getTicket(refreshTokenId);
        // refresh token should be valid
        if (refreshToken == null || refreshToken.isExpired()) {
            LOGGER.error("Refresh Token expired : {}", refreshTokenId);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT, HttpStatus.SC_BAD_REQUEST);
        }

        final ServiceTicket accessToken = fetchAccessToken(clientId, refreshToken);
        if (accessToken == null) {
            LOGGER.error("Could not fetch access token for : {}", refreshToken);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT, HttpStatus.SC_BAD_REQUEST);
        }

        final int expires = (int) (timeout - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - accessToken.getCreationTime()));
        final JSONObject result = new JSONObject();
        result.put(OAuthConstants.ACCESS_TOKEN, accessToken.getId());
        result.put(OAuthConstants.EXPIRES_IN, expires);
        result.put(OAuthConstants.TOKEN_TYPE, OAuthConstants.BEARER_TOKEN);
        LOGGER.debug("result : {}", result);

        response.setContentType("application/json; charset=UTF-8");
        return OAuthUtils.writeText(response, result.toString(), HttpStatus.SC_OK);
    }

    /**
     * Fetch a new access token.
     *
     * @param clientId the client id
     * @param refreshToken the refresh token
     * @return ServiceTicket, if successful
     */
    private ServiceTicket fetchAccessToken(final String clientId, final TicketGrantingTicket refreshToken) {
        final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(servicesManager, clientId);
        final Service service = new SimpleWebApplicationServiceImpl(registeredService.getServiceId());
        try {
            return centralAuthenticationService.grantServiceTicket(refreshToken.getId(), service);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verify the request by reviewing the values of client id, client secret, refresh token, etc.
     *
     * @param clientId the client id
     * @param clientSecret the client secret
     * @param refreshTokenId the refresh token id
     * @return true, if successful
     */
    private boolean verifyRequest(final String clientId, final String clientSecret, final String refreshTokenId) {
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
        // refreshToken is required
        if (StringUtils.isBlank(refreshTokenId)) {
            LOGGER.error("Missing {}", OAuthConstants.REFRESH_TOKEN);
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
        final Ticket refreshToken = ticketRegistry.getTicket(refreshTokenId);
        if (!(refreshToken instanceof TicketGrantingTicket)) {
            LOGGER.error("Invalid {} : {} for serviceId : {}", OAuthConstants.REFRESH_TOKEN, refreshTokenId, service.getServiceId());
            return false;
        }

        return true;
    }
}
