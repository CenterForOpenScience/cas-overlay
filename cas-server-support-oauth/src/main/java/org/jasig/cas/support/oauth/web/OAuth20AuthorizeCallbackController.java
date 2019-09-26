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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The OAuth 2.0 authorization callback controller.
 *
 * This controller is called twice. In the first pass, it handles one step of the CAS protocol; in the second one, it
 * handles one step of the OAuth protocol.
 *
 * It is first called after the first authentication step of the CAS protocol after credentials check has passed and a
 * service ticket pending validation has been issued. In this pass, it handles the second step of the CAS protocol:
 * validating the service ticket and granting a ticket granting ticket. One important and interesting thing to note is
 * that this controller acts smartly as both a CAS client and a CAS server in the process. This saves the extra efforts
 * for talking to the primary CAS service as well as preserving session.
 *
 * After successful CAS authentication (both steps done, service ticket removed and ticket granting ticket generated),
 * the controller redirects back to itself to be called a second time. Depending on settings of the registered service
 * and OAuth parameters, it either redirects the user to the callback url for allowing the authorization or the view
 * for asking user to confirm the authorization action.
 *
 * @author Jerome Leleu
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuth20AuthorizeCallbackController extends AbstractController {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20AuthorizeCallbackController.class);

    /** The CAS OAuth authorization service. */
    private final CentralOAuthService centralOAuthService;

    /** The ticket registry for accessing (retrieving and deleting) tickets. */
    private final TicketRegistry ticketRegistry;

    /**
     * Instantiates a new {@link OAuth20AuthorizeCallbackController}.
     *
     * @param centralOAuthService the the CAS OAuth service
     * @param ticketRegistry the ticket registry
     */
    public OAuth20AuthorizeCallbackController(
            final CentralOAuthService centralOAuthService,
            final TicketRegistry ticketRegistry
    ) {
        this.centralOAuthService = centralOAuthService;
        this.ticketRegistry = ticketRegistry;
    }

    @Override
    protected ModelAndView handleRequestInternal(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        // Retrieve the session which stores the current authorization parameters.
        final HttpSession session = request.getSession();

        // Before first pass: the login service ticket is a query parameter the first time this controller is called.
        // There is no ticket granting ticket in the user session. Before second pass: the login service ticket has
        // been removed and a ticket granting ticket has been generated and stored in the user session.

        // Retrieve the login service ticket and determine which pass the controller is called.
        final String serviceTicketId = request.getParameter(OAuthConstants.TICKET);
        LOGGER.debug("{} : {}", OAuthConstants.TICKET, serviceTicketId);

        // Service ticket found in the query parameters of the request, first pass starts.
        if (serviceTicketId != null) {

            // Create the login ticket granting ticket from the service ticket
            final ServiceTicket serviceTicket = (ServiceTicket) ticketRegistry.getTicket(serviceTicketId);
            if (serviceTicket == null || serviceTicket.isExpired()) {
                LOGGER.error("Service Ticket expired : {}", serviceTicketId);
                return OAuthUtils.writeJsonError(
                        response,
                        OAuthConstants.INVALID_GRANT,
                        OAuthConstants.EXPIRED_ST_DESCRIPTION,
                        HttpStatus.SC_BAD_REQUEST
                );
            }
            final TicketGrantingTicket ticketGrantingTicket = serviceTicket.getGrantingTicket();

            // Remove login service ticket.
            ticketRegistry.deleteTicket(serviceTicket.getId());

            // Store the login ticket granting ticket id in the OAuth session.
            session.setAttribute(OAuthConstants.OAUTH20_LOGIN_TICKET_ID, ticketGrantingTicket.getId());

            // Redirects back to itself to start the second pass.
            return OAuthUtils.redirectTo(request.getRequestURL().toString());
        }

        // No service ticket is found, second pass starts.

        // Retrieve the login ticket granting ticket.
        final String ticketGrantingTicketId = (String) session.getAttribute(OAuthConstants.OAUTH20_LOGIN_TICKET_ID);
        LOGGER.debug("{} : {}", OAuthConstants.TICKET, ticketGrantingTicketId);

        // Verify the login ticket granting ticket is still valid.
        final TicketGrantingTicket ticketGrantingTicket
                = (TicketGrantingTicket) ticketRegistry.getTicket(ticketGrantingTicketId);
        if (ticketGrantingTicket == null || ticketGrantingTicket.isExpired()) {
            LOGGER.error("Ticket Granting Ticket expired : {}", ticketGrantingTicketId);
            // Display the error view as we are still interacting w/ the user.
            return OAuthUtils.writeJsonError(response, OAuthConstants.INVALID_GRANT,
                    OAuthConstants.EXPIRED_TGT_DESCRIPTION,
                    HttpStatus.SC_BAD_REQUEST);
        }

        // Build the callback url for allowing the authorization.
        final String callbackUrl = request.getRequestURL().toString().replace(
                "/" + OAuthConstants.CALLBACK_AUTHORIZE_URL,
                "/" + OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL
        );
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
        LOGGER.debug("{} : {}", OAuthConstants.OAUTH20_APPROVAL_PROMPT, approvalPrompt);

        final Boolean bypassApprovalPrompt = (Boolean) session.getAttribute(OAuthConstants.BYPASS_APPROVAL_PROMPT);
        LOGGER.debug("{} : {}", OAuthConstants.BYPASS_APPROVAL_PROMPT, bypassApprovalPrompt);

        final Set<String> requestedScopeSet = new HashSet<>(Arrays.asList(scope.split(" ")));

        // Use a map rather a set for scopes as the OAuth service has the potential to add default scopes(s).
        final Map<String, Scope> scopeMap = centralOAuthService.getScopes(requestedScopeSet);
        session.setAttribute(OAuthConstants.OAUTH20_SCOPE_SET, new HashSet<>(scopeMap.keySet()));

        // Update the callback url with action "allow".
        final String allowCallbackUrl = OAuthUtils.addParameter(
                callbackUrl,
                OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION,
                OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION_ALLOW
        );

        // Final redirect - option 1: Ignore the `OAUTH20_APPROVAL_PROMPT` parameter in session and redirect to the
        // callback URL for allowing the authorization if the `BYPASS_APPROVAL_PROMPT` parameter is set. This override
        // can ONLY be set by the OAuth registered service itself.
        if (bypassApprovalPrompt != null && bypassApprovalPrompt) {
            return OAuthUtils.redirectTo(allowCallbackUrl);
        }

        // Final redirect - option 2: If `OAUTH20_APPROVAL_PROMPT` is automatic, check if we have already approved the
        // requested scopes, if so do not ask the user again for authorization.
        if (StringUtils.isBlank(approvalPrompt)
                || !approvalPrompt.equalsIgnoreCase(OAuthConstants.APPROVAL_PROMPT_FORCE)) {
            final String principalId = ticketGrantingTicket.getAuthentication().getPrincipal().getId();
            final Boolean existingToken = (tokenType == TokenType.ONLINE)
                    ? centralOAuthService.isAccessToken(tokenType, clientId, principalId, scopeMap.keySet())
                    : centralOAuthService.isRefreshToken(clientId, principalId, scopeMap.keySet());

            if (existingToken) {
                return OAuthUtils.redirectTo(allowCallbackUrl);
            }
        }

        // Final redirect - option 3: If `OAUTH20_APPROVAL_PROMPT` is forced or if we haven't previously approved the
        // requested scopes, redirect to the authorization confirmation page for the user to approve the action.
        final Map<String, Object> model = new HashMap<>();
        model.put("callbackUrl", callbackUrl);
        model.put("scopeMap", scopeMap);
        model.put("serviceName", serviceName);
        return new ModelAndView(OAuthConstants.CONFIRM_VIEW, model);
    }
}
