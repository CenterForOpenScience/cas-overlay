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
package org.jasig.cas.support.oauth.personal;

import java.util.Set;

/**
 * Personal access token (PAT).
 *
 * This is not a model class but a helper. It stores the token information retrieved from the OSF database, of which
 * the model class is {@link io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkApiOauth2PersonalAccessToken}.
 *
 * The CAS OAuth Service {@link org.jasig.cas.support.oauth.CentralOAuthServiceImpl} uses this information to create
 * its own PAT, of which the model class is {@link org.jasig.cas.support.oauth.token.AccessTokenImpl} and of which the
 * token type {@link org.jasig.cas.support.oauth.token.TokenType} is PERSONAL - 2.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class PersonalAccessToken {

    /** The id of the token. */
    private final String id;

    /** The principal id of the token. */
    private final String principalId;

    /** The scopes assigned to the token. */
    private final Set<String> scopes;

    /**
     * Constructs a new instance of personal access token.
     *
     * @param id the id of the token
     * @param principalId the principal id of the token
     * @param scopes the scopes of the token
     */
    public PersonalAccessToken(final String id, final String principalId, final Set<String> scopes) {
        this.id = id;
        this.principalId = principalId;
        this.scopes = scopes;
    }

    /**
     * Get the identifier of the token.
     *
     * @return the the id of the token
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the principal id of the token.
     *
     * @return the principal id
     */
    public String getPrincipalId() {
        return this.principalId;
    }

    /**
     * Get a set of scopes assigned to the token.
     *
     * @return a set of scopes
     */
    public Set<String> getScopes() {
        return this.scopes;
    }
}
