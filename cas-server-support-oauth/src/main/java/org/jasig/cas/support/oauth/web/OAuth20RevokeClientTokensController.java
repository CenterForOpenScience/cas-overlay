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
import org.apache.http.HttpStatus;
import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This controller handles requests to revoke all tokens associated with a client id.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public final class OAuth20RevokeClientTokensController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20RevokeClientTokensController.class);

    private final CentralOAuthService centralOAuthService;

    /**
     * Instantiates a new o auth20 revoke client tokens controller.
     *
     * @param centralOAuthService the central oauth service
     */
    public OAuth20RevokeClientTokensController(final CentralOAuthService centralOAuthService) {
        this.centralOAuthService = centralOAuthService;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_ID, clientId);

        final String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_SECRET, "************");

        if (!verifyRequest(clientId, clientSecret)) {
            LOGGER.error("Could not validate request parameters");
            return OAuthUtils.writeText(response, null, HttpStatus.SC_BAD_REQUEST);
        }

        if (!centralOAuthService.revokeClientTokens(clientId, clientSecret)) {
            LOGGER.error("Could not revoke client tokens, mismatched client id or client secret");
            return OAuthUtils.writeText(response, null, HttpStatus.SC_BAD_REQUEST);
        }

        return OAuthUtils.writeText(response, null, HttpStatus.SC_NO_CONTENT);
    }

    /**
     * Verify the request by reviewing the values of client id, etc...
     *
     * @param clientId the client id
     * @param clientSecret the client secret
     * @return true, if successful
     */
    private boolean verifyRequest(final String clientId, final String clientSecret) {
        // clientId is required
        if (StringUtils.isBlank(clientId)) {
            LOGGER.error("Missing {}", OAuthConstants.CLIENT_ID);
            return false;
        }
        // clientSecret is required
        if (StringUtils.isBlank(clientSecret)) {
            LOGGER.error("Missing {}", OAuthConstants.CLIENT_SECRET);
            return false;
        }

        return true;
    }
}
