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

package io.cos.cas.web.flow;

import io.cos.cas.adaptors.postgres.handlers.OpenScienceFrameworkInstitutionHandler;

import org.apache.commons.collections4.Predicate;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.jasig.cas.web.support.WebUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;


/**
 * The Open Science Framework Terminate Session Action.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
public class OpenScienceFrameworkTerminateSessionAction {

    /** The logger instance. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** Webflow event helper component. */
    private final EventFactorySupport eventFactorySupport = new EventFactorySupport();

    /** The CORE to which we delegate for all CAS functionality. */
    @NotNull
    private final CentralAuthenticationService centralAuthenticationService;

    /** CookieGenerator for TGT Cookie. */
    @NotNull
    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    /** CookieGenerator for Warn Cookie. */
    @NotNull
    private final CookieRetrievingCookieGenerator warnCookieGenerator;

    /** Open Science Framework Institution Handler. */
    @NotNull
    private final OpenScienceFrameworkInstitutionHandler institutionHandler;

    /**
     * Creates a new instance with the given parameters.
     * @param cas Core business logic object.
     * @param tgtCookieGenerator TGT cookie generator.
     * @param warnCookieGenerator Warn cookie generator.
     * @param institutionHandler Institution Handler.
     */
    public OpenScienceFrameworkTerminateSessionAction(
            final CentralAuthenticationService cas,
            final CookieRetrievingCookieGenerator tgtCookieGenerator,
            final CookieRetrievingCookieGenerator warnCookieGenerator,
            final OpenScienceFrameworkInstitutionHandler institutionHandler) {
        this.centralAuthenticationService = cas;
        this.ticketGrantingTicketCookieGenerator = tgtCookieGenerator;
        this.warnCookieGenerator = warnCookieGenerator;
        this.institutionHandler = institutionHandler;
    }

    /** A predicate class that selects all active ticket granting tickets that have the same auth principal. */
    public static final class ActiveTicketGrantingTicketPredicate implements Predicate {

        private String principalId;

        /**
         * Instantiate the predicate with the principal ID.
         *
         * @param principalId the principal ID, which is the user's OSF GUID in most cases.
         */
        ActiveTicketGrantingTicketPredicate(final String principalId) {
            this.principalId = principalId;
        }

        @Override
        public boolean evaluate(final Object object) {
            final Ticket ticket = (Ticket) object;
            if (ticket instanceof TicketGrantingTicket) {
                final Authentication auth = ((TicketGrantingTicket) ticket).getAuthentication();
                if (auth != null) {
                    final Principal principal = auth.getPrincipal();
                    return principal != null && this.principalId.equals(principal.getId()) && !ticket.isExpired();
                }
            }
            return false;
        }
    }

    /**
     * The terminate session action. Return `success` or `finish` event.
     * @param context The request context
     * @return Event
     */
    public Event terminate(final RequestContext context) {

        // For the login web flow : obtain the ticket ID from context as it may have already been stored
        String tgtId = WebUtils.getTicketGrantingTicketId(context);
        // For the logout web flow: obtain the ticket ID from the TGC cookie
        if (tgtId == null) {
            final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
            tgtId = this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        }

        // Institution login info
        String institutionId = null;
        Boolean remotePrincipal = Boolean.FALSE;

        if (tgtId != null) {

            TicketGrantingTicket tgt = null;
            try {
                tgt = centralAuthenticationService.getTicket(tgtId, TicketGrantingTicket.class);
            } catch (final Exception e) {
                // ignore
            }

            // Destroy other TGTs that belong to the same authenticated user
            if (tgt != null) {
                final Authentication auth = tgt.getAuthentication();
                if (auth != null) {
                    // Obtain all TGTs that belongs to the same OSF user
                    final String principalId = auth.getPrincipal().getId();
                    final Collection<Ticket> tickets = this.centralAuthenticationService.getTickets(
                            new ActiveTicketGrantingTicketPredicate(principalId)
                    );
                    // Destroy all TGTs except the one for the current session
                    for(final Ticket ticket: tickets) {
                        if (!tgtId.equals(ticket.getId())) {
                            try {
                                this.centralAuthenticationService.destroyTicketGrantingTicket(ticket.getId());
                            } catch (final Exception e) {
                                // ignore
                            }
                        }
                    }
                    // Check if the session to terminate is one authenticated via institution login
                    if (auth.getAttributes().containsKey("institutionId")) {
                        institutionId = (String) auth.getAttributes().get("institutionId");
                    }
                    if (auth.getAttributes().containsKey("remotePrincipal")) {
                        remotePrincipal = (Boolean) auth.getAttributes().get("remotePrincipal");
                    }
                }
            }

            WebUtils.putLogoutRequests(context, this.centralAuthenticationService.destroyTicketGrantingTicket(tgtId));
        }

        // Remove cookies in the response
        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
        this.ticketGrantingTicketCookieGenerator.removeCookie(response);
        this.warnCookieGenerator.removeCookie(response);

        // Redirect to the institution logout endpoint if the session was authenticated via institution auth
        if (remotePrincipal && institutionId != null) {
            final String institutionLogoutUrl = institutionHandler.findInstitutionLogoutUrlById(institutionId);
            if (institutionLogoutUrl == null) {
                logger.warn(
                        "Institution {} does not have a dedicated logout url, use default logout redirection instead",
                        institutionId
                );
            } else {
                context.getFlowScope().put("logoutRedirectUrl", institutionLogoutUrl);
                // Must return the "finish" event to prevent `logoutRedirectUrl` being overwritten
                return new Event(this, "finish");
            }
        }

        // Default return event
        return this.eventFactorySupport.success(this);
    }
}
