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
import org.jasig.cas.support.oauth.metadata.ClientMetadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

/**
 * This class tests the {@link OAuth20MetadataClientController} class.
 *
 * @author Fitz Elliott
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuth20MetadataClientControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String CLIENT_ID = "1";

    private static final String CLIENT_SECRET = "secret";

    private static final String CLIENT_META_NAME = "Smithington Wellborn";

    private static final String CLIENT_META_DESCRIPTION = "A cad, of course.";

    private static final Integer CLIENT_META_USERS = 2;

    private static final String NO_SUCH_CLIENT_ID = "nope-no-way";

    private static final String WRONG_CLIENT_SECRET = "gorblewharf";

    private static final String CONTENT_TYPE = "application/json";

    private static final ClientMetadata METADATA
        = new ClientMetadata(CLIENT_ID, CLIENT_META_NAME, CLIENT_META_DESCRIPTION, CLIENT_META_USERS);

    @Test
    public void verifyNoClientId() throws Exception {

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getClientMetadata(CLIENT_ID, CLIENT_SECRET)).thenReturn(METADATA);
        
        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.METADATA_URL);
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

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST
                + "\",\"error_description\":\"" + "Invalid or missing parameter 'client_id'\"}";

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyNoClientSecret() throws Exception {

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getClientMetadata(CLIENT_ID, CLIENT_SECRET)).thenReturn(METADATA);
        
        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.METADATA_URL);
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

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST
                + "\",\"error_description\":\"" + "Invalid or missing parameter 'client_secret'\"}";

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyOK() throws Exception {

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getClientMetadata(NO_SUCH_CLIENT_ID, CLIENT_SECRET)).thenReturn(null);
        when(centralOAuthService.getClientMetadata(CLIENT_ID, WRONG_CLIENT_SECRET)).thenReturn(null);
        when(centralOAuthService.getClientMetadata(CLIENT_ID, CLIENT_SECRET)).thenReturn(METADATA);

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.METADATA_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final String expected = "{\"client_id\":\"" + CLIENT_ID + "\",\"name\":\"" + CLIENT_META_NAME
                + "\",\"description\":\"" + CLIENT_META_DESCRIPTION + "\",\"users\":\"" + CLIENT_META_USERS + "\"}";

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("client_id").asText(), receivedObj.get("client_id").asText());
        assertEquals(expectedObj.get("name").asText(), receivedObj.get("name").asText());
        assertEquals(expectedObj.get("description").asText(), receivedObj.get("description").asText());
        assertEquals(expectedObj.get("users").asText(), receivedObj.get("users").asText());
    }

    @Test
    public void verifyNoSuchClientId() throws Exception {

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getClientMetadata(NO_SUCH_CLIENT_ID, CLIENT_SECRET)).thenReturn(null);
        when(centralOAuthService.getClientMetadata(CLIENT_ID, WRONG_CLIENT_SECRET)).thenReturn(null);
        when(centralOAuthService.getClientMetadata(CLIENT_ID, CLIENT_SECRET)).thenReturn(METADATA);

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.METADATA_URL);
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

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST
                + "\",\"error_description\":\"" + OAuthConstants.INVALID_CLIENT_ID_OR_SECRET_DESCRIPTION + "\"}";

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyWrongClientSecret() throws Exception {

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getClientMetadata(NO_SUCH_CLIENT_ID, CLIENT_SECRET)).thenReturn(null);
        when(centralOAuthService.getClientMetadata(CLIENT_ID, WRONG_CLIENT_SECRET)).thenReturn(null);
        when(centralOAuthService.getClientMetadata(CLIENT_ID, CLIENT_SECRET)).thenReturn(METADATA);

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.METADATA_URL);
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

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST
                + "\",\"error_description\":\"" + OAuthConstants.INVALID_CLIENT_ID_OR_SECRET_DESCRIPTION + "\"}";

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

}
