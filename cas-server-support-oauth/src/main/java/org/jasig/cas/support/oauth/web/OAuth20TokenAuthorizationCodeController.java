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
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.RefreshToken;
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

    private final long timeout;

    /**
     * Instantiates a new o auth20 grant type authorization code controller.
     *
     * @param centralOAuthService the central oauth service
     * @param timeout the timeout
     */
    public OAuth20TokenAuthorizationCodeController(final CentralOAuthService centralOAuthService,
                                                   final long timeout) {
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

        if (!verifyRequest(redirectUri, clientId, clientSecret, code)) {
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_BAD_REQUEST);
        }

        final RefreshToken refreshToken = centralOAuthService.grantRefreshToken(code, clientId, clientSecret, redirectUri);
        final AccessToken accessToken = centralOAuthService.grantAccessToken(refreshToken);

        final int expires = (int) (timeout - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - accessToken.getServiceTicket().getCreationTime()));

        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, Object> map = new HashMap<>();
        map.put(OAuthConstants.ACCESS_TOKEN, accessToken.getId());
        map.put(OAuthConstants.REFRESH_TOKEN, refreshToken.getId());
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

        return true;
    }
}
