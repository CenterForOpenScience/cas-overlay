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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.InvalidParameterException;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.RefreshToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The OAuth 2.0 "Revoke Client Tokens" controller.
 *
 * This controller handles requests to revoke all tokens of the registered service associated with a given client id.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuth20RevokeClientTokensController extends AbstractController {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20RevokeClientTokensController.class);

    /** The CAS OAuth authorization service. */
    private final CentralOAuthService centralOAuthService;

    /**
     * Instantiates a new {@link OAuth20RevokeClientTokensController}.
     *
     * @param centralOAuthService the CAS OAuth service
     */
    public OAuth20RevokeClientTokensController(final CentralOAuthService centralOAuthService) {
        this.centralOAuthService = centralOAuthService;
    }

    @Override
    protected ModelAndView handleRequestInternal(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        // Verify that all required OAuth 2.0 parameters are provided.
        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_ID, clientId);
        final String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_SECRET, "************");
        try {
            verifyRequest(clientId, clientSecret);
        } catch (final InvalidParameterException e) {
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_REQUEST,
                    e.getMessage(),
                    HttpStatus.SC_BAD_REQUEST
            );
        }

        // Retrieve the registered service associated with the client id and verify the client secret.
        final OAuthRegisteredService service = centralOAuthService.getRegisteredService(clientId);
        if (service == null || !service.getClientSecret().equals(clientSecret)) {
            LOGGER.error("Could not revoke client tokens, mismatched client id or client secret");
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_REQUEST,
                    OAuthConstants.INVALID_CLIENT_ID_OR_SECRET_DESCRIPTION,
                    HttpStatus.SC_BAD_REQUEST
            );
        }

        // Attempt to remove all refresh and access tokens associated with the registered service via client id.
        final Collection<RefreshToken> refreshTokens = centralOAuthService.getClientRefreshTokens(clientId);
        for (final RefreshToken token: refreshTokens) {
            LOGGER.debug("Revoking refresh token : {}", token.getId());
            centralOAuthService.revokeToken(token);
        }
        final Collection<AccessToken> accessTokens = centralOAuthService.getClientAccessTokens(clientId);
        for (final AccessToken token: accessTokens) {
            LOGGER.info("Revoking access token : {}", token.getId());
            centralOAuthService.revokeToken(token);
        }

        // Return an HTTP 204 No Content after all tokens have been removed successfully.
        return OAuthUtils.writeText(response, null, HttpStatus.SC_NO_CONTENT);
    }

    /**
     * Verify that all required OAuth 2.0 parameters are provided.
     *
     * @param clientId the client id
     * @param clientSecret the client secret
     * @throws InvalidParameterException with the name of the invalid parameter
     */
    private void verifyRequest(final String clientId, final String clientSecret) throws InvalidParameterException {

        if (StringUtils.isBlank(clientId)) {
            LOGGER.error(OAuthConstants.MISSING_CLIENT_ID_DESCRIPTION);
            throw new InvalidParameterException(OAuthConstants.CLIENT_ID);
        }

        if (StringUtils.isBlank(clientSecret)) {
            LOGGER.error(OAuthConstants.MISSING_CLIENT_SECRET_DESCRIPTION);
            throw new InvalidParameterException(OAuthConstants.CLIENT_SECRET);
        }
    }
}
