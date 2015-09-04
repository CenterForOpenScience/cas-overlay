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
import org.jasig.cas.support.oauth.scope.Scope;
import org.jasig.cas.support.oauth.token.TokenType;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This controller is called after successful authentication and
 * redirects user to the callback url of the OAuth application. A code is
 * added which is the service ticket retrieved from previous authentication.
 *
 * @author Jerome Leleu
 * @author Michael Haselton
 * @since 3.5.0
 */
public final class OAuth20AuthorizeCallbackController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20AuthorizeCallbackController.class);

    private final CentralOAuthService centralOAuthService;

    private final TicketRegistry ticketRegistry;

    /**
     * Instantiates a new o auth20 authorize callback controller.
     *
     * @param centralOAuthService the central oauth service
     * @param ticketRegistry the ticket registry
     */
    public OAuth20AuthorizeCallbackController(final CentralOAuthService centralOAuthService, final TicketRegistry ticketRegistry) {
        this.centralOAuthService = centralOAuthService;
        this.ticketRegistry = ticketRegistry;
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
            if (serviceTicket == null || serviceTicket.isExpired()) {
                LOGGER.error("Service Ticket expired : {}", serviceTicketId);
                return OAuthUtils.writeJsonError(response, OAuthConstants.INVALID_GRANT, "Service Ticket expired", HttpStatus.SC_BAD_REQUEST);
            }

            final TicketGrantingTicket ticketGrantingTicket = serviceTicket.getGrantingTicket();

            // remove login service ticket
            ticketRegistry.deleteTicket(serviceTicket.getId());

            // store the login tgt id in the user's session, used to create service tickets for validation and
            // oauth credentials later in the flow
            session.setAttribute(OAuthConstants.OAUTH20_LOGIN_TICKET_ID, ticketGrantingTicket.getId());

            // redirect back to self, clears the service ticket from the url, allows the page to be refreshed w/o error
            return OAuthUtils.redirectTo(request.getRequestURL().toString());
        }

        // get cas login service ticket from the session
        final String ticketGrantingTicketId = (String) session.getAttribute(OAuthConstants.OAUTH20_LOGIN_TICKET_ID);
        LOGGER.debug("{} : {}", OAuthConstants.TICKET, ticketGrantingTicketId);

        // verify the login ticket granting ticket is still valid
        final TicketGrantingTicket ticketGrantingTicket = (TicketGrantingTicket) ticketRegistry.getTicket(ticketGrantingTicketId);
        if (ticketGrantingTicket == null || ticketGrantingTicket.isExpired()) {
            LOGGER.error("Ticket Granting Ticket expired : {}", ticketGrantingTicketId);
            // display error view as we are still interacting w/ the user.
            return OAuthUtils.writeJsonError(response, OAuthConstants.INVALID_GRANT, "Ticket Granting Ticket expired", HttpStatus.SC_BAD_REQUEST);
        }

        final String callbackUrl = request.getRequestURL().toString()
                .replace("/" + OAuthConstants.CALLBACK_AUTHORIZE_URL, "/" + OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL);
        LOGGER.debug("{} : {}", OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL, callbackUrl);

        final String clientId = (String) session.getAttribute(OAuthConstants.OAUTH20_CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_CLIENT_ID, clientId);

        final String scope = (String) session.getAttribute(OAuthConstants.OAUTH20_SCOPE);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_SCOPE, scope);

        final String serviceName = (String) session.getAttribute(OAuthConstants.OAUTH20_SERVICE_NAME);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_SERVICE_NAME, serviceName);

        final TokenType tokenType = (TokenType) session.getAttribute(OAuthConstants.OAUTH20_TOKEN_TYPE);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_TOKEN_TYPE, tokenType);

        final String approvalPrompt = (String) session.getAttribute(OAuthConstants.OAUTH20_APPROVAL_PROMPT);
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_APPROVAL_PROMPT);

        final Boolean bypassApprovalPrompt = (Boolean) session.getAttribute(OAuthConstants.BYPASS_APPROVAL_PROMPT);
        LOGGER.debug("{} : {}", OAuthConstants.BYPASS_APPROVAL_PROMPT, bypassApprovalPrompt);

        final Set<String> requestedScopeSet = new HashSet<>(Arrays.asList(scope.split(" ")));

        // we use the scope map rather than scope set as the oauth service has the potential to add default scopes(s).
        final Map<String, Scope> scopeMap = centralOAuthService.getScopes(requestedScopeSet);
        session.setAttribute(OAuthConstants.OAUTH20_SCOPE_SET, scopeMap.keySet());

        final String allowCallbackUrl = OAuthUtils.addParameter(callbackUrl, OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION,
                OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION_ALLOW);
        // this override can only be set on the service itself
        if (bypassApprovalPrompt != null && bypassApprovalPrompt) {
            return OAuthUtils.redirectTo(allowCallbackUrl);
        }
        // if approval prompt is not forced, check if we have already approved the requested scopes,
        // if so do not ask the user again for authorization.
        if (StringUtils.isBlank(approvalPrompt) || !approvalPrompt.equalsIgnoreCase(OAuthConstants.APPROVAL_PROMPT_FORCE)) {
            final String principalId = ticketGrantingTicket.getAuthentication().getPrincipal().getId();
            final Boolean existingToken = (tokenType == TokenType.ONLINE)
                    ? centralOAuthService.isAccessToken(tokenType, clientId, principalId, scopeMap.keySet())
                    : centralOAuthService.isRefreshToken(clientId, principalId, scopeMap.keySet());

            if (existingToken) {
                return OAuthUtils.redirectTo(allowCallbackUrl);
            }
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("callbackUrl", callbackUrl);
        model.put("scopeMap", scopeMap);
        model.put("serviceName", serviceName);
        return new ModelAndView(OAuthConstants.CONFIRM_VIEW, model);
    }
}
