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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpStatus;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.InvalidTokenException;
import org.jasig.cas.support.oauth.token.TokenType;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.validation.Assertion;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class tests the {@link OAuth20ProfileController} class.
 *
 * @author Jerome Leleu
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuth20ProfileControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String ID = "1234";

    private static final String AT_ID = "AT-1";

    private static final String NAME = "attributeName";

    private static final String NAME2 = "attributeName2";

    private static final String VALUE = "attributeValue";

    private static final String CONTENT_TYPE = "application/json";

    @Test
    public void verifyNoAccessToken() throws Exception {

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("GET", CONTEXT + OAuthConstants.PROFILE_URL);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();
        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST
                + "\",\"error_description\":\"" + "Invalid or missing parameter 'access_token'\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyNoTokenAndAuthHeaderIsMalformed() throws Exception {

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("GET", CONTEXT + OAuthConstants.PROFILE_URL);
        mockRequest.addHeader("Authorization", "Let me in i am authorized");

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST
                + "\",\"error_description\":\"" + "Invalid or missing parameter 'access_token'\"}";

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyInvalidAccessToken() throws Exception {

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(AT_ID, AccessToken.class)).thenThrow(new InvalidTokenException("error"));
        when(centralOAuthService.getPersonalAccessToken(AT_ID)).thenReturn(null);

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("GET", CONTEXT + OAuthConstants.PROFILE_URL);
        mockRequest.setParameter(OAuthConstants.ACCESS_TOKEN, AT_ID);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();
        final String expected = "{\"error\":\"" + OAuthConstants.UNAUTHORIZED_REQUEST
                + "\",\"error_description\":\"" + OAuthConstants.INVALID_ACCESS_TOKEN_DESCRIPTION + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyInvalidValidateServiceTicket() throws Exception {

        final TicketGrantingTicket ticketGrantingTicket = mock(TicketGrantingTicket.class);
        when(ticketGrantingTicket.isExpired()).thenReturn(false);

        final Service service = new SimpleWebApplicationServiceImpl("id");

        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);
        when(accessToken.getType()).thenReturn(TokenType.ONLINE);
        when(accessToken.getService()).thenReturn(service);
        when(accessToken.getTicketGrantingTicket()).thenReturn(ticketGrantingTicket);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(AT_ID, AccessToken.class)).thenReturn(accessToken);

        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        when(serviceTicket.getId()).thenReturn(ID);
        when(serviceTicket.getService()).thenReturn(service);

        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        when(centralAuthenticationService.grantServiceTicket(
                accessToken.getTicketGrantingTicket().getId(),
                accessToken.getService()
        )).thenReturn(serviceTicket);
        when(centralAuthenticationService.validateServiceTicket(
                serviceTicket.getId(),
                serviceTicket.getService()
        )).thenThrow(new InvalidTicketException("expired ticket"));

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("GET", CONTEXT + OAuthConstants.PROFILE_URL);
        mockRequest.setParameter(OAuthConstants.ACCESS_TOKEN, AT_ID);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.setCentralAuthenticationService(centralAuthenticationService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"error\":\"" + OAuthConstants.UNAUTHORIZED_REQUEST
                + "\",\"error_description\":\"" + OAuthConstants.INVALID_ACCESS_TOKEN_DESCRIPTION + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyOK() throws Exception {

        final TicketGrantingTicket ticketGrantingTicket = mock(TicketGrantingTicket.class);
        when(ticketGrantingTicket.isExpired()).thenReturn(false);

        final Service service = new SimpleWebApplicationServiceImpl("id");

        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);
        when(accessToken.getType()).thenReturn(TokenType.ONLINE);
        when(accessToken.getService()).thenReturn(service);
        when(accessToken.getTicketGrantingTicket()).thenReturn(ticketGrantingTicket);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(AT_ID, AccessToken.class)).thenReturn(accessToken);

        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        when(serviceTicket.getId()).thenReturn(ID);
        when(serviceTicket.getService()).thenReturn(service);

        final Map<String, Object> map = new HashMap<>();
        map.put(NAME, VALUE);
        final List<String> list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        final Principal principal = mock(Principal.class);
        when(principal.getId()).thenReturn(ID);
        when(principal.getAttributes()).thenReturn(map);

        final Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        final Assertion assertion = mock(Assertion.class);
        when(assertion.getPrimaryAuthentication()).thenReturn(authentication);

        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        when(centralAuthenticationService.grantServiceTicket(
                accessToken.getTicketGrantingTicket().getId(),
                accessToken.getService()
        )).thenReturn(serviceTicket);
        when(centralAuthenticationService.validateServiceTicket(
                serviceTicket.getId(),
                serviceTicket.getService()
        )).thenReturn(assertion);

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("GET", CONTEXT + OAuthConstants.PROFILE_URL);
        mockRequest.setParameter(OAuthConstants.ACCESS_TOKEN, AT_ID);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.setCentralAuthenticationService(centralAuthenticationService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"id\":\"" + ID + "\",\"attributes\":[{\"" + NAME
                + "\":\"" + VALUE + "\"},{\"" + NAME2 + "\":[\"" + VALUE + "\",\"" + VALUE + "\"]}]}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("id").asText(), receivedObj.get("id").asText());

        final JsonNode expectedAttributes = expectedObj.get("attributes");
        final JsonNode receivedAttributes = receivedObj.get("attributes");

        assertEquals(expectedAttributes.findValue(NAME).asText(), receivedAttributes.findValue(NAME).asText());
        assertEquals(expectedAttributes.findValues(NAME2), receivedAttributes.findValues(NAME2));
    }

    @Test
    public void verifyOKWithAuthorizationHeader() throws Exception {

        final TicketGrantingTicket ticketGrantingTicket = mock(TicketGrantingTicket.class);
        when(ticketGrantingTicket.isExpired()).thenReturn(false);

        final Service service = new SimpleWebApplicationServiceImpl("id");

        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);
        when(accessToken.getType()).thenReturn(TokenType.ONLINE);
        when(accessToken.getService()).thenReturn(service);
        when(accessToken.getTicketGrantingTicket()).thenReturn(ticketGrantingTicket);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(AT_ID, AccessToken.class)).thenReturn(accessToken);

        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        when(serviceTicket.getId()).thenReturn(ID);
        when(serviceTicket.getService()).thenReturn(service);

        final Map<String, Object> map = new HashMap<>();
        map.put(NAME, VALUE);
        final List<String> list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        final Principal principal = mock(Principal.class);
        when(principal.getId()).thenReturn(ID);
        when(principal.getAttributes()).thenReturn(map);

        final Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        final Assertion assertion = mock(Assertion.class);
        when(assertion.getPrimaryAuthentication()).thenReturn(authentication);

        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        when(centralAuthenticationService.grantServiceTicket(
                accessToken.getTicketGrantingTicket().getId(),
                accessToken.getService()
        )).thenReturn(serviceTicket);
        when(centralAuthenticationService.validateServiceTicket(
                serviceTicket.getId(),
                serviceTicket.getService()
        )).thenReturn(assertion);

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("GET", CONTEXT + OAuthConstants.PROFILE_URL);
        mockRequest.addHeader("Authorization", OAuthConstants.BEARER_TOKEN + " " + AT_ID);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.setCentralAuthenticationService(centralAuthenticationService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"id\":\"" + ID + "\",\"attributes\":[{\"" + NAME
                + "\":\"" + VALUE + "\"},{\"" + NAME2 + "\":[\"" + VALUE + "\",\"" + VALUE + "\"]}]}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("id").asText(), receivedObj.get("id").asText());

        final JsonNode expectedAttributes = expectedObj.get("attributes");
        final JsonNode receivedAttributes = receivedObj.get("attributes");

        assertEquals(expectedAttributes.findValue(NAME).asText(), receivedAttributes.findValue(NAME).asText());
        assertEquals(expectedAttributes.findValues(NAME2), receivedAttributes.findValues(NAME2));
    }

    @Test
    public void verifyOKWithScopes() throws Exception {

        final TicketGrantingTicket ticketGrantingTicket = mock(TicketGrantingTicket.class);
        when(ticketGrantingTicket.isExpired()).thenReturn(false);

        final Service service = new SimpleWebApplicationServiceImpl("id");

        final Set<String> scopes = new HashSet<>();
        scopes.add(NAME);
        scopes.add(NAME2);

        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);
        when(accessToken.getType()).thenReturn(TokenType.ONLINE);
        when(accessToken.getService()).thenReturn(service);
        when(accessToken.getTicketGrantingTicket()).thenReturn(ticketGrantingTicket);
        when(accessToken.getScopes()).thenReturn(scopes);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(AT_ID, AccessToken.class)).thenReturn(accessToken);

        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        when(serviceTicket.getId()).thenReturn(ID);
        when(serviceTicket.getService()).thenReturn(service);

        final Principal principal = mock(Principal.class);
        when(principal.getId()).thenReturn(ID);
        when(principal.getAttributes()).thenReturn(new HashMap<>());

        final Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        final Assertion assertion = mock(Assertion.class);
        when(assertion.getPrimaryAuthentication()).thenReturn(authentication);

        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        when(centralAuthenticationService.grantServiceTicket(
                accessToken.getTicketGrantingTicket().getId(),
                accessToken.getService()
        )).thenReturn(serviceTicket);
        when(centralAuthenticationService.validateServiceTicket(
                serviceTicket.getId(),
                serviceTicket.getService()
        )).thenReturn(assertion);

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("GET", CONTEXT + OAuthConstants.PROFILE_URL);
        mockRequest.setParameter(OAuthConstants.ACCESS_TOKEN, AT_ID);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.setCentralAuthenticationService(centralAuthenticationService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final String expected = "{\"id\":\"" + ID + "\",\"scope\":[\"" + NAME + "\",\"" + NAME2 + "\"]}";
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("id").asText(), receivedObj.get("id").asText());
        assertEquals(expectedObj.get("scope").size(), receivedObj.get("scope").size());

        for (final JsonNode expectedNode : expectedObj.get("scope")) {
            Boolean found = Boolean.FALSE;
            for (final JsonNode receivedNode : receivedObj.get("scope")) {
                if (receivedNode.asText().equals(expectedNode.asText())) {
                    found = Boolean.TRUE;
                    break;
                }
            }
            assertEquals(found, Boolean.TRUE);
        }
    }

    @Test
    public void verifyOKWithPersonalToken() throws Exception {

        final Map<String, Object> map = new HashMap<>();
        map.put(NAME, VALUE);
        final List<String> list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        final Principal principal = mock(Principal.class);
        when(principal.getId()).thenReturn(ID);
        when(principal.getAttributes()).thenReturn(map);

        final Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        final TicketGrantingTicket ticketGrantingTicket = mock(TicketGrantingTicket.class);
        when(ticketGrantingTicket.isExpired()).thenReturn(false);
        when(ticketGrantingTicket.getAuthentication()).thenReturn(authentication);

        final Service service = new SimpleWebApplicationServiceImpl("id");

        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);
        when(accessToken.getType()).thenReturn(TokenType.PERSONAL);
        when(accessToken.getService()).thenReturn(service);
        when(accessToken.getTicketGrantingTicket()).thenReturn(ticketGrantingTicket);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(AT_ID, AccessToken.class)).thenReturn(accessToken);

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("GET", CONTEXT + OAuthConstants.PROFILE_URL);
        mockRequest.setParameter(OAuthConstants.ACCESS_TOKEN, AT_ID);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"id\":\"" + ID + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("id").asText(), receivedObj.get("id").asText());
    }

    @Test
    public void verifyOKWithOfflineToken() throws Exception {

        final Service service = new SimpleWebApplicationServiceImpl("id");

        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        when(serviceTicket.getId()).thenReturn(ID);
        when(serviceTicket.getService()).thenReturn(service);

        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);
        when(accessToken.getType()).thenReturn(TokenType.OFFLINE);
        when(accessToken.getServiceTicket()).thenReturn(serviceTicket);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(AT_ID, AccessToken.class)).thenReturn(accessToken);

        final Map<String, Object> map = new HashMap<>();
        map.put(NAME, VALUE);
        final List<String> list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        final Principal principal = mock(Principal.class);
        when(principal.getId()).thenReturn(ID);
        when(principal.getAttributes()).thenReturn(map);

        final Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        final Assertion assertion = mock(Assertion.class);
        when(assertion.getPrimaryAuthentication()).thenReturn(authentication);

        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        when(centralAuthenticationService.validateServiceTicket(serviceTicket.getId(),
                serviceTicket.getService())).thenReturn(assertion);

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("GET", CONTEXT + OAuthConstants.PROFILE_URL);
        mockRequest.setParameter(OAuthConstants.ACCESS_TOKEN, AT_ID);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.setCentralAuthenticationService(centralAuthenticationService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"id\":\"" + ID + "\",\"attributes\":[{\"" + NAME
                + "\":\"" + VALUE + "\"},{\"" + NAME2 + "\":[\"" + VALUE + "\",\"" + VALUE + "\"]}]}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("id").asText(), receivedObj.get("id").asText());

        final JsonNode expectedAttributes = expectedObj.get("attributes");
        final JsonNode receivedAttributes = receivedObj.get("attributes");

        assertEquals(expectedAttributes.findValue(NAME).asText(), receivedAttributes.findValue(NAME).asText());
        assertEquals(expectedAttributes.findValues(NAME2), receivedAttributes.findValues(NAME2));
    }

}
