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

import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.token.TokenType;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the {@link OAuth20AuthorizeController} class.
 *
 * @author Jerome Leleu
 * @author Michael Haselton
 * @since 3.5.2
 */
public final class OAuth20AuthorizeControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String RESPONSE_TYPE = "code";

    private static final String INVALID_RESPONSE_TYPE = "not_code_or_token";

    private static final String CLIENT_ID = "1";

    private static final String NO_SUCH_CLIENT_ID = "nope";

    private static final String REDIRECT_URI = "http://someurl";

    private static final String OTHER_REDIRECT_URI = "http://someotherurl";

    private static final String ACCESS_TYPE = "offline";

    private static final TokenType DEFAULT_ACCESS_TYPE = TokenType.ONLINE;

    private static final String INVALID_ACCESS_TYPE = "not_online_or_offline";

    private static final String UNSUPPORTED_ACCESS_TYPE = "personal";

    private static final String CAS_SERVER = "casserver";

    private static final String CAS_SCHEME = "https";

    private static final int CAS_PORT = 443;

    private static final String CAS_URL = CAS_SCHEME + "://" + CAS_SERVER + ":" + CAS_PORT;

    private static final String SERVICE_NAME = "serviceName";

    private static final String SCOPE = "scope1 scope2";

    private static final String DEFAULT_SCOPE = "";

    private static final String STATE = "state";

    @Test
    public void verifyInvalidResponseType() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.RESPONSE_TYPE, INVALID_RESPONSE_TYPE);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.ACCESS_TYPE, ACCESS_TYPE);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();
        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyNoClientId() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.RESPONSE_TYPE, RESPONSE_TYPE);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.ACCESS_TYPE, ACCESS_TYPE);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyNoRedirectUri() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.RESPONSE_TYPE, RESPONSE_TYPE);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.ACCESS_TYPE, ACCESS_TYPE);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyInvalidAccessType() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.RESPONSE_TYPE, RESPONSE_TYPE);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.ACCESS_TYPE, INVALID_ACCESS_TYPE);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyUnsupportedAccessType() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.RESPONSE_TYPE, RESPONSE_TYPE);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.ACCESS_TYPE, UNSUPPORTED_ACCESS_TYPE);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyNoRegisteredService() throws Exception {
        final OAuthRegisteredService service = getRegisteredService(REDIRECT_URI, CLIENT_ID);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getRegisteredService(CLIENT_ID)).thenReturn(service);
        when(centralOAuthService.getRegisteredService(NO_SUCH_CLIENT_ID)).thenReturn(null);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.RESPONSE_TYPE, RESPONSE_TYPE);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, NO_SUCH_CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.ACCESS_TYPE, ACCESS_TYPE);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyRedirectUriDoesNotStartWithServiceId() throws Exception {
        final OAuthRegisteredService service = getRegisteredService(REDIRECT_URI, CLIENT_ID);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getRegisteredService(CLIENT_ID)).thenReturn(service);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.RESPONSE_TYPE, RESPONSE_TYPE);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, OTHER_REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.ACCESS_TYPE, ACCESS_TYPE);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyOK() throws Exception {
        final OAuthRegisteredService service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getRegisteredService(CLIENT_ID)).thenReturn(service);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.RESPONSE_TYPE, RESPONSE_TYPE);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.ACCESS_TYPE, ACCESS_TYPE);
        mockRequest.setParameter(OAuthConstants.STATE, STATE);
        mockRequest.setParameter(OAuthConstants.SCOPE, SCOPE);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.setLoginUrl(CAS_URL);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertTrue(modelAndView.getView() instanceof RedirectView);
        final RedirectView redirectView = (RedirectView) modelAndView.getView();
        assertTrue(redirectView.getUrl().contains("?service=http"));
        assertTrue(redirectView.getUrl().endsWith(OAuthConstants.CALLBACK_AUTHORIZE_URL));

        final HttpSession session = mockRequest.getSession();
        assertEquals(Boolean.FALSE, session.getAttribute(OAuthConstants.BYPASS_APPROVAL_PROMPT));
        assertEquals(OAuthConstants.APPROVAL_PROMPT_AUTO, session.getAttribute(OAuthConstants.OAUTH20_APPROVAL_PROMPT));
        assertEquals(TokenType.OFFLINE, session.getAttribute(OAuthConstants.OAUTH20_TOKEN_TYPE));
        assertEquals(RESPONSE_TYPE, session.getAttribute(OAuthConstants.OAUTH20_RESPONSE_TYPE));
        assertEquals(CLIENT_ID, session.getAttribute(OAuthConstants.OAUTH20_CLIENT_ID));
        assertEquals(REDIRECT_URI, session.getAttribute(OAuthConstants.OAUTH20_REDIRECT_URI));
        assertEquals(SERVICE_NAME, session.getAttribute(OAuthConstants.OAUTH20_SERVICE_NAME));
        assertEquals(SCOPE, session.getAttribute(OAuthConstants.OAUTH20_SCOPE));
        assertEquals(STATE, session.getAttribute(OAuthConstants.OAUTH20_STATE));
    }

    @Test
    public void verifyDefaultsOK() throws Exception {
        final OAuthRegisteredService service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);

        final CentralOAuthService centralOAuthService = mock(CentralOAuthService.class);
        when(centralOAuthService.getRegisteredService(CLIENT_ID)).thenReturn(service);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.STATE, STATE);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setCentralOAuthService(centralOAuthService);
        oauth20WrapperController.setLoginUrl(CAS_URL);
        oauth20WrapperController.afterPropertiesSet();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertTrue(modelAndView.getView() instanceof RedirectView);
        final RedirectView redirectView = (RedirectView) modelAndView.getView();
        assertTrue(redirectView.getUrl().contains("?service=http"));
        assertTrue(redirectView.getUrl().endsWith(OAuthConstants.CALLBACK_AUTHORIZE_URL));

        final HttpSession session = mockRequest.getSession();
        assertEquals(Boolean.FALSE, session.getAttribute(OAuthConstants.BYPASS_APPROVAL_PROMPT));
        assertEquals(OAuthConstants.APPROVAL_PROMPT_AUTO, session.getAttribute(OAuthConstants.OAUTH20_APPROVAL_PROMPT));
        assertEquals(DEFAULT_ACCESS_TYPE, session.getAttribute(OAuthConstants.OAUTH20_TOKEN_TYPE));
        assertEquals(RESPONSE_TYPE, session.getAttribute(OAuthConstants.OAUTH20_RESPONSE_TYPE));
        assertEquals(CLIENT_ID, session.getAttribute(OAuthConstants.OAUTH20_CLIENT_ID));
        assertEquals(REDIRECT_URI, session.getAttribute(OAuthConstants.OAUTH20_REDIRECT_URI));
        assertEquals(SERVICE_NAME, session.getAttribute(OAuthConstants.OAUTH20_SERVICE_NAME));
        assertEquals(DEFAULT_SCOPE, session.getAttribute(OAuthConstants.OAUTH20_SCOPE));
        assertEquals(STATE, session.getAttribute(OAuthConstants.OAUTH20_STATE));
    }

    private OAuthRegisteredService getRegisteredService(final String serviceId, final String name) {
        final OAuthRegisteredService registeredServiceImpl = new OAuthRegisteredService();

        registeredServiceImpl.setName(name);
        registeredServiceImpl.setServiceId(serviceId);
        registeredServiceImpl.setClientId(CLIENT_ID);

        return registeredServiceImpl;
    }
}
