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

import io.cos.cas.authentication.handler.OpenScienceFrameworkInstitutionHandler;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.jasig.cas.web.support.WebUtils;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

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

    /**
     * The terminate session action. Return `success` or `finish` event.
     * @param context The request context
     * @return Event
     */
    public Event terminate(final RequestContext context) {
        // in login's webflow : we can get the value from context as it has already been stored
        String tgtId = WebUtils.getTicketGrantingTicketId(context);

        String institutionId = null;
        Boolean remotePrincipal = Boolean.FALSE;

        // for logout, we need to get the cookie's value
        if (tgtId == null) {
            final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
            tgtId = this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        }
        // for institution logout, get the institutionId stored in TGT
        if (tgtId != null) {
            TicketGrantingTicket tgt = null;
            try {
                tgt  = centralAuthenticationService.getTicket(tgtId, TicketGrantingTicket.class);
            } catch (final Exception e) {
                //  ignore
            }
            if (tgt != null) {
                final Authentication auth = tgt.getAuthentication();
                if (auth != null) {
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
        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
        this.ticketGrantingTicketCookieGenerator.removeCookie(response);
        this.warnCookieGenerator.removeCookie(response);

        // if logged in through institutions, redirect to institution logout endpoint
        if (remotePrincipal && institutionId != null) {
            final String institutionLogoutUrl = institutionHandler.findInstitutionLogoutUrlById(institutionId);
            if (institutionLogoutUrl == null) {
                logger.warn("Institution {} does not have a dedicated logout url, use default logout redirection instead", institutionId);
            } else {
                context.getFlowScope().put("logoutRedirectUrl", institutionLogoutUrl);
                // return `finish` event to prevent `logoutRedirectUrl` being overwritten
                return new Event(this, "finish");
            }
        }

        return this.eventFactorySupport.success(this);
    }
}
