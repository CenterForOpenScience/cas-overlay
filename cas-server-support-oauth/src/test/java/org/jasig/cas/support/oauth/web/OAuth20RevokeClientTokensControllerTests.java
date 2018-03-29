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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.OAuthConstants;

import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.RefreshToken;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class tests the {@link OAuth20RevokeClienTokensController} class.
 *
 * @author Fitz Elliott
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuth20RevokeClientTokensControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String CLIENT_ID = "1";

    private static final String CLIENT_SECRET = "secret";

    private static final String NO_SUCH_CLIENT_ID = "nope-no-way";

    private static final String WRONG_CLIENT_SECRET = "gorblewharf";

    private static final String CONTENT_TYPE = "application/json";


    /** Test that no client id raises HTTP 400 Bad Request. */
    @Test
    public void verifyNoClientId() throws Exception {
        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.REVOKE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, "");
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST + "\",\"error_description\":\""
                + "Invalid or missing parameter 'client_id'\"}";

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    /** Test that no client secret raises HTTP 400 Bad Request. */
    @Test
    public void verifyNoClientSecret() throws Exception {
        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.REVOKE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, "");
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST + "\",\"error_description\":\""
                + "Invalid or missing parameter 'client_secret'\"}";

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    /** Test that client id not found raises HTTP 400 Bad Request. */
    @Test
    public void verifyNoSuchClientId() throws Exception {
        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.REVOKE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, NO_SUCH_CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST + "\",\"error_description\":\""
                + OAuthConstants.INVALID_CLIENT_ID_OR_SECRET_DESCRIPTION + "\"}";

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    /** Test that wrong client secret raises HTTP 400 Bad Request. */
    @Test
    public void verifyWrongClientSecret() throws Exception {
        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.REVOKE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST + "\",\"error_description\":\""
                + OAuthConstants.INVALID_CLIENT_ID_OR_SECRET_DESCRIPTION + "\"}";

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    /* Test that valid client id and secret succeeds and returns HTTP 204 No Content. */
    @Test
    public void verifyOK() throws Exception {
        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        final Collection<AccessToken> accessTokens = new LinkedList<>();
        final Collection<RefreshToken> refreshTokens = new LinkedList<>();
        when(centralOAuthService.getClientAccessTokens(CLIENT_ID)).thenReturn(accessTokens);
        when(centralOAuthService.getClientRefreshTokens(CLIENT_ID)).thenReturn(refreshTokens);

        final OAuthRegisteredService service = new OAuthRegisteredService();
        service.setClientId(CLIENT_ID);
        service.setClientSecret(CLIENT_SECRET);
        when(centralOAuthService.getRegisteredService(CLIENT_ID)).thenReturn(service);

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.REVOKE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_NO_CONTENT, mockResponse.getStatus());
        assertNull(mockResponse.getContentType());
        assertEquals("null", mockResponse.getContentAsString());
    }
}
