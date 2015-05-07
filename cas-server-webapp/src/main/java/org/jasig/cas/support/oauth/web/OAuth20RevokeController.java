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
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This controller handles requests to revoke access and refresh tokens.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public final class OAuth20RevokeController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20RevokeController.class);

    private final TicketRegistry ticketRegistry;

    /**
     * Instantiates a new o auth20 revoke token controller.
     *
     * @param ticketRegistry the ticket registry
     */
    public OAuth20RevokeController(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String token = request.getParameter(OAuthConstants.TOKEN);
        LOGGER.debug("{} : {}", OAuthConstants.TOKEN, token);

        // token must be valid
        if (StringUtils.isBlank(token)) {
            LOGGER.error("Missing {}", OAuthConstants.TOKEN);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_BAD_REQUEST);
        }

        if (!ticketRegistry.deleteTicket(token)) {
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_NOT_FOUND);
        }
        return OAuthUtils.writeText(response, null, HttpStatus.SC_OK);
    }
}
