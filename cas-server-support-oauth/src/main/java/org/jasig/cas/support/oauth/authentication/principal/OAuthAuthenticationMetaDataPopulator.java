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
 * Determines if the credential provided are for OAuth Services and then sets the appropriate
 * Authentication attribute.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public final class OAuthAuthenticationMetaDataPopulator implements
        AuthenticationMetaDataPopulator {

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
