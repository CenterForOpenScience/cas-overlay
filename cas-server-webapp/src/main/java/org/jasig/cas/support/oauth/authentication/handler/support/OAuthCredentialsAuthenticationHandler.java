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
package org.jasig.cas.support.oauth.authentication.handler.support;

import java.security.GeneralSecurityException;

import org.jasig.cas.authentication.AbstractAuthenticationHandler;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.support.oauth.authentication.principal.OAuthCredential;

/**
 * Wraps the existing user authentication in an OAuth specific credential.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public final class OAuthCredentialsAuthenticationHandler extends AbstractAuthenticationHandler {

    @Override
    public HandlerResult authenticate(final Credential credential) throws GeneralSecurityException {
        final OAuthCredential c = (OAuthCredential) credential;

        return new HandlerResult(this, new BasicCredentialMetaData(credential), this.principalFactory.createPrincipal(c.getUsername(), c.getAttributes()));
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OAuthCredential;
    }
}
