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
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.scope.OAuthScope;
import org.jasig.cas.support.oauth.token.CodeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * This controller is called if a user selects an action to allow or deny
 * authorization.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public final class OAuth20CallbackAuthorizeActionController extends AbstractController {

    private final Logger LOGGER = LoggerFactory.getLogger(OAuth20CallbackAuthorizeActionController.class);

    private final CentralOAuthService centralOAuthService;

    public OAuth20CallbackAuthorizeActionController(final CentralOAuthService centralOAuthService) {
        this.centralOAuthService = centralOAuthService;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final HttpSession session = request.getSession();

        // get action
        final String action = request.getParameter(OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION, action);

        if (!action.equalsIgnoreCase(OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION_ALLOW)) {
            LOGGER.error("{} was not allowed.", OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION);
            // callback url with error
            String callbackUrl = (String) session.getAttribute(OAuthConstants.OAUTH20_CALLBACKURL);
            callbackUrl = OAuthUtils.addParameter(callbackUrl, OAuthConstants.ERROR, OAuthConstants.ACCESS_DENIED);
            LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_CALLBACKURL, callbackUrl);
            return OAuthUtils.redirectTo(callbackUrl);
        }

        // retrieve client id from session
        final String clientId = (String) session.getAttribute(OAuthConstants.OAUTH20_CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_CLIENT_ID, clientId);
        session.removeAttribute(OAuthConstants.OAUTH20_CLIENT_ID);
        if (StringUtils.isBlank(clientId)) {
            LOGGER.error("{} is missing from the session and can not be retrieved.", OAuthConstants.OAUTH20_CLIENT_ID);
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }

        // retrieve callback url from session
        final String callbackUrl = (String) session.getAttribute(OAuthConstants.OAUTH20_CALLBACKURL);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_CALLBACKURL, callbackUrl);
        session.removeAttribute(OAuthConstants.OAUTH20_CALLBACKURL);
        if (StringUtils.isBlank(callbackUrl)) {
            LOGGER.error("{} is missing from the session and can not be retrieved.", OAuthConstants.OAUTH20_CALLBACKURL);
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }

        // retrieve scope from session
        @SuppressWarnings("unchecked")
        final Map<String, OAuthScope> scopeMap = (Map<String, OAuthScope>) session.getAttribute(OAuthConstants.OAUTH20_SCOPE_MAP);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_SCOPE_MAP, scopeMap);
        session.removeAttribute(OAuthConstants.OAUTH20_SCOPE_MAP);

        final String loginTicketId = (String) session.getAttribute(OAuthConstants.OAUTH20_LOGIN_TICKET_ID);
        session.removeAttribute(OAuthConstants.OAUTH20_LOGIN_TICKET_ID);

        final CodeToken codeToken = centralOAuthService.grantCodeToken(loginTicketId, clientId, callbackUrl, scopeMap.keySet());
        LOGGER.debug("{} : {}", OAuthConstants.CODE, codeToken);
        if (codeToken == null) {
            LOGGER.error("Unknown Code Token for tgtId [{}], clientId [{}] and callbackUrl [{}]", loginTicketId, clientId, callbackUrl);
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }

        // redirect url is comprised of the callback url, code and possibly state
        String redirectUrl = OAuthUtils.addParameter(callbackUrl, OAuthConstants.CODE, codeToken.getId());

        // retrieve state from session (csrf equivalent)
        final String state = (String) session.getAttribute(OAuthConstants.OAUTH20_STATE);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_STATE, state);
        session.removeAttribute(OAuthConstants.OAUTH20_STATE);
        if (state != null) {
            redirectUrl = OAuthUtils.addParameter(redirectUrl, OAuthConstants.STATE, state);
        }

        LOGGER.debug("Redirecting Client to {} : {}", OAuthConstants.OAUTH20_CALLBACKURL, redirectUrl);
        return OAuthUtils.redirectTo(redirectUrl);
    }
}
