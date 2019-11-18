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

/**
 * Client metadata.
 *
 * The metadata about an OAuth registered service, which is the CAS perspective of an OSF developer app. In addition,
 * the property {@link ClientMetadata#users} is not a list of users as its name indicates but actually the total number
 * of users that have authorized the client / service / developer app.
 *
 * The CAS OAuth Service {@link org.jasig.cas.support.oauth.CentralOAuthServiceImpl#getClientMetadata} uses this class
 * when retrieving the metadata about a given client / service / developer app.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public class ClientMetadata {

    private final String clientId;
    private final String name;
    private final String description;
    private final Integer users;

    /**
     * Instantiate a {@link ClientMetadata} with details of an OAuth registered service.
     *
     * @param clientId the client id
     * @param name the name of the client
     * @param description the description of the client
     * @param users the number of users of the client
     */
    public ClientMetadata(final String clientId, final String name, final String description, final Integer users) {
        this.clientId = clientId;
        this.name = name;
        this.description = description;
        this.users = users;
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

    public Integer getUsers() {
        return this.users;
    }
}
