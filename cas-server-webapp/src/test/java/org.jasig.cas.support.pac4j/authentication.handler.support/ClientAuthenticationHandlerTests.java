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

package org.jasig.cas.support.pac4j.authentication.handler.support;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.security.GeneralSecurityException;

import org.jasig.cas.support.pac4j.test.MockOpenScienceFrameworkInstitutionHandler;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.pac4j.authentication.principal.ClientCredential;
import org.jasig.cas.support.pac4j.test.MockCasClient;
import org.jasig.cas.support.pac4j.test.MockOrcidClient;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.credentials.CasCredentials;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.client.Clients;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.oauth.credentials.OAuthCredentials;
import org.pac4j.oauth.profile.orcid.OrcidProfile;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;


/**
 *This class tests the {@link ClientAuthenticationHandler} class.
 *
 * @author Longze Chen
 * @since 4.1
 */
public final class ClientAuthenticationHandlerTests {

    private static final String CALLBACK_URL = "http://casserver/login";

    private static final String INSTITUTION_CAS_CLIENT = "CasClientInstitution";

    private static final String PROFILE_ID = "0123456789";

    private ClientAuthenticationHandler clientAuthenticationHandler;

    private ClientCredential clientCredential;

    private MockCasClient mockInstitutionCasClient;

    private CasProfile mockCasProfile;

    private MockOrcidClient mockOrcidClient;

    private OrcidProfile mockOrcidProfile;


    @Before
    public void setUp() {

        // mock institution handler
        final MockOpenScienceFrameworkInstitutionHandler institutionHandler = new MockOpenScienceFrameworkInstitutionHandler();

        // mock institution cas client
        mockInstitutionCasClient = new MockCasClient();
        mockInstitutionCasClient.setClientName(INSTITUTION_CAS_CLIENT);
        mockInstitutionCasClient.setCasProtocol(CasClient.CasProtocol.SAML);

        // mock orcid client
        mockOrcidClient = new MockOrcidClient();

        // initialize client and authentication handler
        final Clients clients = new Clients(
                CALLBACK_URL,
                mockInstitutionCasClient,
                mockOrcidClient
        );
        clientAuthenticationHandler = new ClientAuthenticationHandler(institutionHandler, clients);

        // mock profile
        mockCasProfile = new CasProfile();
        mockCasProfile.setId(PROFILE_ID);
        mockInstitutionCasClient.setCasProfile(mockCasProfile);
        mockOrcidProfile = new OrcidProfile();
        mockOrcidProfile.setId(PROFILE_ID);
        mockOrcidClient.setOrcidProfile(mockOrcidProfile);

        ExternalContextHolder.setExternalContext(mock(ServletExternalContext.class));
    }

    @Test
    public void verifyPrincipleIdForInstitutionCasClients() throws GeneralSecurityException, PreventedException {

        final Credentials credentials = new CasCredentials(null, mockInstitutionCasClient.getName());
       clientCredential = new ClientCredential(credentials);

        final HandlerResult result = clientAuthenticationHandler.createResult(clientCredential, mockCasProfile);
        final Principal principal = result.getPrincipal();
        assertEquals(INSTITUTION_CAS_CLIENT, principal.getId());
    }

    @Test
    public void verifyPrincipleIdForOrcidClient() throws GeneralSecurityException, PreventedException {

        final Credentials credentials = new OAuthCredentials(null, mockOrcidClient.getName());
        clientCredential = new ClientCredential(credentials);

        final HandlerResult result = this.clientAuthenticationHandler.createResult(clientCredential, mockOrcidProfile);
        final Principal principal = result.getPrincipal();
        assertEquals(OrcidProfile.class.getSimpleName() + "#" + mockOrcidProfile.getId(), principal.getId());
    }
}
