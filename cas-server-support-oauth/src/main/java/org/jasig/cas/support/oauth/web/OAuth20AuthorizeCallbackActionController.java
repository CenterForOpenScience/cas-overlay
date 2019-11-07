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

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The OAuth 2.0 authorization callback action controller.
 *
 * This controller is called 1) if the user clicks an action button on the OAuth confirmation page to allow / deny the
 * authorization or 2) if the {@link OAuth20AuthorizeCallbackController} decides to bypass the aforementioned explicit
 * user confirmation with "allow" as the action. If allowed, the user is redirected to the "redirect uri" with `token`
 * or `code` and other parameters appended as fragment (after the "#"). Otherwise if denied, the user is redirected to
 * the "redirect uri" with the query parameter `error=access_denied`.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuth20AuthorizeCallbackActionController extends AbstractController {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20AuthorizeCallbackActionController.class);

    /** The CAS OAuth authorization service. */
    private final CentralOAuthService centralOAuthService;

    /** The ticket timeout. */
    private final Long timeout;

    /**
     * Instantiates a new {@link OAuth20AuthorizeCallbackActionController}.
     *
     * @param centralOAuthService the central oauth service
     * @param timeout the ticket timeout
     */
    public OAuth20AuthorizeCallbackActionController(
            final CentralOAuthService centralOAuthService,
            final Long timeout
    ) {
        this.centralOAuthService = centralOAuthService;
        this.timeout = timeout;
    }

    @Override
    protected ModelAndView handleRequestInternal(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final HttpSession session = request.getSession();

        // Retrieve the authorization action of the the user.
        final String action = request.getParameter(OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION, action);

        final String responseType = (String) session.getAttribute(OAuthConstants.OAUTH20_RESPONSE_TYPE);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_RESPONSE_TYPE, responseType);
        session.removeAttribute(OAuthConstants.OAUTH20_RESPONSE_TYPE);

        final String clientId = (String) session.getAttribute(OAuthConstants.OAUTH20_CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_CLIENT_ID, clientId);
        session.removeAttribute(OAuthConstants.OAUTH20_CLIENT_ID);

        // Retrieve the state from session, which is equivalent to using CSRF token and prevents CSRF.
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

        // The user has denied the authorization. Redirect to the "redirect uri" with an error query parameter.
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

        // TODO: The "token" response type should be disabled. It is very dangerous to include the access token in the
        //       URL as a query parameter (as opposed to be returned in a HTTPS POST response body) since it is neither
        //       one-time or short-lived (as opposed to the authorization code).
        if ("token".equals(responseType)) {
            final AuthorizationCode authorizationCode = centralOAuthService
                    .grantAuthorizationCode(TokenType.ONLINE, clientId, loginTicketId, redirectUri, scopeSet);
            final AccessToken accessToken = centralOAuthService.grantOnlineAccessToken(authorizationCode);
            String callbackUrl = redirectUri;
            final long timeSinceTicketCreation = System.currentTimeMillis() - accessToken.getTicket().getCreationTime();
            final int expiresIn = (int) (timeout - TimeUnit.MILLISECONDS.toSeconds(timeSinceTicketCreation));
            callbackUrl += "#" + OAuthConstants.ACCESS_TOKEN + "=" + accessToken.getId();
            callbackUrl += "&" + OAuthConstants.EXPIRES_IN + "=" + expiresIn;
            callbackUrl += "&" + OAuthConstants.TOKEN_TYPE + "=" + OAuthConstants.BEARER_TOKEN;
            if (!StringUtils.isBlank(state)) {
                callbackUrl += "&" + OAuthConstants.STATE + "=" + state;
            }
            LOGGER.debug("Redirecting Client to : {}", callbackUrl);
            return OAuthUtils.redirectTo(callbackUrl);
        }

        // Response type is "code", redirect to the callback url (which is the redirect uri of the registered service
        // instead of the OAuth authorization callback endpoints this time) with code and state.
        final AuthorizationCode authorizationCode = centralOAuthService
                .grantAuthorizationCode(tokenType, clientId, loginTicketId, redirectUri, scopeSet);
        String callbackUrl = OAuthUtils.addParameter(redirectUri, OAuthConstants.CODE, authorizationCode.getId());
        if (!StringUtils.isBlank(state)) {
            callbackUrl = OAuthUtils.addParameter(callbackUrl, OAuthConstants.STATE, state);
        }
        LOGGER.debug("Redirecting Client to : {}", callbackUrl);
        return OAuthUtils.redirectTo(callbackUrl);
    }
}
