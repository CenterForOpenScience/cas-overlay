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
package org.jasig.cas.support.oauth.metadata;

import java.util.HashSet;
import java.util.Set;

/**
 * Principal metadata.
 *
 * The names of the class and its properties are not descriptive at all (if not confusing). The principal metadata
 * itself contains neither the principal id (i.e. who the user is) nor the authorization id (i.e. the access token).
 * It only contains the service details (the id, name and description of the client and the scopes authorized) of an
 * given authorization of a certain principal.
 *
 * I guess the primary reason that this class was originally designed with the name "Principal Metadata" is that it is
 * only used to retrieve the full metadata about all authorized clients of a given principal in the CAS OAuth Service
 * {@link org.jasig.cas.support.oauth.CentralOAuthServiceImpl#getPrincipalMetadata}.
 *
 * In addition, only an access token of {@link org.jasig.cas.support.oauth.token.TokenType#CAS} is allowed to be used
 * for retrieving the full metadata of a principal. Such a token is issued only during the CAS authentication service
 * validation phase by {@link org.jasig.cas.support.oauth.web.OAuth20ServiceValidateController}.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public class PrincipalMetadata {

    private final String clientId;
    private final String name;
    private final String description;
    private final Set<String> scopes = new HashSet<>();

    /**
     * Constructs a new principal metadata class.
     *
     * @param clientId the client id
     * @param name the client name
     * @param description the client description
     */
    public PrincipalMetadata(final String clientId, final String name, final String description) {
        this.clientId = clientId;
        this.name = name;
        this.description = description;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Set<String> getScopes() {
        return this.scopes;
    }
}
