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


import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.jasig.cas.authentication.CredentialMetaData;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.jasig.cas.ticket.TicketGrantingTicket;

public class OpenScienceFrameworkTerminateSessionAction {

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


    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Creates a new instance with the given parameters.
     * @param cas Core business logic object.
     * @param tgtCookieGenerator TGT cookie generator.
     * @param warnCookieGenerator Warn cookie generator.
     */
    public OpenScienceFrameworkTerminateSessionAction(
            final CentralAuthenticationService cas,
            final CookieRetrievingCookieGenerator tgtCookieGenerator,
            final CookieRetrievingCookieGenerator warnCookieGenerator) {
        this.centralAuthenticationService = cas;
        this.ticketGrantingTicketCookieGenerator = tgtCookieGenerator;
        this.warnCookieGenerator = warnCookieGenerator;
    }

    public Event terminate(final RequestContext context) {
        // in login's webflow : we can get the value from context as it has already been stored
        String tgtId = WebUtils.getTicketGrantingTicketId(context);
        String institutionId = "osf";
        Boolean remotePrincipal = Boolean.FALSE;
        // for logout, we need to get the cookie's value
        if (tgtId == null) {
            final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
            tgtId = this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        }
        if (tgtId != null) {
            TicketGrantingTicket TGT = null;
            try {
                TGT  = centralAuthenticationService.getTicket(tgtId, TicketGrantingTicket.class);
            }
            catch (final Exception e) {
                logger.info("TGT error: " + e.toString());
            }

            Authentication auth = TGT.getAuthentication();
            if (auth != null) {
                logger.info("authentication: " + auth.getAttributes().toString());
                institutionId = (String) auth.getAttributes().get("instutionId");
                remotePrincipal = (Boolean) auth.getAttributes().get("remotePrincipal");
            }

            WebUtils.putLogoutRequests(context, this.centralAuthenticationService.destroyTicketGrantingTicket(tgtId));
        }
        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
        this.ticketGrantingTicketCookieGenerator.removeCookie(response);
        this.warnCookieGenerator.removeCookie(response);

        if (remotePrincipal == true) {
            String institutionSloUrl = this.getInstitutionSloUrl(institutionId);
            if (institutionSloUrl != null) {
                context.getFlowScope().put("logoutRedirectUrl", institutionSloUrl);
                return new Event(this, "finish");
            }
        }

        return this.eventFactorySupport.success(this);
    }

    // TODO: move this to somewhere else
    // TODO: talk to mongo database
    private final String getInstitutionSloUrl(String insitutuionId) {
        return "https://shibbolethqa.es.its.nyu.edu/idp/profile/Logout";
    }
}
