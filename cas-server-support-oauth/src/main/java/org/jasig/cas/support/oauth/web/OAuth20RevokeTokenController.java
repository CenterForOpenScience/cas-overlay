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
import org.jasig.cas.support.oauth.*;
import org.jasig.cas.support.oauth.token.Token;
import org.jasig.cas.ticket.InvalidTicketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This controller handles requests to revoke access tokens and refresh tokens.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public final class OAuth20RevokeTokenController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20RevokeTokenController.class);

    private final CentralOAuthService centralOAuthService;

    /**
     * Instantiates a new oauth2 revoke user token controller.
     *
     * @param centralOAuthService the central oauth service
     */
    public OAuth20RevokeTokenController(final CentralOAuthService centralOAuthService) {
        this.centralOAuthService = centralOAuthService;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String tokenId = request.getParameter(OAuthConstants.TOKEN);
        LOGGER.debug("{} : {}", OAuthConstants.TOKEN, tokenId);

        final Token token;
        try {
            token = centralOAuthService.getToken(tokenId);
        } catch (InvalidTicketException e) {
            LOGGER.error("Unknown token : {}", tokenId);
            throw new TokenInvalidException();
        }

        if (!centralOAuthService.revokeToken(token)) {
            LOGGER.error("Token revocation failed [{}]", token.getId());
            throw new TokenInvalidException();
        }

        return OAuthUtils.writeText(response, null, HttpStatus.SC_NO_CONTENT);
    }
}
