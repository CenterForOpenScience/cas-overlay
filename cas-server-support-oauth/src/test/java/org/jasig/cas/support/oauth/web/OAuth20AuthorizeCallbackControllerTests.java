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

import static org.junit.Assert.assertEquals;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.scope.Scope;
import org.jasig.cas.support.oauth.token.TokenType;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anySetOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class tests the {@link OAuth20AuthorizeCallbackController} class.
 *
 * @author Jerome Leleu
 * @author Michael Haselton
 * @since 3.5.2
 */
public final class OAuth20AuthorizeCallbackControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String TICKET_GRANTING_TICKET_ID = "TGT-1";

    private static final String SERVICE_TICKET_ID = "ST-1";

    private static final String PRINCIPAL_ID = "1234";

    private static final String SCOPE = "scope1 scope2";

    private static final String SERVICE_NAME = "serviceName";

    @Test
    public void verifySetupOK() throws Exception {
        final TicketGrantingTicket ticketGrantingTicket = mock(TicketGrantingTicket.class);
        when(ticketGrantingTicket.getId()).thenReturn(TICKET_GRANTING_TICKET_ID);

        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        when(serviceTicket.getGrantingTicket()).thenReturn(ticketGrantingTicket);

        final TicketRegistry ticketRegistry = mock(TicketRegistry.class);
        when(ticketRegistry.getTicket(SERVICE_TICKET_ID)).thenReturn(serviceTicket);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(
                "GET", CONTEXT + OAuthConstants.CALLBACK_AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.TICKET, SERVICE_TICKET_ID);
        final MockHttpSession mockSession = new MockHttpSession();
        mockRequest.setSession(mockSession);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setTicketRegistry(ticketRegistry);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertTrue(modelAndView.getView() instanceof RedirectView);
    }

    @Test
    public void verifyOK() throws Exception {
        final Principal principal = mock(Principal.class);
        when(principal.getId()).thenReturn(PRINCIPAL_ID);

        final Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        final TicketGrantingTicket ticketGrantingTicket = mock(TicketGrantingTicket.class);
        when(ticketGrantingTicket.isExpired()).thenReturn(false);
        when(ticketGrantingTicket.getAuthentication()).thenReturn(authentication);

        final TicketRegistry ticketRegistry = mock(TicketRegistry.class);
        when(ticketRegistry.getTicket(TICKET_GRANTING_TICKET_ID)).thenReturn(ticketGrantingTicket);

        final Map<String, Scope> scopeMap = new HashMap<>();
        scopeMap.put("scope1", new Scope("scope1", "description2"));
        scopeMap.put("scope2", new Scope("scope2", "description2"));

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getScopes(anySetOf(String.class))).thenReturn(scopeMap);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(
                "GET", CONTEXT + OAuthConstants.CALLBACK_AUTHORIZE_URL);
        final MockHttpSession mockSession = new MockHttpSession();
        mockSession.putValue(OAuthConstants.OAUTH20_LOGIN_TICKET_ID, TICKET_GRANTING_TICKET_ID);
        mockSession.putValue(OAuthConstants.OAUTH20_SCOPE, SCOPE);
        mockSession.putValue(OAuthConstants.OAUTH20_SERVICE_NAME, SERVICE_NAME);
        mockSession.putValue(OAuthConstants.OAUTH20_TOKEN_TYPE, TokenType.OFFLINE);
        mockSession.putValue(OAuthConstants.OAUTH20_APPROVAL_PROMPT, OAuthConstants.APPROVAL_PROMPT_FORCE);
        mockRequest.setSession(mockSession);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setTicketRegistry(ticketRegistry);
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.CONFIRM_VIEW, modelAndView.getViewName());

        final Map<String, Object> map = modelAndView.getModel();
        assertEquals(SERVICE_NAME, map.get("serviceName"));
        assertEquals(scopeMap.hashCode(), map.get("scopeMap").hashCode());
    }
}
