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
package org.jasig.cas.support.oauth.web;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.InvalidParameterException;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.InvalidTokenException;
import org.jasig.cas.support.oauth.token.RefreshToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The OAuth 2.0 refresh access token controller.
 *
 * This controller handles requests asking for a new access token with "refresh_token" as the grant type. A valid
 * refresh token must be provided. The new token is granted by {@link CentralOAuthService#grantOfflineAccessToken}
 * using a new service ticket created with the service and ticket granting ticket of the refresh token.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuth20TokenRefreshTokenController extends AbstractController {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20TokenRefreshTokenController.class);

    /** The CAS OAuth authorization service. */
    private final CentralOAuthService centralOAuthService;

    /** The ticket timeout. */
    private final long timeout;

    /**
     * Instantiates a new {@link OAuth20TokenRefreshTokenController}.
     *
     * @param centralOAuthService the central OAuth service
     * @param timeout ticket timeout
     */
    public OAuth20TokenRefreshTokenController(
            final CentralOAuthService centralOAuthService,
            final long timeout
    ) {
        this.centralOAuthService = centralOAuthService;
        this.timeout = timeout;
    }

    @Override
    protected ModelAndView handleRequestInternal(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        // Verify that all required OAuth parameters are provided.
        final String refreshTokenId = request.getParameter(OAuthConstants.REFRESH_TOKEN);
        LOGGER.debug("{} : {}", OAuthConstants.REFRESH_TOKEN, refreshTokenId);
        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_ID, clientId);
        final String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_SECRET, "*********");
        final String grantType = request.getParameter(OAuthConstants.GRANT_TYPE);
        LOGGER.debug("{} : {}", OAuthConstants.GRANT_TYPE, grantType);
        try {
            verifyRequest(refreshTokenId, clientId, clientSecret, grantType);
        } catch (final InvalidParameterException e) {
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_REQUEST,
                    e.getMessage(),
                    HttpStatus.SC_BAD_REQUEST
            );
        }

        // Verify that the refresh token is valid.
        final RefreshToken refreshToken;
        try {
            refreshToken = centralOAuthService.getToken(refreshTokenId, RefreshToken.class);
        } catch (final InvalidTokenException e) {
            LOGGER.error("Invalid {} : {}", OAuthConstants.REFRESH_TOKEN, refreshTokenId);
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_REQUEST,
                    OAuthConstants.INVALID_REFRESH_TOKEN_DESCRIPTION,
                    HttpStatus.SC_BAD_REQUEST
            );
        }

        // Grant an OFFLINE access token with the given refresh token.
        final AccessToken accessToken = centralOAuthService.grantOfflineAccessToken(refreshToken);

        // Build and return the response.
        final Map<String, Object> map = new HashMap<>();
        map.put(OAuthConstants.TOKEN_TYPE, OAuthConstants.BEARER_TOKEN);
        map.put(OAuthConstants.ACCESS_TOKEN, accessToken.getId());
        final long timeSinceTicketCreation = System.currentTimeMillis() - accessToken.getTicket().getCreationTime();
        final int expiresIn = (int) (timeout - TimeUnit.MILLISECONDS.toSeconds(timeSinceTicketCreation));
        map.put(OAuthConstants.EXPIRES_IN, expiresIn);
        final ObjectMapper mapper = new ObjectMapper();
        final String result = mapper.writeValueAsString(map);
        LOGGER.debug("result : {}", result);
        response.setContentType("application/json");
        return OAuthUtils.writeText(response, result, HttpStatus.SC_OK);
    }

    /**
     * Verify the request by reviewing the values of OAuth 2.0 parameters.
     *
     * @param refreshTokenId the refresh token id
     * @param clientId the client id
     * @param clientSecret the client secret
     * @param grantType the grant type
     * @throws InvalidParameterException with the name of the invalid parameter
     */
    private void verifyRequest(
            final String refreshTokenId,
            final String clientId,
            final String clientSecret,
            final String grantType
    ) throws InvalidParameterException {

        if (StringUtils.isBlank(refreshTokenId)) {
            LOGGER.error(OAuthConstants.MISSING_REFRESH_TOKEN_DESCRIPTION);
            throw new InvalidParameterException(OAuthConstants.REFRESH_TOKEN);
        }

        if (StringUtils.isBlank(clientId)) {
            LOGGER.error(OAuthConstants.MISSING_CLIENT_ID_DESCRIPTION);
            throw new InvalidParameterException(OAuthConstants.CLIENT_ID);
        }

        if (StringUtils.isBlank(clientSecret)) {
            LOGGER.error(OAuthConstants.MISSING_CLIENT_SECRET_DESCRIPTION);
            throw new InvalidParameterException(OAuthConstants.CLIENT_SECRET);
        }

        if (StringUtils.isBlank(grantType)) {
            LOGGER.error(OAuthConstants.MISSING_GRANT_TYPE_DESCRIPTION);
            throw new InvalidParameterException(OAuthConstants.GRANT_TYPE);
        } else if (!grantType.equalsIgnoreCase(OAuthConstants.REFRESH_TOKEN)) {
            LOGGER.error("{} : {}", OAuthConstants.INVALID_GRANT_TYPE_DESCRIPTION, grantType);
            throw new InvalidParameterException(OAuthConstants.GRANT_TYPE);
        }
    }
}
