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
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketCreationException;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.jasig.cas.validation.Assertion;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class tests the {@link OAuth20ServiceValidateController} class.
 *
 * @author Fitz Elliott
 * @since 3.5.2
 */
public final class OAuth20ServiceValidateControllerTests {

    public static final String URL = "/p3/serviceValidate";

    public static final String SERVICE_TICKET_ID = "ST-1";

    public static final String AT_ID = "AT-1";

    public static final String SCOPE1 = "user1";

    public static final String SCOPE2 = "user2";

    public static final String SUCCESS_VIEW = "cas2ServiceSuccessView";

    @Test
    public void verifyHandleInternal() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", URL);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final OAuth20ServiceValidateController oauth20ServiceValidateController = new OAuth20ServiceValidateController();
        final ModelAndView modelAndView = oauth20ServiceValidateController.handleRequestInternal(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertEquals("", mockResponse.getContentAsString());
    }

    @Test
    public void verifyGetTicketException() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", URL);

        final WebApplicationService webApplicationService = mock(WebApplicationService.class);
        when(webApplicationService.getArtifactId()).thenReturn(SERVICE_TICKET_ID);

        final ArgumentExtractor argumentExtractor = mock(ArgumentExtractor.class);
        when(argumentExtractor.extractService(mockRequest)).thenReturn(webApplicationService);

        final TicketGrantingTicket ticketGrantingTicket = mock(TicketGrantingTicket.class);

        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        when(serviceTicket.getGrantingTicket()).thenReturn(ticketGrantingTicket);
        when(serviceTicket.getService()).thenReturn(webApplicationService);

        final Assertion assertion = mock(Assertion.class);

        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        when(centralAuthenticationService.getTicket(SERVICE_TICKET_ID, Ticket.class)).thenThrow(new InvalidTicketException("weak"));
        when(centralAuthenticationService.validateServiceTicket(SERVICE_TICKET_ID, webApplicationService)).thenReturn(assertion);


        final Set<String> scopes = new HashSet<>();
        scopes.add(SCOPE1);
        scopes.add(SCOPE2);

        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);
        when(accessToken.getScopes()).thenReturn(scopes);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.grantCASAccessToken(ticketGrantingTicket, webApplicationService)).thenReturn(accessToken);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final OAuth20ServiceValidateController oauth20ServiceValidateController = new OAuth20ServiceValidateController();
        oauth20ServiceValidateController.setArgumentExtractor(argumentExtractor);
        oauth20ServiceValidateController.setCentralAuthenticationService(centralAuthenticationService);
        oauth20ServiceValidateController.setCentralOAuthService(centralOAuthService);
        oauth20ServiceValidateController.setSuccessView(SUCCESS_VIEW);
        final ModelAndView modelAndView = oauth20ServiceValidateController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertEquals("", mockResponse.getContentAsString());

        final Map<String, Object> model = modelAndView.getModel();
        assertTrue(!model.containsKey(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN));
        assertTrue(!model.containsKey(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN_SCOPE));
    }

    @Test
    public void verifyOK() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", URL);

        final WebApplicationService webApplicationService = mock(WebApplicationService.class);
        when(webApplicationService.getArtifactId()).thenReturn(SERVICE_TICKET_ID);

        final ArgumentExtractor argumentExtractor = mock(ArgumentExtractor.class);
        when(argumentExtractor.extractService(mockRequest)).thenReturn(webApplicationService);

        final TicketGrantingTicket ticketGrantingTicket = mock(TicketGrantingTicket.class);

        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        when(serviceTicket.getGrantingTicket()).thenReturn(ticketGrantingTicket);
        when(serviceTicket.getService()).thenReturn(webApplicationService);

        final Assertion assertion = mock(Assertion.class);

        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        when(centralAuthenticationService.getTicket(SERVICE_TICKET_ID, Ticket.class)).thenReturn(serviceTicket);
        when(centralAuthenticationService.validateServiceTicket(SERVICE_TICKET_ID, webApplicationService)).thenReturn(assertion);

        final Set<String> scopes = new HashSet<>();
        scopes.add(SCOPE1);
        scopes.add(SCOPE2);

        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);
        when(accessToken.getScopes()).thenReturn(scopes);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.grantCASAccessToken(ticketGrantingTicket, webApplicationService)).thenReturn(accessToken);

        final WebApplicationContext webApplicationContext = mock(WebApplicationContext.class);

        final OAuth20ServiceValidateController oauth20ServiceValidateController = new OAuth20ServiceValidateController();
        oauth20ServiceValidateController.initApplicationContext(webApplicationContext);
        oauth20ServiceValidateController.setArgumentExtractor(argumentExtractor);
        oauth20ServiceValidateController.setCentralAuthenticationService(centralAuthenticationService);
        oauth20ServiceValidateController.setCentralOAuthService(centralOAuthService);
        oauth20ServiceValidateController.setSuccessView(SUCCESS_VIEW);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final ModelAndView modelAndView = oauth20ServiceValidateController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertEquals("", mockResponse.getContentAsString());

        final Map<String, Object> model = modelAndView.getModel();
        assertEquals(AT_ID, model.get(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN));
        assertEquals(scopes, model.get(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN_SCOPE));
    }

    @Test
    public void verifyBypassCASWithNoService() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", URL);

        final WebApplicationService webApplicationService = mock(WebApplicationService.class);
        when(webApplicationService.getArtifactId()).thenReturn(SERVICE_TICKET_ID);

        final ArgumentExtractor argumentExtractor = mock(ArgumentExtractor.class);
        when(argumentExtractor.extractService(mockRequest)).thenReturn(null);

        final TicketGrantingTicket ticketGrantingTicket = mock(TicketGrantingTicket.class);

        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        when(serviceTicket.getGrantingTicket()).thenReturn(ticketGrantingTicket);
        when(serviceTicket.getService()).thenReturn(webApplicationService);

        final Assertion assertion = mock(Assertion.class);

        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        when(centralAuthenticationService.getTicket(SERVICE_TICKET_ID, Ticket.class)).thenReturn(serviceTicket);
        when(centralAuthenticationService.validateServiceTicket(SERVICE_TICKET_ID, webApplicationService)).thenReturn(assertion);

        final Set<String> scopes = new HashSet<>();
        scopes.add(SCOPE1);
        scopes.add(SCOPE2);

        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);
        when(accessToken.getScopes()).thenReturn(scopes);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.grantCASAccessToken(ticketGrantingTicket, webApplicationService)).thenReturn(accessToken);

        final WebApplicationContext webApplicationContext = mock(WebApplicationContext.class);

        final OAuth20ServiceValidateController oauth20ServiceValidateController = new OAuth20ServiceValidateController();
        oauth20ServiceValidateController.initApplicationContext(webApplicationContext);
        oauth20ServiceValidateController.setArgumentExtractor(argumentExtractor);
        oauth20ServiceValidateController.setCentralAuthenticationService(centralAuthenticationService);
        oauth20ServiceValidateController.setCentralOAuthService(centralOAuthService);
        oauth20ServiceValidateController.setSuccessView(SUCCESS_VIEW);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final ModelAndView modelAndView = oauth20ServiceValidateController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertEquals("", mockResponse.getContentAsString());

        final Map<String, Object> model = modelAndView.getModel();
        assertTrue(!model.containsKey(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN));
        assertTrue(!model.containsKey(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN_SCOPE));
    }

    @Test
    public void verifyBypassCASWithNoServiceTicketWrongSuccessView() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", URL);

        final WebApplicationService webApplicationService = mock(WebApplicationService.class);
        when(webApplicationService.getArtifactId()).thenReturn(SERVICE_TICKET_ID);

        final ArgumentExtractor argumentExtractor = mock(ArgumentExtractor.class);
        when(argumentExtractor.extractService(mockRequest)).thenReturn(webApplicationService);

        final TicketGrantingTicket ticketGrantingTicket = mock(TicketGrantingTicket.class);

        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        when(serviceTicket.getGrantingTicket()).thenReturn(ticketGrantingTicket);
        when(serviceTicket.getService()).thenReturn(webApplicationService);

        final Assertion assertion = mock(Assertion.class);

        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        when(centralAuthenticationService.getTicket(SERVICE_TICKET_ID, Ticket.class)).thenReturn(null);
        when(centralAuthenticationService.validateServiceTicket(SERVICE_TICKET_ID, webApplicationService)).thenReturn(assertion);

        final Set<String> scopes = new HashSet<>();
        scopes.add(SCOPE1);
        scopes.add(SCOPE2);

        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);
        when(accessToken.getScopes()).thenReturn(scopes);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.grantCASAccessToken(ticketGrantingTicket, webApplicationService)).thenReturn(accessToken);

        final WebApplicationContext webApplicationContext = mock(WebApplicationContext.class);

        final OAuth20ServiceValidateController oauth20ServiceValidateController = new OAuth20ServiceValidateController();
        oauth20ServiceValidateController.initApplicationContext(webApplicationContext);
        oauth20ServiceValidateController.setArgumentExtractor(argumentExtractor);
        oauth20ServiceValidateController.setCentralAuthenticationService(centralAuthenticationService);
        oauth20ServiceValidateController.setCentralOAuthService(centralOAuthService);
        oauth20ServiceValidateController.setSuccessView(SUCCESS_VIEW);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final ModelAndView modelAndView = oauth20ServiceValidateController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertEquals("", mockResponse.getContentAsString());

        final Map<String, Object> model = modelAndView.getModel();
        assertTrue(!model.containsKey(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN));
        assertTrue(!model.containsKey(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN_SCOPE));
    }

    @Test
    public void verifyBypassCASWithWrongSuccessView() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", URL);

        final WebApplicationService webApplicationService = mock(WebApplicationService.class);
        when(webApplicationService.getArtifactId()).thenReturn(SERVICE_TICKET_ID);

        final ArgumentExtractor argumentExtractor = mock(ArgumentExtractor.class);
        when(argumentExtractor.extractService(mockRequest)).thenReturn(webApplicationService);

        final TicketGrantingTicket ticketGrantingTicket = mock(TicketGrantingTicket.class);

        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        when(serviceTicket.getGrantingTicket()).thenReturn(ticketGrantingTicket);
        when(serviceTicket.getService()).thenReturn(webApplicationService);

        final Assertion assertion = mock(Assertion.class);

        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        when(centralAuthenticationService.getTicket(SERVICE_TICKET_ID, Ticket.class)).thenReturn(serviceTicket);
        when(centralAuthenticationService.validateServiceTicket(SERVICE_TICKET_ID, webApplicationService))
            .thenThrow(new TicketCreationException());

        final Set<String> scopes = new HashSet<>();
        scopes.add(SCOPE1);
        scopes.add(SCOPE2);

        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);
        when(accessToken.getScopes()).thenReturn(scopes);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.grantCASAccessToken(ticketGrantingTicket, webApplicationService)).thenReturn(accessToken);

        final WebApplicationContext webApplicationContext = mock(WebApplicationContext.class);

        final OAuth20ServiceValidateController oauth20ServiceValidateController = new OAuth20ServiceValidateController();
        oauth20ServiceValidateController.initApplicationContext(webApplicationContext);
        oauth20ServiceValidateController.setArgumentExtractor(argumentExtractor);
        oauth20ServiceValidateController.setCentralAuthenticationService(centralAuthenticationService);
        oauth20ServiceValidateController.setCentralOAuthService(centralOAuthService);
        oauth20ServiceValidateController.setSuccessView(SUCCESS_VIEW);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final ModelAndView modelAndView = oauth20ServiceValidateController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertEquals("", mockResponse.getContentAsString());

        final Map<String, Object> model = modelAndView.getModel();
        assertTrue(!model.containsKey(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN));
        assertTrue(!model.containsKey(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN_SCOPE));
    }
}
