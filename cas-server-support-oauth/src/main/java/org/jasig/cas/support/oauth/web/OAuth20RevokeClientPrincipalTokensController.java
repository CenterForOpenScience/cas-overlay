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
import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.InvalidTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This controller handles requests revoke all tokens associated with a client id and principal.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public final class OAuth20RevokeClientPrincipalTokensController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20RevokeClientPrincipalTokensController.class);

    private final CentralOAuthService centralOAuthService;

    /**
     * Instantiates a new o auth20 revoke client principal tokens controller.
     *
     * @param centralOAuthService the central oauth service
     */
    public OAuth20RevokeClientPrincipalTokensController(final CentralOAuthService centralOAuthService) {
        this.centralOAuthService = centralOAuthService;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        String accessTokenId = request.getParameter(OAuthConstants.ACCESS_TOKEN);
        if (StringUtils.isBlank(accessTokenId)) {
            final String authHeader = request.getHeader("Authorization");
            if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith(OAuthConstants.BEARER_TOKEN + " ")) {
                accessTokenId = authHeader.substring(OAuthConstants.BEARER_TOKEN.length() + 1);
            } else {
                LOGGER.debug("Missing Access Token");
                return OAuthUtils.writeJsonError(response, OAuthConstants.INVALID_REQUEST, OAuthConstants.MISSING_ACCESS_TOKEN_DESCRIPTION,
                        HttpStatus.SC_BAD_REQUEST);
            }
        }

        final AccessToken accessToken;
        try {
            accessToken = centralOAuthService.getToken(accessTokenId, AccessToken.class);
        } catch (final InvalidTokenException e) {
            LOGGER.error("Could not get Access Token [{}]", accessTokenId);
            return OAuthUtils.writeJsonError(response, OAuthConstants.UNAUTHORIZED_REQUEST, OAuthConstants.INVALID_ACCESS_TOKEN_DESCRIPTION,
                    HttpStatus.SC_UNAUTHORIZED);
        }

        if (!centralOAuthService.revokeClientPrincipalTokens(accessToken)) {
            LOGGER.error("Could not revoke client principal tokens");
            return OAuthUtils.writeJsonError(response, OAuthConstants.INVALID_REQUEST, OAuthConstants.INVALID_ACCESS_TOKEN_DESCRIPTION,
                    HttpStatus.SC_BAD_REQUEST);
        }

        return OAuthUtils.writeText(response, null, HttpStatus.SC_NO_CONTENT);
    }
}
