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

import io.cos.cas.authentication.exceptions.DelegatedLoginException;

import org.apache.commons.lang3.StringUtils;

import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.support.pac4j.authentication.principal.ClientCredential;

import org.pac4j.core.client.Clients;
import org.pac4j.core.profile.UserProfile;

import java.security.GeneralSecurityException;

/**
 * The Client Authentication Handler.
 *
 * This class is a specialized handler which builds the authenticated user directly from the retrieved user profile.
 *
 * @author Jerome Leleu
 * @author Longze Chen
 * @since 4.1.5
 */
public class ClientAuthenticationHandler extends AbstractClientAuthenticationHandler {

    /** Whether to use the typed identifier (default) or just the identifier. */
    private boolean typedIdUsed = true;

    /**
     * Instantiate a new {@link ClientAuthenticationHandler} and define the clients.
     *
     * @param theClients the clients for authentication
     */
    public ClientAuthenticationHandler(final Clients theClients) {
        super(theClients);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HandlerResult createResult(
            final ClientCredential credentials,
            final UserProfile profile
    ) throws GeneralSecurityException {

        final String id = typedIdUsed ? profile.getTypedId() : profile.getId();
        if (StringUtils.isNotBlank(id)) {
            try {
                credentials.setUserProfile(profile);
                credentials.setTypedIdUsed(typedIdUsed);
                return new DefaultHandlerResult(
                        this,
                        new BasicCredentialMetaData(credentials),
                        this.principalFactory.createPrincipal(id, profile.getAttributes()));
            } catch (final Exception e) {
                throw new DelegatedLoginException(e.getMessage());
            }
        }
        throw new DelegatedLoginException("No identifier found for this user profile: " + profile);
    }

    public boolean isTypedIdUsed() {
        return typedIdUsed;
    }

    public void setTypedIdUsed(final boolean typedIdUsed) {
        this.typedIdUsed = typedIdUsed;
    }
}
