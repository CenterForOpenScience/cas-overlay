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

package io.cos.cas.api.type;

/**
 * Open Science Framework API Endpoint Type.
 *
 * @author Longze
 * @since 4.1.5
 */
public enum ApiEndpoint {

    /** OSF Default Login. */
    AUTH_LOGIN(ApiEndpoint.AUTH + "/login"),

    /** OSF Account Creation. */
    AUTH_REGISTER(ApiEndpoint.AUTH + "/register"),

    /** OSF Institution Login. */
    AUTH_INSTITUTION(ApiEndpoint.AUTH + "/institution"),

    /** OSF Non-institution External Login. */
    AUTH_EXTERNAL(ApiEndpoint.AUTH + "/external"),

    /** OSF Non-institution External Login: Create Or Link OSF Account. */
    AUTH_EXTERNAL_CREATE_OR_LINK(ApiEndpoint.AUTH_EXTERNAL.getId() + "/createOrLink"),

    /** Verify Email. */
    AUTH_VERIFY_EMAIL(ApiEndpoint.AUTH + "/verifyEmail"),

    /** Reset Password. */
    AUTH_RESET_PASSWORD(ApiEndpoint.AUTH + "/resetPassword"),

    /** Load Developer Applications as Registered Services. */
    SERVICE_LOAD_DEVELOPER_APPS(ApiEndpoint.SERVICE + "/loadDeveloperApps"),

    /** Load OSF Institutions as Registered Services. */
    SERVICE_LOAD_INSTITUTIONS(ApiEndpoint.SERVICE + "/loadInstitutions"),

    /** Find OSF Account by Email. */
    SERVICE_FIND_ACCOUNT(ApiEndpoint.SERVICE + "/findAccount"),

    /** Personal Access Token. */
    SERVICE_CHECK_PERSONAL_ACCESS_TOKEN(ApiEndpoint.SERVICE + "/checkPAT"),

    /** OAuth Scopes. */
    SERVICE_CHECK_OAUTH_SCOPE(ApiEndpoint.SERVICE + "/checkOauthScope");

    /** Prefix for Authentication Endpoints. */
    private static final String AUTH = "auth";

    /** Prefix for Service (Non-authentication) Endpoints. */
    private static final String SERVICE = "service";

    /** The id of the API Endpoint as a String. */
    private final String id;

    /**
     * API Endpoint Type Constructor.
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
