package org.jasig.cas.support.oauth.web;

import org.apache.http.HttpStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.OAuthConstants;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class tests the {@link OAuth20RevokeClienTokensController} class.
 *
 * @author Fitz Elliott
 * @since 3.5.2
 */
public final class OAuth20RevokeClientTokensControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String CLIENT_ID = "1";

    private static final String CLIENT_SECRET = "secret";

    private static final String NO_SUCH_CLIENT_ID = "nope-no-way";

    private static final String WRONG_CLIENT_SECRET = "gorblewharf";

    private static final String CONTENT_TYPE = "application/json";


    @Test
    public void verifyNoClientId() throws Exception {
        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.revokeClientTokens(CLIENT_ID, CLIENT_SECRET)).thenReturn(true);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.REVOKE_URL);
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

    @Test
    public void verifyNoClientSecret() throws Exception {
        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.revokeClientTokens(CLIENT_ID, CLIENT_SECRET)).thenReturn(true);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.REVOKE_URL);
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

    @Test
    public void verifyNoSuchClientId() throws Exception {
        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.revokeClientTokens(CLIENT_ID, CLIENT_SECRET)).thenReturn(true);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.REVOKE_URL);
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

    @Test
    public void verifyWrongClientSecret() throws Exception {
        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.revokeClientTokens(CLIENT_ID, CLIENT_SECRET)).thenReturn(false);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.REVOKE_URL);
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

    @Test
    public void verifyOK() throws Exception {
        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.revokeClientTokens(CLIENT_ID, CLIENT_SECRET)).thenReturn(true);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", CONTEXT
                + OAuthConstants.REVOKE_URL);
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
