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
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.*;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.util.CipherExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This controller handles requests for grant type refresh token,
 * returning an access token which is the CAS service ticket according
 * to the service and refresh token (granting ticket) given.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public final class OAuth20RefreshTokenController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20RefreshTokenController.class);

    private final ServicesManager servicesManager;

    private final CentralAuthenticationService centralAuthenticationService;

    private final CipherExecutor cipherExecutor;

    private final long timeout;

    /**
     * Instantiates a new o auth20 grant type refresh token controller.
     *
     * @param servicesManager the services manager
     * @param centralAuthenticationService the central authentication service
     * @param cipherExecutor the cipher executor
     * @param timeout the timeout
     */
    public OAuth20RefreshTokenController(final ServicesManager servicesManager,
                                         final CentralAuthenticationService centralAuthenticationService,
                                         final CipherExecutor cipherExecutor, final long timeout) {
        this.servicesManager = servicesManager;
        this.centralAuthenticationService = centralAuthenticationService;
        this.cipherExecutor = cipherExecutor;
        this.timeout = timeout;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_ID, clientId);

        final String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);

        final boolean isVerified = verifyRequest(clientId, clientSecret);
        if (!isVerified) {
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_BAD_REQUEST);
        }

        final OAuthToken refreshToken = OAuthTokenUtils.getToken(request, cipherExecutor, OAuthConstants.REFRESH_TOKEN);
        final TicketGrantingTicket refreshTicket = (TicketGrantingTicket) OAuthTokenUtils.getTicket(centralAuthenticationService, refreshToken);

        final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(servicesManager, clientId);
        if (registeredService == null) {
            LOGGER.error("Could not find registered service for client id : {}", clientId);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT, HttpStatus.SC_BAD_REQUEST);
        }

        final Service service = new SimpleWebApplicationServiceImpl(registeredService.getServiceId());
        final ServiceTicket accessTicket = OAuthTokenUtils.fetchAccessTicket(centralAuthenticationService, refreshTicket, service);

        final int expires = (int) (timeout - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - accessTicket.getCreationTime()));

        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, Object> map = new HashMap<>();
        map.put(OAuthConstants.ACCESS_TOKEN, OAuthTokenUtils.getJsonWebToken(cipherExecutor, accessTicket));
        map.put(OAuthConstants.EXPIRES_IN, expires);
        map.put(OAuthConstants.TOKEN_TYPE, OAuthConstants.BEARER_TOKEN);

        final String result = mapper.writeValueAsString(map);
        LOGGER.debug("result : {}", result);

        response.setContentType("application/json; charset=UTF-8");
        return OAuthUtils.writeText(response, result, HttpStatus.SC_OK);
    }

    /**
     * Verify the request by reviewing the values of client id, client secret, refresh token, etc.
     *
     * @param clientId the client id
     * @param clientSecret the client secret
     * @return true, if successful
     */
    private boolean verifyRequest(final String clientId, final String clientSecret) {
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

        final OAuthRegisteredService service = OAuthUtils.getRegisteredOAuthService(servicesManager, clientId);
        if (service == null) {
            LOGGER.error("Unknown {} : {}", OAuthConstants.CLIENT_ID, clientId);
            return false;
        }
        if (!StringUtils.equals(service.getClientSecret(), clientSecret)) {
            LOGGER.error("Wrong client secret for service {}", service);
            return false;
        }

        return true;
    }
}
