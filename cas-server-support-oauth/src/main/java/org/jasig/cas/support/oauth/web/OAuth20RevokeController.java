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
import org.jasig.cas.support.oauth.*;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.util.CipherExecutor;
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

    private final CentralAuthenticationService centralAuthenticationService;

    private final CipherExecutor cipherExecutor;

    /**
     * Instantiates a new o auth20 revoke token controller.
     *
     * @param ticketRegistry the ticket registry
     * @param centralAuthenticationService the central authentication service
     * @param cipherExecutor the cipher executor
     */
    public OAuth20RevokeController(final TicketRegistry ticketRegistry,
                                   final CentralAuthenticationService centralAuthenticationService,
                                   final CipherExecutor cipherExecutor) {
        this.ticketRegistry = ticketRegistry;
        this.centralAuthenticationService = centralAuthenticationService;
        this.cipherExecutor = cipherExecutor;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        OAuthToken accessToken = OAuthTokenUtils.getAccessToken(request, cipherExecutor);
        Ticket accessTicket = OAuthTokenUtils.getTicket(centralAuthenticationService, accessToken);

        OAuthToken token = OAuthTokenUtils.getToken(request, cipherExecutor, OAuthConstants.TOKEN);
        Ticket ticket = OAuthTokenUtils.getTicket(centralAuthenticationService, token);

        if (!OAuthTokenUtils.hasPermission(accessTicket, ticket)) {
            LOGGER.debug("Permission denied to access ticket [{}] for ticket [{}]", accessTicket.getId(), ticket.getId());
            throw new TokenInvalidException();
        }

        if (!ticketRegistry.deleteTicket(ticket.getId())) {
            LOGGER.debug("Delete ticket failed [{}]", ticket.getId());
            throw new TokenInvalidException();
        }
        return OAuthUtils.writeText(response, null, HttpStatus.SC_OK);
    }
}
