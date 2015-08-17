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
import org.jasig.cas.support.oauth.scope.OAuthScope;
import org.jasig.cas.support.oauth.token.RefreshToken;
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

    private final TicketRegistry ticketRegistry;

    private final CentralOAuthService centralOAuthService;

    public OAuth20CallbackAuthorizeController(final TicketRegistry ticketRegistry,
                                              final CentralOAuthService centralOAuthService) {
        this.ticketRegistry = ticketRegistry;
        this.centralOAuthService = centralOAuthService;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final HttpSession session = request.getSession();

        // get cas login service ticket
        final String serviceTicketId = request.getParameter(OAuthConstants.TICKET);
        LOGGER.debug("{} : {}", OAuthConstants.TICKET, serviceTicketId);

        // first time this url is requested the login ticket will be a query parameter
        if (serviceTicketId != null) {
            // create the login ticket granting ticket
            final ServiceTicket serviceTicket = (ServiceTicket) ticketRegistry.getTicket(serviceTicketId);
            // login service ticket should be valid
            if (serviceTicket == null || serviceTicket.isExpired()) {
                LOGGER.error("Service Ticket expired : {}", serviceTicketId);
                return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT, HttpStatus.SC_BAD_REQUEST);
            }

            final TicketGrantingTicket ticketGrantingTicket = serviceTicket.getGrantingTicket();
            // remove login service ticket
            ticketRegistry.deleteTicket(serviceTicket.getId());

            // store the login tgt id in the user's session, used to create service tickets for validation and
            // oauth credentials later in the flow.
            session.setAttribute(OAuthConstants.OAUTH20_LOGIN_TICKET_ID, ticketGrantingTicket.getId());

            // redirect back to self, clears the service ticket from the url, allows the url to be requested multiple
            // times w/o error
            return OAuthUtils.redirectTo(request.getRequestURL().toString());
        }

        // get cas login service ticket from the session
        final String ticketGrantingTicketId = (String) session.getAttribute(OAuthConstants.OAUTH20_LOGIN_TICKET_ID);
        LOGGER.debug("{} : {}", OAuthConstants.TICKET, ticketGrantingTicketId);
        if (StringUtils.isBlank(ticketGrantingTicketId)) {
            LOGGER.error("Missing Ticket Granting Ticket");
            // TODO: display error view since we are still interacting w/ the user.
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT, HttpStatus.SC_BAD_REQUEST);
        }

        // verify the login ticket granting ticket is still valid
        final TicketGrantingTicket ticketGrantingTicket = (TicketGrantingTicket) ticketRegistry.getTicket(ticketGrantingTicketId);
        if (ticketGrantingTicket == null || ticketGrantingTicket.isExpired()) {
            LOGGER.error("Ticket Granting Ticket expired : {}", ticketGrantingTicketId);
            // TODO: display error view since we are still interacting w/ the user.
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT, HttpStatus.SC_BAD_REQUEST);
        }

        final String callbackUrl = request.getRequestURL().toString()
                .replace("/" + OAuthConstants.CALLBACK_AUTHORIZE_URL, "/" + OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL);
        LOGGER.debug("{} : {}", OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL, callbackUrl);

        final String allowCallbackUrl = OAuthUtils.addParameter(callbackUrl, OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION, OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION_ALLOW);

        final Map<String, Object> model = new HashMap<>();
        model.put("callbackUrl", callbackUrl);

        final Boolean bypassApprovalPrompt = (Boolean) session.getAttribute(OAuthConstants.BYPASS_APPROVAL_PROMPT);
        LOGGER.debug("bypassApprovalPrompt : {}", bypassApprovalPrompt);
        if (bypassApprovalPrompt != null && bypassApprovalPrompt) {
            return OAuthUtils.redirectTo(allowCallbackUrl);
        }

        final String clientId = (String) session.getAttribute(OAuthConstants.OAUTH20_CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_CLIENT_ID, clientId);

        @SuppressWarnings("unchecked")
        final Map<String, OAuthScope> scopeMap = (Map<String, OAuthScope>) session.getAttribute(OAuthConstants.OAUTH20_SCOPE_MAP);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_SCOPE_MAP, scopeMap);
        model.put("scopeMap", scopeMap);

        final RefreshToken refreshToken = centralOAuthService.getRefreshToken(clientId, ticketGrantingTicket.getAuthentication().getPrincipal().getId());

        final String approvalPrompt = (String) session.getAttribute(OAuthConstants.OAUTH20_APPROVAL_PROMPT);
        LOGGER.debug("approvalPrompt : {}", approvalPrompt);
        // if not forced approval prompt, no scope changes and existing refresh token, do not ask the user for approval.
        if (StringUtils.isBlank(approvalPrompt) || !approvalPrompt.equalsIgnoreCase(OAuthConstants.APPROVAL_PROMPT_FORCE)) {
            if (refreshToken != null) {
                if (refreshToken.getScope().containsAll(scopeMap.keySet())) {
                    return OAuthUtils.redirectTo(allowCallbackUrl);
                }
            }
        }

        // retrieve service name from session
        final String serviceName = (String) session.getAttribute(OAuthConstants.OAUTH20_SERVICE_NAME);
        LOGGER.debug("serviceName : {}", serviceName);
        model.put("serviceName", serviceName);

        return new ModelAndView(OAuthConstants.CONFIRM_VIEW, model);
    }
}
