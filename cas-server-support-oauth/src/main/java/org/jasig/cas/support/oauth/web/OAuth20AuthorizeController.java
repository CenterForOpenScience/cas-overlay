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
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.token.TokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This controller is in charge of responding to the authorize call in
 * OAuth protocol. It stores the callback url and redirects user to the
 * login page with the callback service.
 *
 * @author Jerome Leleu
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuth20AuthorizeController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20AuthorizeController.class);

    private final CentralOAuthService centralOAuthService;

    private final String loginUrl;

    /**
     * Instantiates a new o auth20 authorize controller.
     *
     * @param centralOAuthService the central oauth service
     * @param loginUrl the login url
     */
    public OAuth20AuthorizeController(final CentralOAuthService centralOAuthService, final String loginUrl) {
        this.centralOAuthService = centralOAuthService;
        this.loginUrl = loginUrl;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String responseType = request.getParameter(OAuthConstants.RESPONSE_TYPE);
        LOGGER.debug("{} : {}", OAuthConstants.RESPONSE_TYPE, responseType);

        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_ID, clientId);

        final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
        LOGGER.debug("{} : {}", OAuthConstants.REDIRECT_URI, redirectUri);

        final String scope = request.getParameter(OAuthConstants.SCOPE);
        LOGGER.debug("{} : {}", OAuthConstants.SCOPE, scope);

        final String state = request.getParameter(OAuthConstants.STATE);
        LOGGER.debug("{} : {}", OAuthConstants.STATE, state);

        final String accessType = request.getParameter(OAuthConstants.ACCESS_TYPE);
        LOGGER.debug("{} : {}", OAuthConstants.ACCESS_TYPE, accessType);

        final String approvalPrompt = request.getParameter(OAuthConstants.APPROVAL_PROMPT);
        LOGGER.debug("{} : {}", OAuthConstants.APPROVAL_PROMPT, approvalPrompt);

        verifyRequest(responseType, clientId, redirectUri, accessType);

        final OAuthRegisteredService service = centralOAuthService.getRegisteredService(clientId);
        if (service == null) {
            LOGGER.error("Unknown {} : {}", OAuthConstants.CLIENT_ID, clientId);
            throw new InvalidParameterException(OAuthConstants.CLIENT_ID);
        }
        // Redirect URI is a literal string (not regex) and the match is done by case-insensitive equality check.
        if (!redirectUri.equalsIgnoreCase(service.getServiceId())) {
            LOGGER.error("Unmatched {} : {} for serviceId : {}", OAuthConstants.REDIRECT_URI, redirectUri, service.getServiceId());
            throw new InvalidParameterException(OAuthConstants.REDIRECT_URI);
        }

        // keep info in session
        final HttpSession session = request.getSession();
        session.setAttribute(OAuthConstants.BYPASS_APPROVAL_PROMPT, service.isBypassApprovalPrompt());
        session.setAttribute(OAuthConstants.OAUTH20_APPROVAL_PROMPT, StringUtils.isBlank(approvalPrompt)
                ? OAuthConstants.APPROVAL_PROMPT_AUTO : approvalPrompt);
        session.setAttribute(OAuthConstants.OAUTH20_TOKEN_TYPE, TokenType.valueOf(StringUtils.isBlank(accessType)
                ? "ONLINE" : accessType.toUpperCase()));
        session.setAttribute(OAuthConstants.OAUTH20_RESPONSE_TYPE, StringUtils.isBlank(responseType) ? "code" : responseType.toLowerCase());
        session.setAttribute(OAuthConstants.OAUTH20_CLIENT_ID, clientId);
        session.setAttribute(OAuthConstants.OAUTH20_REDIRECT_URI, redirectUri);
        session.setAttribute(OAuthConstants.OAUTH20_SERVICE_NAME, service.getName());
        session.setAttribute(OAuthConstants.OAUTH20_SCOPE, StringUtils.isBlank(scope) ? "" : scope);
        session.setAttribute(OAuthConstants.OAUTH20_STATE, state);

        final String callbackAuthorizeUrl = request.getRequestURL().toString()
                .replace("/" + OAuthConstants.AUTHORIZE_URL, "/" + OAuthConstants.CALLBACK_AUTHORIZE_URL);
        LOGGER.debug("{} : {}", OAuthConstants.CALLBACK_AUTHORIZE_URL, callbackAuthorizeUrl);

        final String loginUrlWithService = OAuthUtils.addParameter(loginUrl, OAuthConstants.SERVICE, callbackAuthorizeUrl);
        LOGGER.debug("loginUrlWithService : {}", loginUrlWithService);

        return OAuthUtils.redirectTo(loginUrlWithService);
    }

    /**
     * Verify the request by reviewing the values of client id, redirect uri, etc...
     *
     * @param responseType the response type
     * @param clientId the client id
     * @param redirectUri the redirect uri
     * @param accessType the access type
     * @throws InvalidParameterException with the name of the invalid parameter
     */
    private void verifyRequest(final String responseType, final String clientId, final String redirectUri, final String accessType)
        throws InvalidParameterException {
        // responseType must be valid
        if (!StringUtils.isBlank(responseType)) {
            if (!"code".equalsIgnoreCase(responseType) && !"token".equalsIgnoreCase(responseType)) {
                LOGGER.error("Invalid {} specified : {}", OAuthConstants.RESPONSE_TYPE, responseType);
                throw new InvalidParameterException(OAuthConstants.RESPONSE_TYPE);
            }
        }
        // clientId is required
        if (StringUtils.isBlank(clientId)) {
            LOGGER.error("Missing {}", OAuthConstants.CLIENT_ID);
            throw new InvalidParameterException(OAuthConstants.CLIENT_ID);
        }
        // redirectUri is required
        if (StringUtils.isBlank(redirectUri)) {
            LOGGER.error("Missing {}", OAuthConstants.REDIRECT_URI);
            throw new InvalidParameterException(OAuthConstants.REDIRECT_URI);
        }
        // accessType must be valid, default is ONLINE
        if (!StringUtils.isBlank(accessType)) {
            try {
                final TokenType tokenType = TokenType.valueOf(accessType.toUpperCase());
                if (tokenType != TokenType.OFFLINE && tokenType != TokenType.ONLINE) {
                    LOGGER.error("Invalid {} specified", OAuthConstants.ACCESS_TYPE);
                    throw new InvalidParameterException(OAuthConstants.ACCESS_TYPE);
                }
            } catch (final IllegalArgumentException e) {
                LOGGER.error("Could not map enumeration for {} : {}", OAuthConstants.ACCESS_TYPE, accessType);
                throw new InvalidParameterException(OAuthConstants.ACCESS_TYPE);
            }
        }
    }
}
