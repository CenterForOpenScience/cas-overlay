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


import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.AuthorizationCode;
import org.jasig.cas.support.oauth.token.TokenType;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class tests the {@link OAuth20AuthorizeCallbackActionController} class.
 *
 * @author Fitz Elliott
 * @since 3.5.2
 */


public final class OAuth20AuthorizeCallbackActionControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String AT_ID = "AT-1";

    private static final String AC_ID = "AC-1";

    private static final String TICKET_GRANTING_TICKET_ID = "TGT-1";

    private static final String NAME1 = "scope1";

    private static final String NAME2 = "scope2";

    private static final String SCOPE = NAME1 + " " + NAME2;

    private static final String SERVICE_NAME = "serviceName";

    private static final String CLIENT_ID = "client1";

    private static final String RESPONSE_TYPE = "token";

    private static final String STATE = "foo";

    private static final String REDIRECT_URI = "foo";

    private static final int TIMEOUT = 7200;

    @Test
    public void verifyActionDenied() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(
                "GET", CONTEXT + OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL);
        final MockHttpSession mockSession = new MockHttpSession();
        mockSession.putValue(OAuthConstants.OAUTH20_RESPONSE_TYPE, RESPONSE_TYPE);
        mockSession.putValue(OAuthConstants.OAUTH20_CLIENT_ID, CLIENT_ID);
        mockSession.putValue(OAuthConstants.OAUTH20_STATE, STATE);
        mockSession.putValue(OAuthConstants.OAUTH20_REDIRECT_URI, REDIRECT_URI);
        mockSession.putValue(OAuthConstants.OAUTH20_TOKEN_TYPE, TokenType.OFFLINE);
        mockSession.putValue(OAuthConstants.OAUTH20_LOGIN_TICKET_ID, TICKET_GRANTING_TICKET_ID);
        mockSession.putValue(OAuthConstants.OAUTH20_SCOPE, SCOPE);
        mockRequest.setSession(mockSession);
        mockRequest.setParameter(OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION, "deny");

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertTrue(modelAndView.getView() instanceof RedirectView);
        final RedirectView redirectView = (RedirectView) modelAndView.getView();
        assertTrue(redirectView.getUrl().endsWith(REDIRECT_URI + "?" + OAuthConstants.ERROR + "=" + OAuthConstants.ACCESS_DENIED));

        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_RESPONSE_TYPE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_CLIENT_ID));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_STATE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_REDIRECT_URI));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_TOKEN_TYPE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_LOGIN_TICKET_ID));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_SCOPE_SET));
    }

    @Test
    public void verifyNoClientIdError() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(
                "GET", CONTEXT + OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL);
        final MockHttpSession mockSession = new MockHttpSession();
        mockSession.putValue(OAuthConstants.OAUTH20_RESPONSE_TYPE, RESPONSE_TYPE);
        mockSession.putValue(OAuthConstants.OAUTH20_STATE, STATE);
        mockSession.putValue(OAuthConstants.OAUTH20_REDIRECT_URI, REDIRECT_URI);
        mockSession.putValue(OAuthConstants.OAUTH20_TOKEN_TYPE, TokenType.OFFLINE);
        mockSession.putValue(OAuthConstants.OAUTH20_LOGIN_TICKET_ID, TICKET_GRANTING_TICKET_ID);
        mockSession.putValue(OAuthConstants.OAUTH20_SCOPE, SCOPE);
        mockRequest.setSession(mockSession);
        mockRequest.setParameter(OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION,
                                 OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION_ALLOW);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());

        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_RESPONSE_TYPE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_CLIENT_ID));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_STATE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_REDIRECT_URI));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_TOKEN_TYPE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_LOGIN_TICKET_ID));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_SCOPE_SET));
    }

    @Test
    public void verifyNoRedirectError() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(
                "GET", CONTEXT + OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL);
        final MockHttpSession mockSession = new MockHttpSession();
        mockSession.putValue(OAuthConstants.OAUTH20_RESPONSE_TYPE, RESPONSE_TYPE);
        mockSession.putValue(OAuthConstants.OAUTH20_STATE, STATE);
        mockSession.putValue(OAuthConstants.OAUTH20_CLIENT_ID, CLIENT_ID);
        mockSession.putValue(OAuthConstants.OAUTH20_TOKEN_TYPE, TokenType.OFFLINE);
        mockSession.putValue(OAuthConstants.OAUTH20_LOGIN_TICKET_ID, TICKET_GRANTING_TICKET_ID);
        mockSession.putValue(OAuthConstants.OAUTH20_SCOPE, SCOPE);
        mockRequest.setSession(mockSession);
        mockRequest.setParameter(OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION,
                                 OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION_ALLOW);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());

        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_RESPONSE_TYPE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_CLIENT_ID));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_STATE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_REDIRECT_URI));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_TOKEN_TYPE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_LOGIN_TICKET_ID));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_SCOPE_SET));
    }


    @Test
    public void verifyResponseIsTokenWithState() throws Exception {
        final AuthorizationCode authorizationCode = mock(AuthorizationCode.class);

        final TicketGrantingTicket ticketGrantingTicket = mock(TicketGrantingTicket.class);
        when(ticketGrantingTicket.getCreationTime()).thenReturn(new Date().getTime());

        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);
        when(accessToken.getTicket()).thenReturn(ticketGrantingTicket);

        final Set<String> scopes = new HashSet<>();
        scopes.add(NAME1);
        scopes.add(NAME2);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.grantAuthorizationCode(TokenType.ONLINE, CLIENT_ID, TICKET_GRANTING_TICKET_ID, REDIRECT_URI, scopes))
             .thenReturn(authorizationCode);
        when(centralOAuthService.grantOnlineAccessToken(authorizationCode)).thenReturn(accessToken);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(
                "GET", CONTEXT + OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL);
        final MockHttpSession mockSession = new MockHttpSession();
        mockSession.putValue(OAuthConstants.OAUTH20_RESPONSE_TYPE, RESPONSE_TYPE);
        mockSession.putValue(OAuthConstants.OAUTH20_CLIENT_ID, CLIENT_ID);
        mockSession.putValue(OAuthConstants.OAUTH20_STATE, STATE);
        mockSession.putValue(OAuthConstants.OAUTH20_REDIRECT_URI, REDIRECT_URI);
        mockSession.putValue(OAuthConstants.OAUTH20_LOGIN_TICKET_ID, TICKET_GRANTING_TICKET_ID);
        mockSession.putValue(OAuthConstants.OAUTH20_SCOPE_SET, scopes);
        mockRequest.setSession(mockSession);
        mockRequest.setParameter(OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION,
                                 OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION_ALLOW);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.setTimeout(TIMEOUT);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertTrue(modelAndView.getView() instanceof RedirectView);
        final RedirectView redirectView = (RedirectView) modelAndView.getView();
        assertEquals(redirectView.getUrl(),
            REDIRECT_URI + "#" + OAuthConstants.ACCESS_TOKEN + "=" + accessToken.getId()
                         + "&" + OAuthConstants.EXPIRES_IN   + '=' + TIMEOUT
                         + "&" + OAuthConstants.TOKEN_TYPE   + '=' + OAuthConstants.BEARER_TOKEN
                         + "&" + OAuthConstants.STATE        + '=' + STATE);

        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_RESPONSE_TYPE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_CLIENT_ID));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_STATE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_REDIRECT_URI));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_TOKEN_TYPE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_LOGIN_TICKET_ID));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_SCOPE_SET));
    }

    @Test
    public void verifyResponseIsTokenWithoutState() throws Exception {
        final AuthorizationCode authorizationCode = mock(AuthorizationCode.class);

        final TicketGrantingTicket ticketGrantingTicket = mock(TicketGrantingTicket.class);
        when(ticketGrantingTicket.getCreationTime()).thenReturn(new Date().getTime());

        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);
        when(accessToken.getTicket()).thenReturn(ticketGrantingTicket);

        final Set<String> scopes = new HashSet<>();
        scopes.add(NAME1);
        scopes.add(NAME2);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.grantAuthorizationCode(TokenType.ONLINE, CLIENT_ID, TICKET_GRANTING_TICKET_ID, REDIRECT_URI, scopes))
             .thenReturn(authorizationCode);
        when(centralOAuthService.grantOnlineAccessToken(authorizationCode)).thenReturn(accessToken);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(
                "GET", CONTEXT + OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL);
        final MockHttpSession mockSession = new MockHttpSession();
        mockSession.putValue(OAuthConstants.OAUTH20_RESPONSE_TYPE, RESPONSE_TYPE);
        mockSession.putValue(OAuthConstants.OAUTH20_CLIENT_ID, CLIENT_ID);
        mockSession.putValue(OAuthConstants.OAUTH20_REDIRECT_URI, REDIRECT_URI);
        mockSession.putValue(OAuthConstants.OAUTH20_LOGIN_TICKET_ID, TICKET_GRANTING_TICKET_ID);
        mockSession.putValue(OAuthConstants.OAUTH20_SCOPE_SET, scopes);
        mockRequest.setSession(mockSession);
        mockRequest.setParameter(OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION,
                                 OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION_ALLOW);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.setTimeout(TIMEOUT);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertTrue(modelAndView.getView() instanceof RedirectView);
        final RedirectView redirectView = (RedirectView) modelAndView.getView();
        assertEquals(redirectView.getUrl(),
            REDIRECT_URI + "#" + OAuthConstants.ACCESS_TOKEN + "=" + accessToken.getId()
                         + "&" + OAuthConstants.EXPIRES_IN   + '=' + TIMEOUT
                         + "&" + OAuthConstants.TOKEN_TYPE   + '=' + OAuthConstants.BEARER_TOKEN);

        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_RESPONSE_TYPE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_CLIENT_ID));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_STATE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_REDIRECT_URI));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_TOKEN_TYPE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_LOGIN_TICKET_ID));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_SCOPE_SET));
    }

    @Test
    public void verifyResponseIsCodeWithState() throws Exception {
        final AuthorizationCode authorizationCode = mock(AuthorizationCode.class);
        when(authorizationCode.getId()).thenReturn(AC_ID);

        final Set<String> scopes = new HashSet<>();
        scopes.add(NAME1);
        scopes.add(NAME2);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.grantAuthorizationCode(TokenType.OFFLINE, CLIENT_ID, TICKET_GRANTING_TICKET_ID, REDIRECT_URI, scopes))
             .thenReturn(authorizationCode);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(
                "GET", CONTEXT + OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL);
        final MockHttpSession mockSession = new MockHttpSession();
        mockSession.putValue(OAuthConstants.OAUTH20_CLIENT_ID, CLIENT_ID);
        mockSession.putValue(OAuthConstants.OAUTH20_STATE, STATE);
        mockSession.putValue(OAuthConstants.OAUTH20_REDIRECT_URI, REDIRECT_URI);
        mockSession.putValue(OAuthConstants.OAUTH20_TOKEN_TYPE, TokenType.OFFLINE);
        mockSession.putValue(OAuthConstants.OAUTH20_LOGIN_TICKET_ID, TICKET_GRANTING_TICKET_ID);
        mockSession.putValue(OAuthConstants.OAUTH20_SCOPE_SET, scopes);
        mockRequest.setSession(mockSession);
        mockRequest.setParameter(OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION,
                                 OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION_ALLOW);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertTrue(modelAndView.getView() instanceof RedirectView);
        final RedirectView redirectView = (RedirectView) modelAndView.getView();
        assertEquals(redirectView.getUrl(),
            REDIRECT_URI + "?" + OAuthConstants.CODE + "=" + AC_ID
                         + "&" + OAuthConstants.STATE   + '=' + STATE);

        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_RESPONSE_TYPE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_CLIENT_ID));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_STATE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_REDIRECT_URI));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_TOKEN_TYPE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_LOGIN_TICKET_ID));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_SCOPE_SET));
    }

    @Test
    public void verifyResponseIsCodeWithoutState() throws Exception {
        final AuthorizationCode authorizationCode = mock(AuthorizationCode.class);
        when(authorizationCode.getId()).thenReturn(AC_ID);

        final Set<String> scopes = new HashSet<>();
        scopes.add(NAME1);
        scopes.add(NAME2);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.grantAuthorizationCode(TokenType.OFFLINE, CLIENT_ID, TICKET_GRANTING_TICKET_ID, REDIRECT_URI, scopes))
             .thenReturn(authorizationCode);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(
                "GET", CONTEXT + OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL);
        final MockHttpSession mockSession = new MockHttpSession();
        mockSession.putValue(OAuthConstants.OAUTH20_CLIENT_ID, CLIENT_ID);
        mockSession.putValue(OAuthConstants.OAUTH20_REDIRECT_URI, REDIRECT_URI);
        mockSession.putValue(OAuthConstants.OAUTH20_TOKEN_TYPE, TokenType.OFFLINE);
        mockSession.putValue(OAuthConstants.OAUTH20_LOGIN_TICKET_ID, TICKET_GRANTING_TICKET_ID);
        mockSession.putValue(OAuthConstants.OAUTH20_SCOPE_SET, scopes);
        mockRequest.setSession(mockSession);
        mockRequest.setParameter(OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION,
                                 OAuthConstants.OAUTH20_APPROVAL_PROMPT_ACTION_ALLOW);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertTrue(modelAndView.getView() instanceof RedirectView);
        final RedirectView redirectView = (RedirectView) modelAndView.getView();
        assertEquals(redirectView.getUrl(), REDIRECT_URI + "?" + OAuthConstants.CODE + "=" + AC_ID);

        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_RESPONSE_TYPE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_CLIENT_ID));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_STATE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_REDIRECT_URI));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_TOKEN_TYPE));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_LOGIN_TICKET_ID));
        assertNull(mockSession.getAttribute(OAuthConstants.OAUTH20_SCOPE_SET));
    }
}

