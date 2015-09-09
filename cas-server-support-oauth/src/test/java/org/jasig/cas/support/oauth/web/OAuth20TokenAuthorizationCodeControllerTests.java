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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.InvalidParameterException;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.AuthorizationCode;
import org.jasig.cas.support.oauth.token.InvalidTokenException;
import org.jasig.cas.support.oauth.token.RefreshToken;
import org.jasig.cas.support.oauth.token.TokenType;
import org.jasig.cas.ticket.ServiceTicket;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

/**
 * This class tests the {@link OAuth20TokenAuthorizationCodeController} class.
 *
 * @author Jerome Leleu
 * @author Michael Haselton
 * @since 3.5.2
 */
public final class OAuth20TokenAuthorizationCodeControllerTests {

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
    public void verifyNoCode() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTHORIZATION_CODE);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("application/json", mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST + "\",\"error_description\":\""
                + new InvalidParameterException(OAuthConstants.CODE).getMessage() + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyNoClientId() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTHORIZATION_CODE);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);

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
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTHORIZATION_CODE);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);

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
    public void verifyNoRedirectUri() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTHORIZATION_CODE);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
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
                + new InvalidParameterException(OAuthConstants.REDIRECT_URI).getMessage() + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyNoAuthorizationCode() throws Exception {
        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(CODE, AuthorizationCode.class)).thenThrow(new InvalidTokenException("error"));

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTHORIZATION_CODE);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("application/json", mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST + "\",\"error_description\":\""
                + OAuthConstants.INVALID_CODE_DESCRIPTION + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyExpiredServiceTicket() throws Exception {
        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        when(serviceTicket.isExpired()).thenReturn(Boolean.TRUE);

        final AuthorizationCode authorizationCode = mock(AuthorizationCode.class);
        when(authorizationCode.getTicket()).thenReturn(serviceTicket);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(CODE, AuthorizationCode.class)).thenReturn(authorizationCode);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTHORIZATION_CODE);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("application/json", mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST + "\",\"error_description\":\""
                + OAuthConstants.INVALID_CLIENT_ID_OR_SECRET_DESCRIPTION + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyNoRegisteredService() throws Exception {
        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        when(serviceTicket.isExpired()).thenReturn(Boolean.FALSE);

        final AuthorizationCode authorizationCode = mock(AuthorizationCode.class);
        when(authorizationCode.getTicket()).thenReturn(serviceTicket);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(CODE, AuthorizationCode.class)).thenReturn(authorizationCode);
        when(centralOAuthService.getRegisteredService(CLIENT_ID)).thenReturn(null);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTHORIZATION_CODE);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("application/json", mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST + "\",\"error_description\":\""
                + OAuthConstants.INVALID_CLIENT_ID_OR_SECRET_DESCRIPTION + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyWrongSecret() throws Exception {
        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        when(serviceTicket.isExpired()).thenReturn(Boolean.FALSE);

        final AuthorizationCode authorizationCode = mock(AuthorizationCode.class);
        when(authorizationCode.getTicket()).thenReturn(serviceTicket);

        final OAuthRegisteredService service = getRegisteredService(REDIRECT_URI, WRONG_CLIENT_SECRET);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(CODE, AuthorizationCode.class)).thenReturn(authorizationCode);
        when(centralOAuthService.getRegisteredService(CLIENT_ID)).thenReturn(service);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTHORIZATION_CODE);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("application/json", mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST + "\",\"error_description\":\""
                + OAuthConstants.INVALID_CLIENT_ID_OR_SECRET_DESCRIPTION + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyRedirectUriDoesNotStartWithServiceId() throws Exception {
        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        when(serviceTicket.isExpired()).thenReturn(Boolean.FALSE);

        final AuthorizationCode authorizationCode = mock(AuthorizationCode.class);
        when(authorizationCode.getTicket()).thenReturn(serviceTicket);

        final OAuthRegisteredService service = getRegisteredService(OTHER_REDIRECT_URI, CLIENT_SECRET);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(CODE, AuthorizationCode.class)).thenReturn(authorizationCode);
        when(centralOAuthService.getRegisteredService(CLIENT_ID)).thenReturn(service);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTHORIZATION_CODE);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("application/json", mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST + "\",\"error_description\":\""
                + OAuthConstants.INVALID_REDIRECT_URI_DESCRIPTION + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyInvalidGrantType() throws Exception {
        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        when(serviceTicket.isExpired()).thenReturn(Boolean.FALSE);

        final AuthorizationCode authorizationCode = mock(AuthorizationCode.class);
        when(authorizationCode.getTicket()).thenReturn(serviceTicket);
        when(authorizationCode.getType()).thenReturn(TokenType.PERSONAL);

        final OAuthRegisteredService service = getRegisteredService(REDIRECT_URI, CLIENT_SECRET);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(CODE, AuthorizationCode.class)).thenReturn(authorizationCode);
        when(centralOAuthService.getRegisteredService(CLIENT_ID)).thenReturn(service);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTHORIZATION_CODE);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("application/json", mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_GRANT + "\",\"error_description\":\""
                + OAuthConstants.INVALID_GRANT_TYPE_DESCRIPTION + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyOfflineOK() throws Exception {
        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        when(serviceTicket.isExpired()).thenReturn(Boolean.FALSE);
        when(serviceTicket.getCreationTime()).thenReturn(new Date().getTime());

        final AuthorizationCode authorizationCode = mock(AuthorizationCode.class);
        when(authorizationCode.getTicket()).thenReturn(serviceTicket);
        when(authorizationCode.getType()).thenReturn(TokenType.OFFLINE);

        final OAuthRegisteredService service = getRegisteredService(REDIRECT_URI, CLIENT_SECRET);

        final RefreshToken refreshToken = mock(RefreshToken.class);
        when(refreshToken.getId()).thenReturn(RT_ID);

        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);
        when(accessToken.getTicket()).thenReturn(serviceTicket);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(CODE, AuthorizationCode.class)).thenReturn(authorizationCode);
        when(centralOAuthService.getRegisteredService(CLIENT_ID)).thenReturn(service);
        when(centralOAuthService.grantOfflineRefreshToken(authorizationCode, REDIRECT_URI)).thenReturn(refreshToken);
        when(centralOAuthService.grantOfflineAccessToken(refreshToken)).thenReturn(accessToken);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTHORIZATION_CODE);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);

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
        final String expected = "{\"token_type\":\"" + OAuthConstants.BEARER_TOKEN + "\",\"expires_in\":\"" + TIMEOUT
                + "\",\"refresh_token\":\"" + RT_ID + "\",\"access_token\":\"" + AT_ID + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("token_type").asText(), receivedObj.get("token_type").asText());
        assertTrue("received expires_at greater or equal to expected",
                expectedObj.get("expires_in").asInt() >= receivedObj.get("expires_in").asInt());
        assertEquals(expectedObj.get("refresh_token").asText(), receivedObj.get("refresh_token").asText());
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
