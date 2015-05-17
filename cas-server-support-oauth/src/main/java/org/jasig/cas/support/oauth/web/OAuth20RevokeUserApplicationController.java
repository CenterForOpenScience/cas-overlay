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
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.oauth.*;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
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
public final class OAuth20RevokeUserApplicationController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20RevokeUserApplicationController.class);

    private final TicketRegistry ticketRegistry;

    private final CentralAuthenticationService centralAuthenticationService;

    private final CipherExecutor cipherExecutor;

    /**
     * Instantiates a new o auth20 revoke user application controller.
     *
     * @param ticketRegistry the ticket registry
     * @param centralAuthenticationService the central authentication service
     * @param cipherExecutor the cipher executor
     */
    public OAuth20RevokeUserApplicationController(final TicketRegistry ticketRegistry,
                                                  final CentralAuthenticationService centralAuthenticationService,
                                                  final CipherExecutor cipherExecutor) {
        this.ticketRegistry = ticketRegistry;
        this.centralAuthenticationService = centralAuthenticationService;
        this.cipherExecutor = cipherExecutor;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final OAuthToken accessToken = OAuthTokenUtils.getAccessToken(request, cipherExecutor);
        final Ticket accessTicket = OAuthTokenUtils.getTicket(centralAuthenticationService, accessToken);
        final Principal accessPrincipal = OAuthTokenUtils.getAuthentication(accessTicket).getPrincipal();

        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        TicketGrantingTicket refreshTicket = OAuthTokenUtils.getRefreshToken(centralAuthenticationService, clientId, accessPrincipal);
        if (refreshTicket == null) {
            LOGGER.debug("Could not find a Refresh Token for the Client ID [{}]", clientId);
            throw new TokenInvalidException();
        }

        if (!ticketRegistry.deleteTicket(refreshTicket.getId())) {
            LOGGER.debug("Delete ticket failed [{}]", refreshTicket.getId());
            throw new TokenInvalidException();
        }
        return OAuthUtils.writeText(response, null, HttpStatus.SC_NO_CONTENT);
    }
}
