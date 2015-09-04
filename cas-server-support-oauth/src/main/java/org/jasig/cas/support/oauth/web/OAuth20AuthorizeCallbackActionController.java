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
import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.InvalidParameterException;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.AuthorizationCode;
import org.jasig.cas.support.oauth.token.TokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This controller is called if a user selects an action to allow or deny
 * authorization.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public final class OAuth20AuthorizeCallbackActionController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20AuthorizeCallbackActionController.class);

    private final CentralOAuthService centralOAuthService;

    private final Long timeout;

    /**
     * Instantiates a new o auth20 authorize callback action controller.
     *
     * @param centralOAuthService the central oauth service
     * @param timeout the ticket timeout
     */
    public OAuth20AuthorizeCallbackActionController(final CentralOAuthService centralOAuthService, final Long timeout) {
        this.centralOAuthService = centralOAuthService;
        this.timeout = timeout;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final HttpSession session = request.getSession();

        // get action
        final String action = request.getParameter(OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION, action);

        final String responseType = (String) session.getAttribute(OAuthConstants.OAUTH20_RESPONSE_TYPE);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_RESPONSE_TYPE, responseType);
        session.removeAttribute(OAuthConstants.OAUTH20_RESPONSE_TYPE);

        final String clientId = (String) session.getAttribute(OAuthConstants.OAUTH20_CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_CLIENT_ID, clientId);
        session.removeAttribute(OAuthConstants.OAUTH20_CLIENT_ID);

        // retrieve state from session (csrf equivalent)
        final String state = (String) session.getAttribute(OAuthConstants.OAUTH20_STATE);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_STATE, state);
        session.removeAttribute(OAuthConstants.OAUTH20_STATE);

        final String redirectUri = (String) session.getAttribute(OAuthConstants.OAUTH20_REDIRECT_URI);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_REDIRECT_URI, redirectUri);
        session.removeAttribute(OAuthConstants.OAUTH20_REDIRECT_URI);

        final TokenType tokenType = (TokenType) session.getAttribute(OAuthConstants.OAUTH20_TOKEN_TYPE);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_TOKEN_TYPE, tokenType);
        session.removeAttribute(OAuthConstants.OAUTH20_TOKEN_TYPE);

        final String loginTicketId = (String) session.getAttribute(OAuthConstants.OAUTH20_LOGIN_TICKET_ID);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_LOGIN_TICKET_ID, loginTicketId);
        session.removeAttribute(OAuthConstants.OAUTH20_LOGIN_TICKET_ID);

        @SuppressWarnings("unchecked")
        final Set<String> scopeSet = (Set<String>) session.getAttribute(OAuthConstants.OAUTH20_SCOPE_SET);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_SCOPE_SET, scopeSet);
        session.removeAttribute(OAuthConstants.OAUTH20_SCOPE_SET);

        if (!action.equalsIgnoreCase(OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION_ALLOW)) {
            LOGGER.warn("Approval Prompt Action was denied by the user.");
            final String deniedCallbackUrl = OAuthUtils.addParameter(redirectUri, OAuthConstants.ERROR, OAuthConstants.ACCESS_DENIED);
            LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_REDIRECT_URI, deniedCallbackUrl);
            return OAuthUtils.redirectTo(deniedCallbackUrl);
        }

        if (StringUtils.isBlank(clientId)) {
            LOGGER.error("{} is missing from the session and can not be retrieved.", OAuthConstants.OAUTH20_CLIENT_ID);
            throw new InvalidParameterException(OAuthConstants.OAUTH20_CLIENT_ID);
        }

        if (StringUtils.isBlank(redirectUri)) {
            LOGGER.error("{} is missing from the session and can not be retrieved.", OAuthConstants.OAUTH20_REDIRECT_URI);
            throw new InvalidParameterException(OAuthConstants.OAUTH20_REDIRECT_URI);
        }

        if ("token".equals(responseType)) {
            final AuthorizationCode authorizationCode = centralOAuthService.grantAuthorizationCode(
                    TokenType.ONLINE, clientId, loginTicketId, redirectUri, scopeSet);
            final AccessToken accessToken = centralOAuthService.grantOnlineAccessToken(authorizationCode);

            String callbackUrl = redirectUri;
            callbackUrl += "#" + OAuthConstants.ACCESS_TOKEN + "=" + accessToken.getId();
            callbackUrl += "&" + OAuthConstants.EXPIRES_IN + "="
                    + (int) (timeout - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()
                    - accessToken.getTicket().getCreationTime()));
            callbackUrl += "&" + OAuthConstants.TOKEN_TYPE + "=" + OAuthConstants.BEARER_TOKEN;
            if (!StringUtils.isBlank(state)) {
                callbackUrl += "&" + OAuthConstants.STATE + "=" + state;
            }
            LOGGER.debug("Redirecting Client to : {}", callbackUrl);
            return OAuthUtils.redirectTo(callbackUrl);
        }

        // response type is code
        final AuthorizationCode authorizationCode = centralOAuthService.grantAuthorizationCode(
                tokenType, clientId, loginTicketId, redirectUri, scopeSet);

        String callbackUrl = OAuthUtils.addParameter(redirectUri, OAuthConstants.CODE, authorizationCode.getId());
        if (!StringUtils.isBlank(state)) {
            callbackUrl = OAuthUtils.addParameter(callbackUrl, OAuthConstants.STATE, state);
        }
        LOGGER.debug("Redirecting Client to : {}", callbackUrl);
        return OAuthUtils.redirectTo(callbackUrl);
    }
}
