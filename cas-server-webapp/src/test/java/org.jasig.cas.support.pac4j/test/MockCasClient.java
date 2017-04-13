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

import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.credentials.CasCredentials;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.RequiresHttpAction;

/**
 * Mock class for the CasClient.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class MockCasClient extends CasClient {

    private String clientName = "mockCasClient";

    private CasProfile casProfile;

    public void setClientName(final String clientName) {
        this.clientName = clientName;
    }

    @Override
    protected void internalInit() {
    }

    @Override
    public String getName() {
        return clientName;
    }

    @Override
    protected CasCredentials retrieveCredentials(final WebContext context) throws RequiresHttpAction {
        return new CasCredentials("fakeServiceTicket", this.getName());
    }

    @Override
    protected CasProfile retrieveUserProfile(final CasCredentials credentials, final WebContext context) {
        return this.casProfile;
    }

    public CasProfile getCasProfile() {
        return casProfile;
    }

    public void setCasProfile(final CasProfile casProfile) {
        this.casProfile = casProfile;
    }
}
