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

import io.cos.cas.types.OsfLoginAction;

/**
 * Open Science Framework API Endpoint Types.
 *
 * @author Longze
 * @since 4.1.5
 */
public enum ApiEndpoint {

    /** Default Endpoint. */
    NONE("none"),

    /** Authentication Endpoint. */
    AUTH("auth"),

    /** Login Help Endpoint. */
    HELP("help"),

    /** Login Challenge Endpoint. */
    CHALLENGE("challenge"),

    /** Service Endpoint. */
    SERVICE("service"),

    /** User Login. */
    AUTH_LOGIN(ApiEndpoint.AUTH.getId() + "/login"),

    /** User Register. */
    AUTH_REGISTER(ApiEndpoint.AUTH.getId() + "/register"),

    /** Login through Institutions. */
    AUTH_INSTITUTION(ApiEndpoint.AUTH.getId() + "/institution"),

    /** Forgot Password. */
    HELP_FORGOT_PASSWORD(ApiEndpoint.HELP.getId() + '/' + OsfLoginAction.FORGOT_PASSWORD.getId()),

    /** Resend Confirmation Email for New Account. */
    HELP_RESEND_CONFIRMATION(ApiEndpoint.HELP.getId() + '/' + OsfLoginAction.RESEND_CONFIRMATION.getId()),

    /** Reset Password. */
    CHALLENGE_RESET_PASSWORD(ApiEndpoint.CHALLENGE.getId() + '/' + OsfLoginAction.RESET_PASSWORD.getId()),

    /** Confirm Email. */
    CHALLENGE_CONFIRM_EMAIL(ApiEndpoint.CHALLENGE.getId() + '/' + OsfLoginAction.CONFIRM_EMAIL.getId()),

    /** Load Developer Applications as Registered Services. */
    SERVICE_DEVELOPER_APPS(ApiEndpoint.SERVICE.getId() + "/developerApps"),

    /** Load Institutions as Registered Services. */
    SERVICE_INSTITUTIONS(ApiEndpoint.SERVICE.getId() + "/institutions"),

    /** Personal Access Token. */
    SERVICE_PERSONAL_ACCESS_TOKEN(ApiEndpoint.SERVICE.getId() + "/personalAccessToken"),

    /** OAuth Scopes. */
    SERVICE_OAUTH_SCOPES(ApiEndpoint.SERVICE.getId() + "/oauthScopes");

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
