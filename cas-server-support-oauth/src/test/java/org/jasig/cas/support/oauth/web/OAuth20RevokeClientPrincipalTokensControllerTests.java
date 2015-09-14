package org.jasig.cas.support.oauth.web;

import org.apache.http.HttpStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.OAuthConstants;
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

/**
 * This class tests the {@link OAuth20RevokeClientPrincipalTokensController} class.
 *
 * @author Fitz Elliott
 * @since 3.5.2
 */
public final class OAuth20RevokeClientPrincipalTokensControllerTests {

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
                + OAuthConstants.REVOKE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST + "\",\"error_description\":\""
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
                + OAuthConstants.REVOKE_URL);
        mockRequest.addHeader("Authorization", "");
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST + "\",\"error_description\":\""
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
                + OAuthConstants.REVOKE_URL);
        mockRequest.addHeader("Authorization", "Let me in i am authorized");
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST + "\",\"error_description\":\""
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
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
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

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(AT_ID, AccessToken.class)).thenReturn(accessToken);
        when(centralOAuthService.revokeClientPrincipalTokens(accessToken)).thenReturn(true);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.REVOKE_URL);
        mockRequest.setParameter(OAuthConstants.ACCESS_TOKEN, AT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
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

    @Test
    public void verifyOKWithAuthHeader() throws Exception {
        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(AT_ID, AccessToken.class)).thenReturn(accessToken);
        when(centralOAuthService.revokeClientPrincipalTokens(accessToken)).thenReturn(true);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.REVOKE_URL);
        mockRequest.addHeader("Authorization", OAuthConstants.BEARER_TOKEN + " " + AT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
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

    @Test
    public void verifyFailedToRevokeTokens() throws Exception {
        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(AT_ID);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(AT_ID, AccessToken.class)).thenReturn(accessToken);
        when(centralOAuthService.revokeClientPrincipalTokens(accessToken)).thenReturn(false);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.REVOKE_URL);
        mockRequest.setParameter(OAuthConstants.ACCESS_TOKEN, AT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST + "\",\"error_description\":\""
                + OAuthConstants.INVALID_ACCESS_TOKEN_DESCRIPTION + "\"}";

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }
}
