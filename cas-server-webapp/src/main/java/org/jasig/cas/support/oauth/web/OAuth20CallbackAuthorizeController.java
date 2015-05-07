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
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * This controller is called after successful authentication and
 * redirects user to the callback url of the OAuth application. A code is
 * added which is the service ticket retrieved from previous authentication.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class OAuth20CallbackAuthorizeController extends AbstractController {

    private final Logger LOGGER = LoggerFactory.getLogger(OAuth20CallbackAuthorizeController.class);

    /** TicketRegistry for storing and retrieving tickets as needed. */
    @NotNull
    private final TicketRegistry ticketRegistry;

    public OAuth20CallbackAuthorizeController(TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        // get CAS ticket
        final String ticket = request.getParameter(OAuthConstants.TICKET);
        LOGGER.debug("{} : {}", OAuthConstants.TICKET, ticket);

        // get the login ticket generating ticket
        final ServiceTicket serviceTicket = (ServiceTicket) ticketRegistry.getTicket(ticket);
        // login service ticket should be valid
        if (serviceTicket == null || serviceTicket.isExpired()) {
            LOGGER.error("Service Ticket expired : {}", ticket);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT, HttpStatus.SC_BAD_REQUEST);
        }
        final TicketGrantingTicket ticketGrantingTicket = serviceTicket.getGrantingTicket();
        // remove login service ticket
        ticketRegistry.deleteTicket(serviceTicket.getId());

        // store the login ticket id in the user's session, used to create service tickets for validation and
        // oauth credentials later in the flow.
        final HttpSession session = request.getSession();
        session.setAttribute(OAuthConstants.OAUTH20_LOGIN_TICKET_ID, ticketGrantingTicket.getId());

        String callbackUrl = request.getRequestURL().toString()
                .replace("/" + OAuthConstants.CALLBACK_AUTHORIZE_URL, "/" + OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL);
        LOGGER.debug("{} : {}", OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL, callbackUrl);

        callbackUrl = OAuthUtils.addParameter(callbackUrl, OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION, OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION_ALLOW);

        final Map<String, Object> model = new HashMap<>();
        model.put("callbackUrl", callbackUrl);

        final Boolean bypassApprovalPrompt = (Boolean) session.getAttribute(OAuthConstants.BYPASS_APPROVAL_PROMPT);
        LOGGER.debug("bypassApprovalPrompt : {}", bypassApprovalPrompt);
        session.removeAttribute(OAuthConstants.BYPASS_APPROVAL_PROMPT);

        // Clients that auto-approve do not need an authorization prompt.
        if (bypassApprovalPrompt != null && bypassApprovalPrompt) {
            return OAuthUtils.redirectTo(callbackUrl);
        }

        // retrieve service name from session
        final String serviceName = (String) session.getAttribute(OAuthConstants.OAUTH20_SERVICE_NAME);
        LOGGER.debug("serviceName : {}", serviceName);
        model.put("serviceName", serviceName);

        return new ModelAndView(OAuthConstants.CONFIRM_VIEW, model);
    }
}
