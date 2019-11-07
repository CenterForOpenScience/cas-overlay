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
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.AuthorizationCode;
import org.jasig.cas.support.oauth.token.InvalidTokenException;
import org.jasig.cas.support.oauth.token.RefreshToken;
import org.jasig.cas.support.oauth.token.TokenType;

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
 * The OAuth 2.0 token-code exchange controller.
 *
 * This controller handles requests that ask for exchanging an authorization code for an ONLINE access token or an
 * OFFLINE refresh token after the initial authorization process where "code" is used for the response type.
 *
 * ONLINE access token is granted via {@link CentralOAuthService#grantOnlineAccessToken} with a new ticket granting
 * ticket generated with the OAuth credentials.
 *
 * Similarly, the OFFLINE refresh token is granted via {@link CentralOAuthService#grantOfflineRefreshToken} with a new
 * ticket granting ticket generated with the OAuth credentials. In addition, an ONLINE access token is granted via
 * {@link CentralOAuthService#grantOfflineAccessToken} with a new service ticket generated from the ticket granting
 * ticket of the OFFLINE refresh token.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuth20TokenAuthorizationCodeController extends AbstractController {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20TokenAuthorizationCodeController.class);

    /** The CAS OAuth authorization service. */
    private final CentralOAuthService centralOAuthService;

    /** The ticket timeout. */
    private final Long timeout;

    /**
     * Instantiates a new {@link OAuth20TokenAuthorizationCodeController}.
     *
     * @param centralOAuthService the CAS OAuth service
     * @param timeout the ticket timeout
     */
    public OAuth20TokenAuthorizationCodeController(final CentralOAuthService centralOAuthService, final Long timeout) {
        this.centralOAuthService = centralOAuthService;
        this.timeout = timeout;
    }

    @Override
    protected ModelAndView handleRequestInternal(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        // Verify that all required OAuth parameters are provided.
        final String code = request.getParameter(OAuthConstants.CODE);
        LOGGER.debug("{} : {}", OAuthConstants.CODE, code);
        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_ID, clientId);
        final String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_SECRET, "*********");
        final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
        LOGGER.debug("{} : {}", OAuthConstants.REDIRECT_URI, redirectUri);
        final String grantType = request.getParameter(OAuthConstants.GRANT_TYPE);
        LOGGER.debug("{} : {}", OAuthConstants.GRANT_TYPE, grantType);
        try {
            verifyRequest(redirectUri, clientId, clientSecret, code, grantType);
        } catch (final InvalidParameterException e) {
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_REQUEST,
                    e.getMessage(),
                    HttpStatus.SC_BAD_REQUEST
            );
        }

        // Verify that the authorization code is valid.
        final AuthorizationCode authorizationCode;
        try {
            authorizationCode = centralOAuthService.getToken(code, AuthorizationCode.class);
        } catch (final InvalidTokenException e) {
            LOGGER.error("Unknown {} : {}", OAuthConstants.AUTHORIZATION_CODE, code);
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_REQUEST,
                    OAuthConstants.INVALID_CODE_DESCRIPTION,
                    HttpStatus.SC_BAD_REQUEST
            );
        }

        // Retrieve the registered service by client id.
        final OAuthRegisteredService service = centralOAuthService.getRegisteredService(clientId);
        if (service == null) {
            // Log the "unknown client id" error while return a general "invalid client id or secret" message.
            LOGGER.error("{} : {}", OAuthConstants.UNKNOWN_CLIENT_ID_DESCRIPTION, clientId);
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_REQUEST,
                    OAuthConstants.INVALID_CLIENT_ID_OR_SECRET_DESCRIPTION,
                    HttpStatus.SC_BAD_REQUEST
            );
        }

        // Verify the client secret.
        if (!service.getClientSecret().equals(clientSecret)) {
            // Log the "invalid client secret" error while return a general "invalid client id or secret" message.
            LOGGER.error(OAuthConstants.INVALID_CLIENT_SECRET_DESCRIPTION);
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_REQUEST,
                    OAuthConstants.INVALID_CLIENT_ID_OR_SECRET_DESCRIPTION,
                    HttpStatus.SC_BAD_REQUEST
            );
        }

        // Verify the redirect uri, which is stored as the `serviceId` property of an `OAuthRegisteredService` object.
        // It is a literal string instead of a regular expression. Thus the match is done by a case-insensitive string
        // equality check.
        if (!redirectUri.equalsIgnoreCase(service.getServiceId())) {
            LOGGER.error(
                    "{} : {} for serviceId : {}",
                    OAuthConstants.INVALID_REDIRECT_URI_DESCRIPTION,
                    redirectUri,
                    service.getServiceId()
            );
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_REQUEST,
                    OAuthConstants.INVALID_REDIRECT_URI_DESCRIPTION,
                    HttpStatus.SC_BAD_REQUEST
            );
        }

        // Grant the access and refresh token.
        final AccessToken accessToken;
        RefreshToken refreshToken = null;
        if (authorizationCode.getType() == TokenType.OFFLINE) {
            refreshToken = centralOAuthService
                    .grantOfflineRefreshToken(authorizationCode, redirectUri);
            accessToken = centralOAuthService.grantOfflineAccessToken(refreshToken);
        } else if (authorizationCode.getType() == TokenType.ONLINE) {
            accessToken = centralOAuthService.grantOnlineAccessToken(authorizationCode);
        } else {
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_GRANT,
                    OAuthConstants.INVALID_GRANT_TYPE_DESCRIPTION,
                    HttpStatus.SC_BAD_REQUEST
            );
        }

        // Build and return the response.
        final Map<String, Object> map = new HashMap<>();
        map.put(OAuthConstants.TOKEN_TYPE, OAuthConstants.BEARER_TOKEN);
        if (authorizationCode.getType() == TokenType.OFFLINE && refreshToken != null) {
            map.put(OAuthConstants.REFRESH_TOKEN, refreshToken.getId());
        }
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
     * @param redirectUri the redirect uri
     * @param clientId the client id
     * @param clientSecret the client secret
     * @param code the authorization code
     * @param grantType the grant type, which must be "authorization_code"
     * @throws InvalidParameterException with the name of the invalid parameter
     */
    private void verifyRequest(
            final String redirectUri,
            final String clientId,
            final String clientSecret,
            final String code,
            final String grantType
    ) throws InvalidParameterException {

        if (StringUtils.isBlank(clientId)) {
            LOGGER.error(OAuthConstants.MISSING_CLIENT_ID_DESCRIPTION);
            throw new InvalidParameterException(OAuthConstants.CLIENT_ID);
        }

        if (StringUtils.isBlank(clientSecret)) {
            LOGGER.error(OAuthConstants.MISSING_CLIENT_SECRET_DESCRIPTION);
            throw new InvalidParameterException(OAuthConstants.CLIENT_SECRET);
        }

        if (StringUtils.isBlank(code)) {
            LOGGER.error(OAuthConstants.MISSING_CODE_DESCRIPTION);
            throw new InvalidParameterException(OAuthConstants.CODE);
        }

        if (StringUtils.isBlank(redirectUri)) {
            LOGGER.error(OAuthConstants.MISSING_REDIRECT_URI_DESCRIPTION);
            throw new InvalidParameterException(OAuthConstants.REDIRECT_URI);
        }

        if (StringUtils.isBlank(grantType)) {
            LOGGER.error(OAuthConstants.MISSING_GRANT_TYPE_DESCRIPTION);
            throw new InvalidParameterException(OAuthConstants.GRANT_TYPE);
        } else if (!grantType.equalsIgnoreCase(OAuthConstants.AUTHORIZATION_CODE)) {
            LOGGER.error("{} : {}", OAuthConstants.INVALID_GRANT_TYPE_DESCRIPTION, grantType);
            throw new InvalidParameterException(OAuthConstants.GRANT_TYPE);
        }
    }
}
