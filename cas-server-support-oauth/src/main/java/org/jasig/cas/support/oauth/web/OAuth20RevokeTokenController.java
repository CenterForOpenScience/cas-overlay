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
import org.jasig.cas.support.oauth.InvalidParameterException;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.token.InvalidTokenException;
import org.jasig.cas.support.oauth.token.Token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The OAuth 2.0 "Revoke Token" controller.
 *
 * This controller handles requests to revoke one access or refresh token.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuth20RevokeTokenController extends AbstractController {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20RevokeTokenController.class);

    /** The CAS OAuth authorization service. */
    private final CentralOAuthService centralOAuthService;

    /**
     * Instantiates a {@link OAuth20RevokeTokenController}.
     *
     * @param centralOAuthService the CAS OAuth service
     */
    public OAuth20RevokeTokenController(final CentralOAuthService centralOAuthService) {
        this.centralOAuthService = centralOAuthService;
    }

    @Override
    protected ModelAndView handleRequestInternal(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        // Verify that all required OAuth 2.0 parameters are provided.
        final String tokenId = request.getParameter(OAuthConstants.TOKEN);
        LOGGER.debug("{} : {}", OAuthConstants.TOKEN, tokenId);
        try {
            verifyRequest(tokenId);
        } catch (final InvalidParameterException e) {
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_REQUEST,
                    e.getMessage(),
                    HttpStatus.SC_BAD_REQUEST
            );
        }

        // Verify that the token is valid.
        final Token token;
        try {
            token = centralOAuthService.getToken(tokenId);
        } catch (final InvalidTokenException e) {
            LOGGER.error("{} : {}", OAuthConstants.INVALID_TOKEN_DESCRIPTION, tokenId);
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_REQUEST,
                    e.getMessage(),
                    HttpStatus.SC_BAD_REQUEST
            );
        }

        // Attempt to revoke the token, return an HTTP 204 No Content if successful or an HTTP 400 otherwise.
        if (!centralOAuthService.revokeToken(token)) {
            LOGGER.error("Token revocation failed [{}]", token.getId());
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_REQUEST,
                    OAuthConstants.FAILED_TOKEN_REVOCATION_DESCRIPTION,
                    HttpStatus.SC_BAD_REQUEST
            );
        }
        return OAuthUtils.writeText(response, null, HttpStatus.SC_NO_CONTENT);
    }

    /**
     * Verify that all required OAuth 2.0 parameters are provided.
     *
     * @param tokenId the token id
     * @throws InvalidParameterException with the name of the invalid parameter
     */
    private void verifyRequest(final String tokenId) throws InvalidParameterException {

        if (StringUtils.isBlank(tokenId)) {
            LOGGER.error(OAuthConstants.MISSING_TOKEN_DESCRIPTION);
            throw new InvalidParameterException(OAuthConstants.TOKEN);
        }
    }
}
