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
package org.jasig.cas.support.oauth.metadata;

import java.util.HashSet;
import java.util.Set;

/**
 * Principal Metadata.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public class PrincipalMetadata {
    private final String clientId;
    private final String name;
    private final String description;
    private final Set<String> scopes = new HashSet<>();

    /**
     * Constructs a new principal metadata class.
     *
     * @param clientId the client id.
     * @param name the name.
     * @param description the description.
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
