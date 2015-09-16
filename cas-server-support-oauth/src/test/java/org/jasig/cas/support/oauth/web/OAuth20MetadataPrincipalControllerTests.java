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
import org.jasig.cas.support.oauth.metadata.PrincipalMetadata;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.InvalidTokenException;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

/**
 * This class tests the {@link OAuth20MetadataPrincipalController} class.
 *
 * @author Fitz Elliott
 * @since 3.5.2
 */
public final class OAuth20MetadataPrincipalControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String CONTENT_TYPE = "application/json";

    private static final String AT_ID = "AT-1";

    private static final String CLIENT_ID = "client1";

    private static final String PRINC_NAME_ONE = "principal_one";

    private static final String PRINC_DESCR_ONE = "this is principal number one";

    private static final String PRINC_NAME_TWO = "principal_two";

    private static final String PRINC_DESCR_TWO = "this is principal number two";

    @Test
    public void verifyNoTokenOrAuthHeader() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.METADATA_URL);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final String expected = "{\"error\":\"" + OAuthConstants.MISSING_ACCESS_TOKEN + "\",\"error_description\":\""
                + OAuthConstants.MISSING_ACCESS_TOKEN_DESCRIPTION + "\"}";

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }


    @Test
    public void verifyNoTokenAndAuthHeaderIsBlank() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.METADATA_URL);
        mockRequest.addHeader("Authorization", "");
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final String expected = "{\"error\":\"" + OAuthConstants.MISSING_ACCESS_TOKEN + "\",\"error_description\":\""
                + OAuthConstants.MISSING_ACCESS_TOKEN_DESCRIPTION + "\"}";

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyNoTokenAndAuthHeaderIsMalformed() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.METADATA_URL);
        mockRequest.addHeader("Authorization", "Let me in i am authorized");
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final String expected = "{\"error\":\"" + OAuthConstants.MISSING_ACCESS_TOKEN + "\",\"error_description\":\""
                + OAuthConstants.MISSING_ACCESS_TOKEN_DESCRIPTION + "\"}";

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

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.PROFILE_URL);
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

        final String expected = "{\"error\":\"" + OAuthConstants.UNAUTHORIZED_REQUEST + "\",\"error_description\":\""
                + OAuthConstants.INVALID_ACCESS_TOKEN_DESCRIPTION + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyOKWithAccessToken() throws Exception {
        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);

        final List<PrincipalMetadata> principalMetas = Arrays.asList(
                new PrincipalMetadata(CLIENT_ID, PRINC_NAME_ONE, PRINC_DESCR_ONE),
                new PrincipalMetadata(CLIENT_ID, PRINC_NAME_TWO, PRINC_DESCR_TWO));

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(AT_ID, AccessToken.class)).thenReturn(accessToken);
        when(centralOAuthService.getPrincipalMetadata(accessToken)).thenReturn(principalMetas);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.METADATA_URL);
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

        final String expected = "{\"data\":["
            + "{\"client_id\":\"" + CLIENT_ID + "\",\"name\":\"" + PRINC_NAME_ONE
            + "\",\"description\":\"" + PRINC_DESCR_ONE + "\",\"scope\":[]},"
            + "{\"client_id\":\"" + CLIENT_ID + "\",\"name\":\"" + PRINC_NAME_TWO
            + "\",\"description\":\"" + PRINC_DESCR_TWO + "\",\"scope\":[]}]}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());

        final JsonNode expectedData = expectedObj.get("data");
        final JsonNode receivedData = receivedObj.get("data");
        assertEquals(expectedData.size(), receivedData.size());

        final JsonNode expectedPrincipalOne = expectedData.get(0);
        final JsonNode receivedPrincipalOne = receivedData.get(0);
        assertEquals(expectedPrincipalOne.get("client_id"), receivedPrincipalOne.get("client_id"));
        assertEquals(expectedPrincipalOne.get("name"), receivedPrincipalOne.get("name"));
        assertEquals(expectedPrincipalOne.get("description"), receivedPrincipalOne.get("description"));
        assertEquals(expectedPrincipalOne.get("scope").size(), receivedPrincipalOne.get("scope").size());

        final JsonNode expectedPrincipalTwo = expectedData.get(1);
        final JsonNode receivedPrincipalTwo = receivedData.get(1);
        assertEquals(expectedPrincipalTwo.get("client_id"), receivedPrincipalTwo.get("client_id"));
        assertEquals(expectedPrincipalTwo.get("name"), receivedPrincipalTwo.get("name"));
        assertEquals(expectedPrincipalTwo.get("description"), receivedPrincipalTwo.get("description"));
        assertEquals(expectedPrincipalTwo.get("scope").size(), receivedPrincipalTwo.get("scope").size());
    }

    @Test
    public void verifyOKWithAuthHeader() throws Exception {
        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);

        final List<PrincipalMetadata> principalMetas = Arrays.asList(
                new PrincipalMetadata(CLIENT_ID, PRINC_NAME_ONE, PRINC_DESCR_ONE),
                new PrincipalMetadata(CLIENT_ID, PRINC_NAME_TWO, PRINC_DESCR_TWO));

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(AT_ID, AccessToken.class)).thenReturn(accessToken);
        when(centralOAuthService.getPrincipalMetadata(accessToken)).thenReturn(principalMetas);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.METADATA_URL);
        mockRequest.addHeader("Authorization", OAuthConstants.BEARER_TOKEN + " " + AT_ID);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"data\":["
            + "{\"client_id\":\"" + CLIENT_ID + "\",\"name\":\"" + PRINC_NAME_ONE
            + "\",\"description\":\"" + PRINC_DESCR_ONE + "\",\"scope\":[]},"
            + "{\"client_id\":\"" + CLIENT_ID + "\",\"name\":\"" + PRINC_NAME_TWO
            + "\",\"description\":\"" + PRINC_DESCR_TWO + "\",\"scope\":[]}]}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());

        final JsonNode expectedData = expectedObj.get("data");
        final JsonNode receivedData = receivedObj.get("data");
        assertEquals(expectedData.size(), receivedData.size());

        final JsonNode expectedPrincipalOne = expectedData.get(0);
        final JsonNode receivedPrincipalOne = receivedData.get(0);
        assertEquals(expectedPrincipalOne.get("client_id"), receivedPrincipalOne.get("client_id"));
        assertEquals(expectedPrincipalOne.get("name"), receivedPrincipalOne.get("name"));
        assertEquals(expectedPrincipalOne.get("description"), receivedPrincipalOne.get("description"));
        assertEquals(expectedPrincipalOne.get("scope").size(), receivedPrincipalOne.get("scope").size());

        final JsonNode expectedPrincipalTwo = expectedData.get(1);
        final JsonNode receivedPrincipalTwo = receivedData.get(1);
        assertEquals(expectedPrincipalTwo.get("client_id"), receivedPrincipalTwo.get("client_id"));
        assertEquals(expectedPrincipalTwo.get("name"), receivedPrincipalTwo.get("name"));
        assertEquals(expectedPrincipalTwo.get("description"), receivedPrincipalTwo.get("description"));
        assertEquals(expectedPrincipalTwo.get("scope").size(), receivedPrincipalTwo.get("scope").size());
    }

    @Test
    public void verifyEmptyPrincipalListOK() throws Exception {
        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);

        final List<PrincipalMetadata> principalMetas = Arrays.asList();

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(AT_ID, AccessToken.class)).thenReturn(accessToken);
        when(centralOAuthService.getPrincipalMetadata(accessToken)).thenReturn(principalMetas);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.METADATA_URL);
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

        final String expected = "{\"data\":[]}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());

        final JsonNode expectedData = expectedObj.get("data");
        final JsonNode receivedData = receivedObj.get("data");
        assertEquals(expectedData.size(), receivedData.size());
        assertEquals(0, receivedData.size());
    }
}
