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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    private final CentralOAuthService centralOAuthService;

    private final Long timeout;

    /**
     * Instantiates a new o auth20 grant type authorization code controller.
     *
     * @param centralOAuthService the central oauth service
     * @param timeout the timeout
     */
    public OAuth20TokenAuthorizationCodeController(final CentralOAuthService centralOAuthService, final Long timeout) {
        this.centralOAuthService = centralOAuthService;
        this.timeout = timeout;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String code = request.getParameter(OAuthConstants.CODE);
        LOGGER.debug("{} : {}", OAuthConstants.CODE, code);

        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_ID, clientId);

        final String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_SECRET, "*********");

        final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
        LOGGER.debug("{} : {}", OAuthConstants.REDIRECT_URI, redirectUri);

        try {
            verifyRequest(redirectUri, clientId, clientSecret, code);
        } catch (final InvalidParameterException e) {
            return OAuthUtils.writeJsonError(response, OAuthConstants.INVALID_REQUEST, e.getMessage(), HttpStatus.SC_BAD_REQUEST);
        }

        final AuthorizationCode authorizationCode;
        try {
            authorizationCode = centralOAuthService.getToken(code, AuthorizationCode.class);
        } catch (final InvalidTokenException e) {
            LOGGER.error("Unknown {} : {}", OAuthConstants.AUTHORIZATION_CODE, code);
            return OAuthUtils.writeJsonError(response, OAuthConstants.INVALID_REQUEST, "Invalid Code", HttpStatus.SC_BAD_REQUEST);
        }

        final OAuthRegisteredService service = centralOAuthService.getRegisteredService(clientId);
        if (service == null) {
            LOGGER.error("Unknown {} : {}", OAuthConstants.CLIENT_ID, clientId);
            return OAuthUtils.writeJsonError(response, OAuthConstants.INVALID_REQUEST, "Invalid Client ID or Client Secret", HttpStatus.SC_BAD_REQUEST);
        }
        if (!service.getClientSecret().equals(clientSecret)) {
            LOGGER.error("Mismatched Client Secret parameters");
            return OAuthUtils.writeJsonError(response, OAuthConstants.INVALID_REQUEST, "Invalid Client ID or Client Secret", HttpStatus.SC_BAD_REQUEST);
        }
        if (!redirectUri.matches(service.getServiceId())) {
            LOGGER.error("Unsupported {} : {} for serviceId : {}", OAuthConstants.REDIRECT_URI, redirectUri, service.getServiceId());
            return OAuthUtils.writeJsonError(response, OAuthConstants.INVALID_REQUEST, "Invalid Redirect URI", HttpStatus.SC_BAD_REQUEST);
        }

        final Map<String, Object> map = new HashMap<>();

        final AccessToken accessToken;
        if (authorizationCode.getType() == TokenType.OFFLINE) {
            final RefreshToken refreshToken = centralOAuthService.grantOfflineRefreshToken(authorizationCode, redirectUri);
            map.put(OAuthConstants.REFRESH_TOKEN, refreshToken.getId());

            accessToken = centralOAuthService.grantOfflineAccessToken(refreshToken);
        } else if (authorizationCode.getType() == TokenType.ONLINE) {
            accessToken = centralOAuthService.grantOnlineAccessToken(authorizationCode);
        } else {
            return OAuthUtils.writeJsonError(response, OAuthConstants.INVALID_GRANT, "Invalid Grant Type", HttpStatus.SC_BAD_REQUEST);
        }

        map.put(OAuthConstants.ACCESS_TOKEN, accessToken.getId());
        map.put(OAuthConstants.EXPIRES_IN,
                (int) (timeout - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - accessToken.getTicket().getCreationTime())));
        map.put(OAuthConstants.TOKEN_TYPE, OAuthConstants.BEARER_TOKEN);

        final ObjectMapper mapper = new ObjectMapper();
        final String result = mapper.writeValueAsString(map);
        LOGGER.debug("result : {}", result);

        response.setContentType("application/json");
        return OAuthUtils.writeText(response, result, HttpStatus.SC_OK);
    }

    /**
     * Verify the request by reviewing the values of client id, redirect uri, client secret, code, etc.
     *
     * @param redirectUri the redirect uri
     * @param clientId the client id
     * @param clientSecret the client secret
     * @param code the code
     * @throws InvalidParameterException with the name of the invalid parameter
     */
    private void verifyRequest(final String redirectUri, final String clientId, final String clientSecret,
                                  final String code) throws InvalidParameterException {
        // clientId is required
        if (StringUtils.isBlank(clientId)) {
            LOGGER.error("Missing {}", OAuthConstants.CLIENT_ID);
            throw new InvalidParameterException(OAuthConstants.CLIENT_ID);
        }
        // clientSecret is required
        if (StringUtils.isBlank(clientSecret)) {
            LOGGER.error("Missing {}", OAuthConstants.CLIENT_SECRET);
            throw new InvalidParameterException(OAuthConstants.CLIENT_SECRET);
        }
        // code is required
        if (StringUtils.isBlank(code)) {
            LOGGER.error("Missing {}", OAuthConstants.CODE);
            throw new InvalidParameterException(OAuthConstants.CODE);
        }
        // redirectUri is required
        if (StringUtils.isBlank(redirectUri)) {
            LOGGER.error("Missing {}", OAuthConstants.REDIRECT_URI);
            throw new InvalidParameterException(OAuthConstants.REDIRECT_URI);
        }
    }
}
