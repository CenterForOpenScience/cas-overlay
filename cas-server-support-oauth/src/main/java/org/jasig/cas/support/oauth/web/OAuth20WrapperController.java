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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

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

    private AbstractController tokenAuthorizationCodeController;
    private AbstractController tokenRefreshTokenController;

    private AbstractController revokeTokenController;
    private AbstractController revokeClientPrincipalTokensController;
    private AbstractController revokeClientTokensController;

    private AbstractController profileController;

    private AbstractController metadataPrincipalController;
    private AbstractController metadataClientController;

    /** Instance of CentralAuthenticationService. */
    @NotNull
    private CentralAuthenticationService centralAuthenticationService;

    /** Instance of CentralOAuthService. */
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
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        try {
            return super.handleRequest(request, response);
        } catch (final RootCasException e) {
            // capture any root cas exceptions and display them properly to the user.
            final Map<String, Object> map = new HashMap<>();
            map.put("rootCauseException", e);
            return new ModelAndView(OAuthConstants.ERROR_VIEW, map);
        }
    }

    @Override
    protected ModelAndView internalHandleRequest(final String method, final HttpServletRequest request,
                                                 final HttpServletResponse response) throws Exception {
        // authorize
        if (OAuthConstants.AUTHORIZE_URL.equals(method) && "GET".equals(request.getMethod())) {
            return authorizeController.handleRequest(request, response);
        }
        // authorize callback
        if (OAuthConstants.CALLBACK_AUTHORIZE_URL.equals(method) && "GET".equals(request.getMethod())) {
            return authorizeCallbackController.handleRequest(request, response);
        }
        // authorize callback action
        if (OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL.equals(method) && "GET".equals(request.getMethod())) {
            return authorizeCallbackActionController.handleRequest(request, response);
        }

        // token
        if (OAuthConstants.TOKEN_URL.equals(method) && "POST".equals(request.getMethod())) {
            final String grantType = request.getParameter(OAuthConstants.GRANT_TYPE);
            LOGGER.debug("{} : {}", OAuthConstants.GRANT_TYPE, grantType);

            if (grantType != null) {
                if (grantType.equals(OAuthConstants.AUTHORIZATION_CODE)) {
                    return tokenAuthorizationCodeController.handleRequest(request, response);
                } else if (grantType.equals(OAuthConstants.REFRESH_TOKEN)) {
                    return tokenRefreshTokenController.handleRequest(request, response);
                }
            }

            return OAuthUtils.writeJsonError(response, OAuthConstants.INVALID_REQUEST,
                                             new InvalidParameterException(OAuthConstants.GRANT_TYPE).getMessage(),
                                             HttpStatus.SC_BAD_REQUEST);
        }

        // revoke
        if (OAuthConstants.REVOKE_URL.equals(method) && "POST".equals(request.getMethod())) {
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
        if (OAuthConstants.PROFILE_URL.equals(method) && "GET".equals(request.getMethod())) {
            return profileController.handleRequest(request, response);
        }

        // metadata
        if (OAuthConstants.METADATA_URL.equals(method) && "POST".equals(request.getMethod())) {
            if (request.getParameterMap().containsKey(OAuthConstants.CLIENT_ID)
                    && request.getParameterMap().containsKey(OAuthConstants.CLIENT_SECRET)) {
                return metadataClientController.handleRequest(request, response);
            } else {
                return metadataPrincipalController.handleRequest(request, response);
            }
        }

        // error
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
