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

package org.jasig.cas.support.pac4j.web.flow;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.cos.cas.adaptors.postgres.handlers.OpenScienceFrameworkInstitutionHandler;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.support.pac4j.test.MockOpenScienceFrameworkInstitutionHandler;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.Clients;
import org.pac4j.oauth.client.OrcidClient;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

/**
 * This class tests the {@link ClientAction} class.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public final class ClientActionTests {

    private static final String OAUTH_CLIENT_ID = "fake_client_id";

    private static final String OAUTH_CLIENT_SECRET = "fake_client_secret";

    private static final String OAUTH_SCOPE = "fake_scope";

    private static final String CALLBACK_URL = "http://casserver/login";

    private static final String SERVICE_URL = "http://service/";

    private static final String THEME = "fake_theme";

    private static final String LOCALE = "en";

    private static final String HTTP_METHOD = "POST";

    /** The login url for a CasClient. */
    private static final String EXTERNAL_CAS_LOGIN_URL = "http://externalcasserver/login";

    /** The client name for a CasClient. */
    private static final String INSTITUTION_CAS_CLIENT = "CasClientInstitution";

    @Before
    public void setUp() {
    }

    @Test
    public void verifyStartAuthentication() throws Exception {

        // mock osf institution handler
        final OpenScienceFrameworkInstitutionHandler institutionHandler = new MockOpenScienceFrameworkInstitutionHandler();

        // mock http request
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setParameter(ClientAction.THEME, THEME);
        mockRequest.setParameter(ClientAction.LOCALE, LOCALE);
        mockRequest.setParameter(ClientAction.METHOD, HTTP_METHOD);

        // mock http session
        final MockHttpSession mockSession = new MockHttpSession();
        mockRequest.setSession(mockSession);

        // mock request context
        final ServletExternalContext servletExternalContext = mock(ServletExternalContext.class);
        when(servletExternalContext.getNativeRequest()).thenReturn(mockRequest);
        final MockRequestContext mockRequestContext = new MockRequestContext();
        mockRequestContext.setExternalContext(servletExternalContext);
        mockRequestContext.getFlowScope().put(ClientAction.SERVICE, new SimpleWebApplicationServiceImpl(SERVICE_URL));

        // configure orcid client
        final OrcidClient orcidClient = new OrcidClient(OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET);
        orcidClient.setScope(OAUTH_SCOPE);

        // configure cas client
        final CasClient institutionCasClient = new CasClient();
        institutionCasClient.setName(INSTITUTION_CAS_CLIENT);
        institutionCasClient.setCasProtocol(CasClient.CasProtocol.SAML);
        institutionCasClient.setCasLoginUrl(EXTERNAL_CAS_LOGIN_URL);

        // initialize client action
        final Clients clients = new Clients(CALLBACK_URL, orcidClient, institutionCasClient);
        final ClientAction action = new ClientAction(mock(CentralAuthenticationService.class), institutionHandler, clients);

        final Event event = action.execute(mockRequestContext);
        assertEquals("error", event.getId());
        assertEquals(THEME, mockSession.getAttribute(ClientAction.THEME));
        assertEquals(LOCALE, mockSession.getAttribute(ClientAction.LOCALE));
        assertEquals(HTTP_METHOD, mockSession.getAttribute(ClientAction.METHOD));
        final MutableAttributeMap flowScope = mockRequestContext.getFlowScope();

        final String expectedOrcidLoginUrl = "http://www.orcid.org/oauth/authorize?"
                + "client_id=fake_client_id&scope=fake_scope&response_type=code&redirect_uri=http%3A%2F%2Fcasserver%2Flogin%3F"
                + Clients.DEFAULT_CLIENT_NAME_PARAMETER
                + "%3D"
                + OrcidClient.class.getSimpleName()
                + "#show_login";
        assertEquals(expectedOrcidLoginUrl, flowScope.get("OrcidClientUrl"));

        final String expectedInstitutionLoginUrl = EXTERNAL_CAS_LOGIN_URL
                + "?service=http%3A%2F%2Fcasserver%2Flogin%3F"
                + Clients.DEFAULT_CLIENT_NAME_PARAMETER
                + "%3D"
                + INSTITUTION_CAS_CLIENT;
        assertEquals(expectedInstitutionLoginUrl, flowScope.get(INSTITUTION_CAS_CLIENT + "Url"));
    }
}
