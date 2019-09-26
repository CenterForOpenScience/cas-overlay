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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpStatus;

import org.jasig.cas.support.oauth.InvalidParameterException;
import org.jasig.cas.support.oauth.OAuthConstants;

import org.junit.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * This class tests the {@link OAuth20WrapperController} class.
 *
 * @author Jerome Leleu
 * @author Fitz Elliott
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuth20WrapperControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    @Test
    public void verifyWrongMethod() throws Exception {

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("GET", CONTEXT + "wrongmethod");

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);

        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("text/plain", mockResponse.getContentType());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyNoPostForAuthCtrl() throws Exception {

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.AUTHORIZE_URL);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);

        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("text/plain", mockResponse.getContentType());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyNoPostForAuthCallbackCtrl() throws Exception {

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.CALLBACK_AUTHORIZE_URL);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);

        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("text/plain", mockResponse.getContentType());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyNoPostForAuthCallbackActionCtrl() throws Exception {

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.CALLBACK_AUTHORIZE_ACTION_URL);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);

        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("text/plain", mockResponse.getContentType());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyNoGetForTokenCtrl() throws Exception {

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("GET", CONTEXT + OAuthConstants.TOKEN_URL);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);

        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("text/plain", mockResponse.getContentType());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyNoGrantTypeForTokenCtrl() throws Exception {

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.TOKEN_URL);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);

        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("application/json", mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST
                + "\",\"error_description\":\"" + new InvalidParameterException(OAuthConstants.GRANT_TYPE).getMessage() + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyInvalidGrantTypeForTokenCtrl() throws Exception {

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.TOKEN_URL);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, "banana");

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);

        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("application/json", mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST
                + "\",\"error_description\":\"" + new InvalidParameterException(OAuthConstants.GRANT_TYPE).getMessage() + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyNoGetForRevokeCtrl() throws Exception {

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("GET", CONTEXT + OAuthConstants.REVOKE_URL);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);

        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("text/plain", mockResponse.getContentType());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyNoPostForProfileCtrl() throws Exception {

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("POST", CONTEXT + OAuthConstants.PROFILE_URL);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);

        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("text/plain", mockResponse.getContentType());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyNoGetForProfileCtrl() throws Exception {

        final MockHttpServletRequest mockRequest
                = new MockHttpServletRequest("GET", CONTEXT + OAuthConstants.METADATA_URL);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);

        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("text/plain", mockResponse.getContentType());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }
}
