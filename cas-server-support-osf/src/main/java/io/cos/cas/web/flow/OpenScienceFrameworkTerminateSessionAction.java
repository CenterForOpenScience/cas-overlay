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
        String remotePrincipalId = "osf";
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
            if (auth == null) {
                logger.info("auth == null");
            }

            Principal principal = auth.getPrincipal();
            if (principal == null) {
                logger.info("principal == null");
                return this.eventFactorySupport.error(this);
            }
            logger.info("principal" + principal.toString());

            List<CredentialMetaData>  meta = auth.getCredentials();
            if (meta == null || meta.isEmpty() || meta.size() != 1)  {
                logger.info("meta == empty || null || invalid");
            }
            String credentialId = meta.get(0).getId();
            logger.info("credentialId: " + credentialId);
            if (credentialId.substring(3, 5).equals("@@")) {
                remotePrincipalId = credentialId.substring(0, 3);
            }
            logger.info("redirect: " + context.toString());

            WebUtils.putLogoutRequests(context, this.centralAuthenticationService.destroyTicketGrantingTicket(tgtId));
        }
        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
        this.ticketGrantingTicketCookieGenerator.removeCookie(response);
        this.warnCookieGenerator.removeCookie(response);

        if (remotePrincipalId.equals("osf")) {
            return this.eventFactorySupport.success(this);
        }
        else if (remotePrincipalId.equals("nyu")) {
            String auth_slo_url = "https://shibbolethqa.es.its.nyu.edu/idp/profile/Logout";
            context.getFlowScope().put("logoutRedirectUrl", auth_slo_url);
            return new Event(this, "finish");
        }
        else {
            return this.eventFactorySupport.no(this);
        }
    }
}