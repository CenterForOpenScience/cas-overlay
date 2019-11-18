/*
 * Copyright (c) 2015. Center for Open Science
 *
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

import org.apache.http.HttpStatus;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.RootCasException;
import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.InvalidParameterException;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
/**
 * The OAuth 2.0 wrapper controller.
 *
 * This controller is the main entry point for OAuth 2.0 wrapping in CAS. With current CAS settings, it is mapped to
 * {@literal /oauth2/*} with {@literal <cas domain>/login} as the login URL. Requests are dispatched to specific
 * controllers based on {@literal /*}.
 *
 * @author Jerome Leleu
 * @author Michael Haselton
 * @author Longze Chen
 * @since 3.5.0
 */
public final class OAuth20WrapperController extends BaseOAuthWrapperController implements InitializingBean {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20WrapperController.class);

    /** Authorization controllers. */
    private AbstractController authorizeController;
    private AbstractController authorizeCallbackController;
    private AbstractController authorizeCallbackActionController;

    /** Token controllers. */
    private AbstractController tokenAuthorizationCodeController;
    private AbstractController tokenRefreshTokenController;

    /** Revoke controllers. */
    private AbstractController revokeTokenController;
    private AbstractController revokeClientPrincipalTokensController;
    private AbstractController revokeClientTokensController;

    /** Profile controllers. */
    private AbstractController profileController;

    /** Metadata controllers. */
    private AbstractController metadataPrincipalController;
    private AbstractController metadataClientController;

    /** The CAS primary authentication service. */
    @NotNull
    private CentralAuthenticationService centralAuthenticationService;

    /** The CAS OAuth authorization service. */
    @NotNull
    private CentralOAuthService centralOAuthService;

    @Override
    public void afterPropertiesSet() throws Exception {

        authorizeController = new OAuth20AuthorizeController(centralOAuthService, loginUrl);
        authorizeCallbackController = new OAuth20AuthorizeCallbackController(centralOAuthService, ticketRegistry);
        authorizeCallbackActionController = new OAuth20AuthorizeCallbackActionController(centralOAuthService, timeout);

        tokenAuthorizationCodeController = new OAuth20TokenAuthorizationCodeController(centralOAuthService, timeout);
        tokenRefreshTokenController = new OAuth20TokenRefreshTokenController(centralOAuthService, timeout);

        revokeTokenController = new OAuth20RevokeTokenController(centralOAuthService);
        revokeClientTokensController = new OAuth20RevokeClientTokensController(centralOAuthService);
        revokeClientPrincipalTokensController = new OAuth20RevokeClientPrincipalTokensController(centralOAuthService);

        profileController = new OAuth20ProfileController(centralOAuthService, centralAuthenticationService);

        metadataPrincipalController = new OAuth20MetadataPrincipalController(centralOAuthService);
        metadataClientController = new OAuth20MetadataClientController(centralOAuthService);
    }

    @Override
    public ModelAndView handleRequest(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        try {
            return super.handleRequest(request, response);
        } catch (final RootCasException e) {
            // Capture any root CAS exceptions and display them properly to the user.
            final Map<String, Object> map = new HashMap<>();
            map.put("rootCauseException", e);
            return new ModelAndView(OAuthConstants.ERROR_VIEW, map);
        }
    }

    @Override
    protected ModelAndView internalHandleRequest(
            final String method,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        // Authorize
        if (OAuthConstants.AUTHORIZE_URL.equals(method) && "GET".equals(request.getMethod())) {
            return authorizeController.handleRequest(request, response);
        }
        // Authorize Callback
        if (OAuthConstants.CALLBACK_AUTHORIZE_URL.equals(method) && "GET".equals(request.getMethod())) {
            return authorizeCallbackController.handleRequest(request, response);
        }
        // Authorize Callback Action
        if (OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL.equals(method) && "GET".equals(request.getMethod())) {
            return authorizeCallbackActionController.handleRequest(request, response);
        }

        // Token (2 controllers)
        if (OAuthConstants.TOKEN_URL.equals(method) && "POST".equals(request.getMethod())) {
            final String grantType = request.getParameter(OAuthConstants.GRANT_TYPE);
            LOGGER.debug("{} : {}", OAuthConstants.GRANT_TYPE, grantType);
            if (grantType != null) {
                if (grantType.equals(OAuthConstants.AUTHORIZATION_CODE)) {
                    // Token 1: Exchange authorization code for ONLINE access token and OFFLINE refresh token
                    return tokenAuthorizationCodeController.handleRequest(request, response);
                } else if (grantType.equals(OAuthConstants.REFRESH_TOKEN)) {
                    // Token 2: Refresh access token using refresh token
                    return tokenRefreshTokenController.handleRequest(request, response);
                }
            }
            // Missing or invalid grant type
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_REQUEST,
                    new InvalidParameterException(OAuthConstants.GRANT_TYPE).getMessage(),
                    HttpStatus.SC_BAD_REQUEST);
        }

        // Revoke (3 controllers)
        if (OAuthConstants.REVOKE_URL.equals(method) && "POST".equals(request.getMethod())) {
            if (request.getParameterMap().containsKey(OAuthConstants.CLIENT_ID)) {
                // Revoke 1: Revoke all client tokens of a client if both client id and secret are provided
                if (request.getParameterMap().containsKey(OAuthConstants.CLIENT_SECRET)) {
                    return revokeClientTokensController.handleRequest(request, response);
                }
                // Revoke 2: Revoke all tokens of a given client for a given principal if the client id is provided
                // without a client secret. In addition, a valid access token of type CAS, ONLINE or OFFLINE must be
                // presented for authorization. If of type CAS, any client id can be specified; if ONLINE or OFFLINE,
                // only the client id associated with the access token will work.
                return revokeClientPrincipalTokensController.handleRequest(request, response);
            } else {
                // Revoke 3: Revoke a token if neither the client id nor secret is provided. A token must be presented.
                return revokeTokenController.handleRequest(request, response);
            }
        }

        // Profile: Retrieve the profile (i.e. identifier and attributes) of a user associated with an access token.
        if (OAuthConstants.PROFILE_URL.equals(method) && "GET".equals(request.getMethod())) {
            return profileController.handleRequest(request, response);
        }

        // Metadata (2 controllers)
        if (OAuthConstants.METADATA_URL.equals(method) && "POST".equals(request.getMethod())) {
            if (request.getParameterMap().containsKey(OAuthConstants.CLIENT_ID)
                    && request.getParameterMap().containsKey(OAuthConstants.CLIENT_SECRET)) {
                // Metadata 1: Retrieve metadata about a given client if both the client id and secret are provided
                return metadataClientController.handleRequest(request, response);
            } else {
                // Metadata 2: Retrieve metadata about a principal if neither the client id nor the secret is provided.
                // In addition, a valid access token of type CAS must be presented for authorization.
                return metadataPrincipalController.handleRequest(request, response);
            }
        }

        // Unknown OAuth 2.0 endpoint and/or unsupported HTTP method
        LOGGER.error("Unknown method : {}", method);
        OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_BAD_REQUEST);
        return null;
    }

    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    public void setCentralOAuthService(final CentralOAuthService centralOAuthService) {
        this.centralOAuthService = centralOAuthService;
    }
}
