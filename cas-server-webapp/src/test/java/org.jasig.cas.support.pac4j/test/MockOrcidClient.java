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

package org.jasig.cas.support.pac4j.test;

import org.pac4j.core.context.WebContext;
import org.pac4j.oauth.client.OrcidClient;
import org.pac4j.oauth.credentials.OAuthCredentials;
import org.pac4j.oauth.profile.orcid.OrcidProfile;


/**
 * Mock class for the OrcidClient.
 *
 * @author Longze Chen
 * @since 4.1
 */
public class MockOrcidClient extends OrcidClient {

    private static final String CLIENT_NAME = "mockOrcidClient";

    private OrcidProfile orcidProfile;

    @Override
    protected void internalInit() {
    }

    @Override
    public String getName() {
        return CLIENT_NAME;
    }

    @Override
    protected OAuthCredentials retrieveCredentials(final WebContext context) {
        return new OAuthCredentials("fakeVerifier", getName());
    }

    @Override
    protected OrcidProfile retrieveUserProfile(final OAuthCredentials credentials, final WebContext context) {
        return orcidProfile;
    }

    public OrcidProfile getOrcidProfile() {
        return orcidProfile;
    }

    public void setOrcidProfile(final OrcidProfile orcidProfile) {
        this.orcidProfile = orcidProfile;
    }
}
