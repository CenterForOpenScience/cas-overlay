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
 * The OAuth 2.0 authorization controller.
 *
 * This controller handles the client's the initial authorization request {@literal /oauth2/authorize}. It verifies the
 * request, stores the authorization parameters in session and finally redirects the user to the CAS default login page
 * for primary authentication with the authorization callback endpoint {@literal /oauth2/callbackAuthorize} as service,
 * which further handles CAS service validation and OAuth authorization callback.
 *
 * @author Jerome Leleu
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuth20AuthorizeController extends AbstractController {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20AuthorizeController.class);

    /** The CAS OAuth authorization service. */
    private final CentralOAuthService centralOAuthService;

    /** The primary CAS authentication login url. */
    private final String loginUrl;

    /**
     * Instantiates a new {@link OAuth20AuthorizeController}.
     *
     * @param centralOAuthService the CAS OAuth service
     * @param loginUrl the CAS login url
     */
    public OAuth20AuthorizeController(final CentralOAuthService centralOAuthService, final String loginUrl) {
        this.centralOAuthService = centralOAuthService;
        this.loginUrl = loginUrl;
    }

    @Override
    protected ModelAndView handleRequestInternal(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

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

        // Verify the authorization request.
        verifyRequest(responseType, clientId, redirectUri, accessType);

        // Retrieve the OAuth registered service.
        final OAuthRegisteredService service = centralOAuthService.getRegisteredService(clientId);
        if (service == null) {
            LOGGER.error("Unknown {} : {}", OAuthConstants.CLIENT_ID, clientId);
            throw new InvalidParameterException(OAuthConstants.CLIENT_ID);
        }

        // Verify the redirect uri, which is stored as the `serviceId` property of the `OAuthRegisteredService` class.
        // It is not a regular expression but a literal string and thus the match is done by a case-insensitive string
        // equality check.
        if (!redirectUri.equalsIgnoreCase(service.getServiceId())) {
            LOGGER.error(
                    "Unmatched {} : {} for serviceId : {}",
                    OAuthConstants.REDIRECT_URI,
                    redirectUri, service.getServiceId()
            );
            throw new InvalidParameterException(OAuthConstants.REDIRECT_URI);
        }

        // Keep the authorization parameters in session.
        final HttpSession session = request.getSession();
        session.setAttribute(OAuthConstants.BYPASS_APPROVAL_PROMPT, service.isBypassApprovalPrompt());
        session.setAttribute(
                OAuthConstants.OAUTH20_APPROVAL_PROMPT,
                StringUtils.isBlank(approvalPrompt) ? OAuthConstants.APPROVAL_PROMPT_AUTO : approvalPrompt
        );
        session.setAttribute(
                OAuthConstants.OAUTH20_TOKEN_TYPE,
                TokenType.valueOf(StringUtils.isBlank(accessType) ? "ONLINE" : accessType.toUpperCase())
        );
        session.setAttribute(
                OAuthConstants.OAUTH20_RESPONSE_TYPE,
                StringUtils.isBlank(responseType) ? "code" : responseType.toLowerCase()
        );
        session.setAttribute(OAuthConstants.OAUTH20_CLIENT_ID, clientId);
        session.setAttribute(OAuthConstants.OAUTH20_REDIRECT_URI, redirectUri);
        session.setAttribute(OAuthConstants.OAUTH20_SERVICE_NAME, service.getName());
        session.setAttribute(
                OAuthConstants.OAUTH20_SCOPE,
                StringUtils.isBlank(scope) ? "" : scope
        );
        session.setAttribute(OAuthConstants.OAUTH20_STATE, state);

        // Generate the authorization callback url.
        final String callbackAuthorizeUrl = request.getRequestURL().toString().replace(
                "/" + OAuthConstants.AUTHORIZE_URL,
                "/" + OAuthConstants.CALLBACK_AUTHORIZE_URL
        );
        LOGGER.debug("{} : {}", OAuthConstants.CALLBACK_AUTHORIZE_URL, callbackAuthorizeUrl);

        // Generate the CAS login url with the authorization callback url as service.
        final String loginUrlWithService
                = OAuthUtils.addParameter(loginUrl, OAuthConstants.SERVICE, callbackAuthorizeUrl);
        LOGGER.debug("loginUrlWithService : {}", loginUrlWithService);

        // Finally, redirect to the CAS default login endpoint.
        return OAuthUtils.redirectTo(loginUrlWithService);
    }

    /**
     * Verify the request by reviewing the values of OAuth 2.0 parameters.
     *
     * @param responseType the response type
     * @param clientId the client id
     * @param redirectUri the redirect uri
     * @param accessType the access type
     * @throws InvalidParameterException with the name of the invalid parameter
     */
    private void verifyRequest(
            final String responseType,
            final String clientId,
            final String redirectUri,
            final String accessType
    ) throws InvalidParameterException {

        // Response type can either be "code" or "token", default (if not provided) is "code".
        if (!StringUtils.isBlank(responseType)) {
            if (!"code".equalsIgnoreCase(responseType) && !"token".equalsIgnoreCase(responseType)) {
                LOGGER.error("Invalid {} specified : {}", OAuthConstants.RESPONSE_TYPE, responseType);
                throw new InvalidParameterException(OAuthConstants.RESPONSE_TYPE);
            }
        }

        // Client id is required (not empty).
        if (StringUtils.isBlank(clientId)) {
            LOGGER.error("Missing {}", OAuthConstants.CLIENT_ID);
            throw new InvalidParameterException(OAuthConstants.CLIENT_ID);
        }

        // Redirect uri is required (not empty).
        if (StringUtils.isBlank(redirectUri)) {
            LOGGER.error("Missing {}", OAuthConstants.REDIRECT_URI);
            throw new InvalidParameterException(OAuthConstants.REDIRECT_URI);
        }

        // Access type can either be "OFFLINE" or "ONLINE", default (if not provided) is "ONLINE".
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
