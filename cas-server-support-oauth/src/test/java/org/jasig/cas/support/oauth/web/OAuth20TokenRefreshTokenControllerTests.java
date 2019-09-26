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

import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.InvalidParameterException;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.InvalidTokenException;
import org.jasig.cas.support.oauth.token.RefreshToken;
import org.jasig.cas.ticket.TicketGrantingTicket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

/**
 * This class tests the {@link OAuth20TokenRefreshTokenController} class.
 *
 * @author Jerome Leleu
 * @author Michael Haselton
 * @author Fitz Elliott
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuth20TokenRefreshTokenControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String CLIENT_ID = "1";

    private static final String CLIENT_SECRET = "secret";

    private static final String WRONG_CLIENT_SECRET = "wrongSecret";

    private static final String CODE = "ST-1";

    private static final String AT_ID = "AT-1";

    private static final String RT_ID = "RT-1";

    private static final String REDIRECT_URI = "http://someurl";

    private static final String OTHER_REDIRECT_URI = "http://someotherurl";

    private static final int TIMEOUT = 7200;

    @Test
    public void verifyNoRefreshTokenID() throws Exception {

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.REFRESH_TOKEN);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("application/json", mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST + "\",\"error_description\":\""
                + new InvalidParameterException(OAuthConstants.REFRESH_TOKEN).getMessage() + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyNoClientId() throws Exception {

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.REFRESH_TOKEN);
        mockRequest.setParameter(OAuthConstants.REFRESH_TOKEN, RT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("application/json", mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST + "\",\"error_description\":\""
                + new InvalidParameterException(OAuthConstants.CLIENT_ID).getMessage() + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyNoClientSecret() throws Exception {

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.REFRESH_TOKEN);
        mockRequest.setParameter(OAuthConstants.REFRESH_TOKEN, RT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("application/json", mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST + "\",\"error_description\":\""
                + new InvalidParameterException(OAuthConstants.CLIENT_SECRET).getMessage() + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyNoRefreshToken() throws Exception {

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(RT_ID, RefreshToken.class)).thenThrow(new InvalidTokenException("error"));

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.REFRESH_TOKEN);
        mockRequest.setParameter(OAuthConstants.REFRESH_TOKEN, RT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("application/json", mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST
                + "\",\"error_description\":\"" + OAuthConstants.INVALID_REFRESH_TOKEN_DESCRIPTION + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyOK() throws Exception {

        final TicketGrantingTicket ticketGrantingTicket = mock(TicketGrantingTicket.class);
        when(ticketGrantingTicket.getCreationTime()).thenReturn(new Date().getTime());

        final OAuthRegisteredService service = getRegisteredService(REDIRECT_URI, CLIENT_SECRET);

        final RefreshToken refreshToken = mock(RefreshToken.class);
        when(refreshToken.getId()).thenReturn(RT_ID);

        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);
        when(accessToken.getTicket()).thenReturn(ticketGrantingTicket);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(RT_ID, RefreshToken.class)).thenReturn(refreshToken);
        when(centralOAuthService.getRegisteredService(CLIENT_ID)).thenReturn(service);
        when(centralOAuthService.grantOfflineAccessToken(refreshToken)).thenReturn(accessToken);

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.REFRESH_TOKEN);
        mockRequest.setParameter(OAuthConstants.REFRESH_TOKEN, RT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.setTimeout(TIMEOUT);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertEquals("application/json", mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();
        final String expected = "{"
                + "\"token_type\":\"" + OAuthConstants.BEARER_TOKEN + "\","
                + "\"expires_in\":\"" + TIMEOUT + "\","
                + "\"refresh_token\":\"" + RT_ID + "\","
                + "\"access_token\":\"" + AT_ID + "\""
                + "}";

        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("token_type").asText(), receivedObj.get("token_type").asText());
        assertTrue(
                "received expires_at greater or equal to expected",
                expectedObj.get("expires_in").asInt() >= receivedObj.get("expires_in").asInt()
        );
        assertEquals(expectedObj.get("access_token").asText(), receivedObj.get("access_token").asText());
    }

    private OAuthRegisteredService getRegisteredService(final String serviceId, final String secret) {

        final OAuthRegisteredService registeredServiceImpl = new OAuthRegisteredService();

        registeredServiceImpl.setName("The registered service name");
        registeredServiceImpl.setServiceId(serviceId);
        registeredServiceImpl.setClientId(CLIENT_ID);
        registeredServiceImpl.setClientSecret(secret);

        return registeredServiceImpl;
    }
}
