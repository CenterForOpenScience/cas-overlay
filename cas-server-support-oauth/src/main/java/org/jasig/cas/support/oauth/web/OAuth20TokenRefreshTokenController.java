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
import org.jasig.cas.support.oauth.*;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.RefreshToken;
import org.jasig.cas.ticket.InvalidTicketException;
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
public final class OAuth20TokenRefreshTokenController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20TokenRefreshTokenController.class);

    private final CentralOAuthService centralOAuthService;

    private final long timeout;

    /**
     * Instantiates a new o auth20 grant type refresh token controller.
     *
     * @param centralOAuthService the central oauth service
     * @param timeout the timeout
     */
    public OAuth20TokenRefreshTokenController(final CentralOAuthService centralOAuthService,
                                              final long timeout) {
        this.centralOAuthService = centralOAuthService;
        this.timeout = timeout;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String refreshTokenId = request.getParameter(OAuthConstants.REFRESH_TOKEN);
        LOGGER.debug("{} : {}", OAuthConstants.REFRESH_TOKEN, refreshTokenId);

        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_ID, clientId);

        final String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_SECRET, "*********");

        final String grantType = request.getParameter(OAuthConstants.GRANT_TYPE);
        LOGGER.debug("{} : {}", OAuthConstants.GRANT_TYPE, grantType);

        final boolean isVerified = verifyRequest(refreshTokenId, clientId, clientSecret, grantType);
        if (!isVerified) {
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_BAD_REQUEST);
        }

        final RefreshToken refreshToken;
        try {
            refreshToken = centralOAuthService.getToken(refreshTokenId, RefreshToken.class);
        } catch (InvalidTicketException e) {
            LOGGER.error("Invalid {} : {}", OAuthConstants.REFRESH_TOKEN, refreshTokenId);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_BAD_REQUEST);
        }

        final AccessToken accessToken = centralOAuthService.grantOfflineAccessToken(refreshToken);

        final Map<String, Object> map = new HashMap<>();
        map.put(OAuthConstants.ACCESS_TOKEN, accessToken.getId());
        map.put(OAuthConstants.EXPIRES_IN, (int) (timeout - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - accessToken.getTicket().getCreationTime())));
        map.put(OAuthConstants.TOKEN_TYPE, OAuthConstants.BEARER_TOKEN);

        final ObjectMapper mapper = new ObjectMapper();
        final String result = mapper.writeValueAsString(map);
        LOGGER.debug("result : {}", result);

        response.setContentType("application/json; charset=UTF-8");
        return OAuthUtils.writeText(response, result, HttpStatus.SC_OK);
    }

    /**
     * Verify the request by reviewing the values of client id, client secret, refresh token, etc.
     *
     * @param refreshTokenId the refresh token id
     * @param clientId the client id
     * @param clientSecret the client secret
     * @param grantType the grant type
     * @return true, if successful
     */
    private boolean verifyRequest(final String refreshTokenId, final String clientId, final String clientSecret, final String grantType) {
        // refreshToken is required
        if (StringUtils.isBlank(refreshTokenId)) {
            LOGGER.error("Missing {}", OAuthConstants.REFRESH_TOKEN);
            return false;
        }
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
        // grantType is required
        if (StringUtils.isBlank(grantType)) {
            LOGGER.error("Missing {}", OAuthConstants.GRANT_TYPE);
            return false;
        }
        if (!grantType.equalsIgnoreCase(OAuthConstants.REFRESH_TOKEN)) {
            LOGGER.error("Invalid {} : {}", OAuthConstants.GRANT_TYPE, grantType);
            return false;
        }

        return true;
    }
}
