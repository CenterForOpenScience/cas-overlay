/*
 * Copyright (c) 2015. Center for Open Science
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jasig.cas.support.oauth.authentication.handler.support;

import org.jasig.cas.authentication.AbstractAuthenticationHandler;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.support.oauth.authentication.principal.OAuthCredential;

import java.security.GeneralSecurityException;

/**
 * OAuth authentication handler, which wraps the existing user authentication in an OAuth specific credential.
 *
 * The policy-based authentication manager {@link org.jasig.cas.authentication.PolicyBasedAuthenticationManager} first
 * calls the {@link #supports} method to check whether the credential provided is for the CAS OAuth Service. If so, it
 * then uses the {@link #authenticate} method to perform the authentication. Otherwise, the manager simply moves on to
 * the next authentication handler if there is any.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuthCredentialsAuthenticationHandler extends AbstractAuthenticationHandler {

    @Override
    public HandlerResult authenticate(final Credential credential) throws GeneralSecurityException {
        final OAuthCredential c = (OAuthCredential) credential;

        return new DefaultHandlerResult(
                this,
                new BasicCredentialMetaData(credential),
                this.principalFactory.createPrincipal(c.getId(), c.getAttributes())
        );
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OAuthCredential;
    }
}
