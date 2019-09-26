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
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.InvalidTokenException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The OAuth 2.0 revoke client-principal tokens controller.
 *
 * This controller handles requests that revoke all of a given user's tokens for a registered service. The service is
 * identified by the client id and the user by the principal id.
 *
 * As mentioned in {@link CentralOAuthService#revokeClientPrincipalTokens}, currently this request does not support
 * PERSONAL access tokens. For CAS access tokens, any client id can be used. However, for OFFLINE and ONLINE access
 * tokens, only the client id with which the token is associated works.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuth20RevokeClientPrincipalTokensController extends AbstractController {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20RevokeClientPrincipalTokensController.class);

    /** The CAS oauth authorization service. */
    private final CentralOAuthService centralOAuthService;

    /**
     * Instantiates a new {@link OAuth20RevokeClientPrincipalTokensController}.
     *
     * @param centralOAuthService the CAS OAuth service
     */
    public OAuth20RevokeClientPrincipalTokensController(final CentralOAuthService centralOAuthService) {
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
        final String prefixedBearerToken = request.getHeader(OAuthConstants.AUTHORIZATION_HEADER);
        LOGGER.debug("{} : {}", OAuthConstants.BEARER_TOKEN, prefixedBearerToken);
        String accessTokenId = request.getParameter(OAuthConstants.ACCESS_TOKEN);
        LOGGER.debug("{} : {}", OAuthConstants.ACCESS_TOKEN, accessTokenId);
        try {
            accessTokenId = verifyRequest(clientId, accessTokenId, prefixedBearerToken);
        } catch (final InvalidParameterException e) {
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_REQUEST,
                    e.getMessage(),
                    HttpStatus.SC_BAD_REQUEST
            );
        }

        // Retrieve the access token from the token registry via the CAS OAuth service.
        final AccessToken accessToken;
        try {
            accessToken = centralOAuthService.getToken(accessTokenId, AccessToken.class);
        } catch (final InvalidTokenException e) {
            LOGGER.error("Could not get Access Token [{}]", accessTokenId);
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.UNAUTHORIZED_REQUEST,
                    OAuthConstants.INVALID_ACCESS_TOKEN_DESCRIPTION,
                    HttpStatus.SC_UNAUTHORIZED
            );
        }

        // Attempt to revoke all of the principal's tokens for the registered service associated with the client id.
        // Return an HTTP 204 No Content response upon success or HTTP 400 Bad Request upon failure.
        if (!centralOAuthService.revokeClientPrincipalTokens(accessToken, clientId)) {
            LOGGER.error("Could not revoke client principal tokens");
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_REQUEST,
                    OAuthConstants.INVALID_ACCESS_TOKEN_DESCRIPTION,
                    HttpStatus.SC_BAD_REQUEST
            );
        }
        return OAuthUtils.writeText(response, null, HttpStatus.SC_NO_CONTENT);
    }

    /**
     * Verify that all required OAuth 2.0 parameters are provided.
     *
     * @param clientId the client id
     * @param accessTokenId the access token id
     * @param prefixedBearerToken the prefixed bearer token provided by the HTTP Authorization header
     * @return the access token id
     * @throws InvalidParameterException with the name of the invalid parameter
     */
    private String verifyRequest(
            final String clientId,
            final String accessTokenId,
            final String prefixedBearerToken
    ) throws InvalidParameterException {

        if (StringUtils.isBlank(clientId)) {
            LOGGER.error(OAuthConstants.MISSING_CLIENT_ID_DESCRIPTION);
            throw new InvalidParameterException(OAuthConstants.CLIENT_ID);
        }

        // An access token must be provided via either the request body or the "Authorization" header.
        if (StringUtils.isBlank(accessTokenId)) {
            if (StringUtils.isNotBlank(prefixedBearerToken)
                    && prefixedBearerToken.startsWith(OAuthConstants.BEARER_TOKEN + " ")
            ) {
                return prefixedBearerToken.substring(OAuthConstants.BEARER_TOKEN.length() + 1);
            } else {
                LOGGER.debug(OAuthConstants.MISSING_ACCESS_TOKEN_DESCRIPTION);
                throw new InvalidParameterException(OAuthConstants.ACCESS_TOKEN);
            }
        } else {
            return accessTokenId;
        }
    }
}
