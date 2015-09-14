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
 * This class tests the {@link OAuth20RevokeTokenController} class.
 *
 * @author Fitz Elliott
 * @since 3.5.2
 */
public final class OAuth20RevokeTokenControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String CONTENT_TYPE = "application/json";

    private static final String TOKEN_ID = "T-1";

    @Test
    public void verifyNoToken() throws Exception {
        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(null)).thenThrow(new InvalidTokenException("error"));

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.REVOKE_URL);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST
            + "\",\"error_description\":\"Invalid Token\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyRevocationFail() throws Exception {
        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(TOKEN_ID);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(TOKEN_ID)).thenReturn(accessToken);
        when(centralOAuthService.revokeToken(accessToken)).thenReturn(false);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.REVOKE_URL);
        mockRequest.setParameter(OAuthConstants.TOKEN, TOKEN_ID);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertNull(modelAndView);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"error\":\"" + OAuthConstants.INVALID_REQUEST
            + "\",\"error_description\":\"" + OAuthConstants.FAILED_TOKEN_REVOCATION_DESCRIPTION + "\"}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("error").asText(), receivedObj.get("error").asText());
        assertEquals(expectedObj.get("error_description").asText(), receivedObj.get("error_description").asText());
    }

    @Test
    public void verifyOK() throws Exception {
        final AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn(TOKEN_ID);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getToken(TOKEN_ID)).thenReturn(accessToken);
        when(centralOAuthService.revokeToken(accessToken)).thenReturn(true);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.REVOKE_URL);
        mockRequest.setParameter(OAuthConstants.TOKEN, TOKEN_ID);
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
