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

package io.cos.cas.types;

/**
 * Open Science Framework API Endpoint Types.
 *
 * @author Longze
 * @since 4.1.5
 */
public enum ApiEndpoint {

    /** User Login. */
    AUTH_LOGIN("auth/login"),

    /** User Register. */
    AUTH_REGISTER("auth/register"),

    /** Login through Institutions. */
    AUTH_INSTITUTION("auth/institution"),

    /** Load Developer Applications as Registered Services. */
    SERVICE_DEVELOPER_APPS("service/developerApps"),

    /** Load Institutions as Registered Services. */
    SERVICE_INSTITUTIONS("service/institutions"),

    /** Personal Access Token. */
    SERVICE_PERSONAL_ACCESS_TOKEN("service/personalAccessToken"),

    /** OAuth Scopes. */
    SERVICE_OAUTH_SCOPES("service/oauthScopes");

    private final String id;

    /**
     * API Endpoints Constructor.
     *
     * @param id the id of the API Endpoint
     */
    ApiEndpoint(final String id) {
        this.id = id;
    }

    /**
     * Matches and returns the API Endpoint enumeration type of the id specified.
     *
     * @param id the id of the endpoint type
     * @return the specific API Endpoint enumeration or null
     * @throws IllegalArgumentException if API Endpoint does not match
     */
    public static ApiEndpoint getType(final String id) throws IllegalArgumentException {
        if (id == null) {
            return null;
        }

        for (final ApiEndpoint type : ApiEndpoint.values()) {
            if (id.equals(type.getId())) {
                return type;
            }
        }
        throw new IllegalArgumentException("No matching type for id " + id);
    }

    public final String getId() {
        return id;
    }
}
