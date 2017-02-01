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

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.support.pac4j.authentication.principal.ClientCredential;
import org.jasig.cas.support.pac4j.web.flow.ClientAction;
import org.pac4j.core.client.Clients;
import org.pac4j.core.profile.UserProfile;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * Specialized handler which builds the authenticated user directly from the retrieved user profile.
 *
 * @author Jerome Leleu
 * @author Longze Chen
 * @since 4.1.0
 */
public class ClientAuthenticationHandler extends AbstractClientAuthenticationHandler {

    /**
     * Whether to use the typed identifier (by default) or just the identifier.
     */
    private boolean typedIdUsed = true;

    /**
     * Define the clients.
     *
     * @param theClients The clients for authentication
     */
    public ClientAuthenticationHandler(final Clients theClients) {
        super(theClients);
    }

    /**
     * {@inheritDoc}
     *
     * Note on customization:
     * 1.   The default behavior is:
     *          id = isTypedIdUsed() ? profile.getTypedId() : profile.getId();
     *      When typedId is used, the "class name" of the client is used. This works for ORCiD client because there is
     *      only one such client. However, this fails to work with CAS clients since the "class name" cannot identify
     *      different CAS providers. The solution is to use "client name" instead of "class name".
     *          id = clientName + "Profile#" + profile.getId()
     *      For OSF compatibility, only CAS clients use client name while the ORCiD client is not changed
     * 2.   For clients considered as "institution", their authe flow is intercepted and redirected to our
     *      institution login flow after successful auth. The principal id only contains client name and profile id.
     */
    @Override
    protected HandlerResult createResult(final ClientCredential credentials, final UserProfile profile)
            throws GeneralSecurityException, PreventedException {

        final String clientName = credentials.getCredentials().getClientName();
        final String id;

        if (clientName != null) {
            // customize profile names
            if (ClientAction.INSTITUTION_CLIENTS.contains(clientName)) {
                // institution clients are independent of authentication delegation protocol
                id = clientName + '#' + profile.getId();
            } else if (clientName.startsWith("CasClient")) {
                // non-institution cas clients:
                id = clientName + "Profile#" + profile.getId();
                // currently there is no client of this type
                // this requires OSF side implementation (similar to what is done for ORCiD or further generalize it)
                throw new FailedLoginException("Client not supported.");
            } else {
                // other: default behavior
                id = isTypedIdUsed() ? profile.getTypedId() : profile.getId();
            }
            if (StringUtils.isNotBlank(id)) {
                credentials.setUserProfile(profile);
                credentials.setTypedIdUsed(typedIdUsed);
                return new DefaultHandlerResult(
                        this,
                        new BasicCredentialMetaData(credentials),
                        this.principalFactory.createPrincipal(id, profile.getAttributes()));
            }
            throw new FailedLoginException("No identifier found for this user profile: " + profile);
        }
        throw new FailedLoginException("No client name found for this user profile: " + profile);
    }

    public boolean isTypedIdUsed() {
        return typedIdUsed;
    }

    public void setTypedIdUsed(final boolean typedIdUsed) {
        this.typedIdUsed = typedIdUsed;
    }
}
