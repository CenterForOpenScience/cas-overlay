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

    /** Default Login through OSF. */
    LOGIN_OSF(ApiEndpoint.LOGIN + "/osf"),

    /** Login through Institution. */
    LOGIN_INSTITUTION(ApiEndpoint.LOGIN + "/institution"),

    /** Login through External IdP. */
    LOGIN_EXTERNAL(ApiEndpoint.LOGIN + "/external"),

    /** Account Creation through OSF. */
    ACCOUNT_REGISTER_OSF(ApiEndpoint.ACCOUNT + "/register/osf"),

    /** Default Account Verification. */
    ACCOUNT_VERIFY_OSF(ApiEndpoint.ACCOUNT + "/verify/osf"),

    /** Default Account Verification: Resend Email. */
    ACCOUNT_VERIFY_OSF_RESEND(ApiEndpoint.ACCOUNT + "/verify/osf/resend"),

    /** Account Creation Or Link through External IdP. */
    ACCOUNT_REGISTER_EXTERNAL(ApiEndpoint.ACCOUNT + "/register/external"),

    /** Account Verification: Create Or Link OSF Account. */
    ACCOUNT_VERIFY_EXTERNAL(ApiEndpoint.ACCOUNT + "/verify/external"),

    /** Account Password Manager: Forgot Password. */
    ACCOUNT_PASSWORD_FORGOT(ApiEndpoint.ACCOUNT + "/password/forgot"),

    /** Account Password Manager: Reset Password. */
    ACCOUNT_PASSWORD_RESET(ApiEndpoint.ACCOUNT + "/password/reset"),

    /** Load OSF Institutions as Registered Services. */
    SERVICE_INSTITUTIONS(ApiEndpoint.SERVICE + "/institutions"),

    /** Load Developer Applications as Registered Services. */
    SERVICE_OAUTH_APPS(ApiEndpoint.SERVICE + "/oauth/apps"),

    /** Personal Access Token. */
    SERVICE_OAUTH_TOKEN(ApiEndpoint.SERVICE + "/oauth/token"),

    /** OAuth Scopes. */
    SERVICE_OAUTH_SCOPE(ApiEndpoint.SERVICE + "/oauth/scope");

    /** Prefix for Login Endpoints. */
    private static final String LOGIN = "login";

    /** Prefix for Account Endpoints. */
    private static final String ACCOUNT = "account";

    /** Prefix for Service Endpoints. */
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
