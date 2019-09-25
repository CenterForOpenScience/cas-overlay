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
package org.jasig.cas.support.oauth.authentication.principal;

import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.Credential;

/**
 * OAuth authentication metadata populator.
 *
 * The policy-based authentication manager {@link org.jasig.cas.authentication.PolicyBasedAuthenticationManager} first
 * calls the {@link #supports} method to check whether the credential provided is for the CAS OAuth Service. If so, it
 * then uses the {@link #populateAttributes} method to set the appropriate attributes for the authentication object
 * {@link org.jasig.cas.authentication.ImmutableAuthentication}. Otherwise, the manager moves on to the next metadata
 * populator if there is any.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuthAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final Credential credential) {
        final OAuthCredential c = (OAuthCredential) credential;
        builder.addAttribute(OAuthCredential.AUTHENTICATION_ATTRIBUTE_ACCESS_TYPE, c.getAccessType());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OAuthCredential;
    }
}
