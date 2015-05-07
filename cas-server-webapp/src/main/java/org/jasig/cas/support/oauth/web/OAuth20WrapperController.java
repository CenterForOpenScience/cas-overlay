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

/**
 * This controller is the main entry point for OAuth version 2.0
 * wrapping in CAS, should be mapped to something like /oauth2.0/*. Dispatch
 * request to specific controllers : authorize, accessToken...
 *
 * @author Jerome Leleu, Michael Haselton
 * @since 3.5.0
 */
public final class OAuth20WrapperController extends BaseOAuthWrapperController implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20WrapperController.class);

    private AbstractController authorizeController;

    private AbstractController callbackAuthorizeController;

    private AbstractController callbackAuthorizeActionController;

    private AbstractController grantTypeAuthorizationCodeController;

    private AbstractController grantTypeRefreshTokenController;

    private AbstractController revokeTokenController;

    private AbstractController profileController;

    /** Instance of CentralAuthenticationService. */
    @NotNull
    private CentralAuthenticationService centralAuthenticationService;

    @Override
    public void afterPropertiesSet() throws Exception {
        authorizeController = new OAuth20AuthorizeController(servicesManager, loginUrl);
        callbackAuthorizeController = new OAuth20CallbackAuthorizeController(ticketRegistry);
        callbackAuthorizeActionController = new OAuth20CallbackAuthorizeActionController(servicesManager, centralAuthenticationService);
        grantTypeAuthorizationCodeController = new OAuth20GrantTypeAuthorizationCodeController(servicesManager, ticketRegistry, centralAuthenticationService, timeout);
        grantTypeRefreshTokenController = new OAuth20GrantTypeRefreshTokenController(servicesManager, ticketRegistry, centralAuthenticationService, timeout);
        revokeTokenController = new OAuth20RevokeController(ticketRegistry);
        profileController = new OAuth20ProfileController(ticketRegistry);
    }

    @Override
    protected ModelAndView internalHandleRequest(final String method, final HttpServletRequest request,
                                                 final HttpServletResponse response) throws Exception {
        // authorize
        if (OAuthConstants.AUTHORIZE_URL.equals(method) && request.getMethod().equals("GET")) {
            return authorizeController.handleRequest(request, response);
        }
        // callback on authorize
        if (OAuthConstants.CALLBACK_AUTHORIZE_URL.equals(method) && request.getMethod().equals("GET")) {
            return callbackAuthorizeController.handleRequest(request, response);
        }
        // callback on authorize action
        if (OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL.equals(method) && request.getMethod().equals("GET")) {
            return callbackAuthorizeActionController.handleRequest(request, response);
        }

        // access token
        if (OAuthConstants.ACCESS_TOKEN_URL.equals(method) && request.getMethod().equals("POST")) {
            final String             grantType = request.getParameter(OAuthConstants.GRANT_TYPE);
            LOGGER.debug("{} : {}", OAuthConstants.GRANT_TYPE, grantType);

            if (grantType.equals(OAuthConstants.AUTHORIZATION_CODE)) {
                return grantTypeAuthorizationCodeController.handleRequest(request, response);
            } else if (grantType.equals(OAuthConstants.REFRESH_TOKEN)) {
                return grantTypeRefreshTokenController.handleRequest(request, response);
            }
        }

        // revoke token
        if (OAuthConstants.REVOKE_TOKEN_URL.equals(method) && request.getMethod().equals("GET")) {
            return revokeTokenController.handleRequest(request, response);
        }

        // get profile
        if (OAuthConstants.PROFILE_URL.equals(method) && request.getMethod().equals("GET")) {
            return profileController.handleRequest(request, response);
        }

        // else error
        logger.error("Unknown method : {}", method);
        OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_OK);
        return null;
    }

    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }
}
