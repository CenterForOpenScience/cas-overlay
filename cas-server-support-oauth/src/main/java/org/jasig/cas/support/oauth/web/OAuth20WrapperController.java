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

import org.apache.http.HttpStatus;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.token.registry.TokenRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * This controller is the main entry point for OAuth version 2.0
 * wrapping in CAS, should be mapped to something like /oauth2.0/*. Dispatch
 * request to specific controllers : authorize, accessToken...
 *
 * @author Jerome Leleu
 * @author Michael Haselton
 * @since 3.5.0
 */
public final class OAuth20WrapperController extends BaseOAuthWrapperController implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20WrapperController.class);

    private AbstractController authorizeController;
    private AbstractController authorizeCallbackController;
    private AbstractController authorizeCallbackActionController;

    private AbstractController tokenAuthorizationController;
    private AbstractController tokenAuthorizationCodeController;
    private AbstractController tokenRefreshTokenController;

    private AbstractController revokeTokenController;
    private AbstractController revokeClientPrincipalTokensController;
    private AbstractController revokeClientTokensController;

    private AbstractController profileController;

    private AbstractController metadataApplicationController;

    /** Instance of CentralAuthenticationService. */
    @NotNull
    private TokenRegistry tokenRegistry;

    /** Instance of CentralAuthenticationService. */
    @NotNull
    private CentralAuthenticationService centralAuthenticationService;

    /** Instance of CentralOAuthService. */
    @NotNull
    private CentralOAuthService centralOAuthService;

    @Override
    public void afterPropertiesSet() throws Exception {
        // TODO: move timeout to central oauth service

        authorizeController = new OAuth20AuthorizeController(centralOAuthService, loginUrl);
        authorizeCallbackController = new OAuth20AuthorizeCallbackController(centralOAuthService, ticketRegistry);
        authorizeCallbackActionController = new OAuth20AuthorizeCallbackActionController(centralOAuthService, timeout);

//        tokenAuthorizationController = new OAuth20TokenAuthorizationController(servicesManager, centralAuthenticationService);
        tokenAuthorizationCodeController = new OAuth20TokenAuthorizationCodeController(centralOAuthService, timeout);
        tokenRefreshTokenController = new OAuth20TokenRefreshTokenController(centralOAuthService, timeout);

        revokeTokenController = new OAuth20RevokeTokenController(centralOAuthService);
        revokeClientTokensController = new OAuth20RevokeClientTokensController(centralOAuthService);
        revokeClientPrincipalTokensController = new OAuth20RevokeClientPrincipalTokensController(centralOAuthService);

        profileController = new OAuth20ProfileController(centralOAuthService, centralAuthenticationService);
//        metadataApplicationController = new OAuth20MetadataApplicationController(servicesManager, centralAuthenticationService);
    }

    @Override
    protected ModelAndView internalHandleRequest(final String method, final HttpServletRequest request,
                                                 final HttpServletResponse response) throws Exception {
        // authorize
        if (OAuthConstants.AUTHORIZE_URL.equals(method) && request.getMethod().equals("GET")) {
            return authorizeController.handleRequest(request, response);
        }
        // callback authorize
        if (OAuthConstants.CALLBACK_AUTHORIZE_URL.equals(method) && request.getMethod().equals("GET")) {
            return authorizeCallbackController.handleRequest(request, response);
        }
        // callback on authorize action
        if (OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL.equals(method) && request.getMethod().equals("GET")) {
            return authorizeCallbackActionController.handleRequest(request, response);
        }

        // token
        if (OAuthConstants.TOKEN_URL.equals(method)) {
            if (request.getMethod().equals("GET")) {
                return tokenAuthorizationController.handleRequest(request, response);
            } else if (request.getMethod().equals("POST")) {
                final String grantType = request.getParameter(OAuthConstants.GRANT_TYPE);
                LOGGER.debug("{} : {}", OAuthConstants.GRANT_TYPE, grantType);

                if (grantType.equals(OAuthConstants.AUTHORIZATION_CODE)) {
                    return tokenAuthorizationCodeController.handleRequest(request, response);
                } else if (grantType.equals(OAuthConstants.REFRESH_TOKEN)) {
                    return tokenRefreshTokenController.handleRequest(request, response);
                }
            }
        }

        // revoke
        if (OAuthConstants.REVOKE_URL.equals(method) && request.getMethod().equals("POST")) {
            if (request.getParameterMap().containsKey(OAuthConstants.CLIENT_ID)) {
                if (request.getParameterMap().containsKey(OAuthConstants.CLIENT_SECRET)) {
                    return revokeClientTokensController.handleRequest(request, response);
                }
                return revokeClientPrincipalTokensController.handleRequest(request, response);
            } else {
                return revokeTokenController.handleRequest(request, response);
            }
        }

        // profile
        if (OAuthConstants.PROFILE_URL.equals(method) && request.getMethod().equals("GET")) {
            return profileController.handleRequest(request, response);
        }

        // metadata
        if (OAuthConstants.METADATA_URL.equals(method) && request.getMethod().equals("GET")) {
            if (request.getParameterMap().containsKey(OAuthConstants.CLIENT_ID) &&
                    request.getParameterMap().containsKey(OAuthConstants.CLIENT_SECRET)) {
                return metadataApplicationController.handleRequest(request, response);
            }
        }

        // else error
        logger.error("Unknown method : {}", method);
        OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_OK);
        return null;
    }

    public void setTokenRegistry(final TokenRegistry tokenRegistry) {
        this.tokenRegistry = tokenRegistry;
    }

    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    public void setCentralOAuthService(final CentralOAuthService centralOAuthService) {
        this.centralOAuthService = centralOAuthService;
    }
}
