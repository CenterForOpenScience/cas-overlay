/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

package io.cos.cas.api;

/**
 * Abstract Util Class for Open Science Framework API Authentication.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public abstract class AbstractOpenScienceFrameworkApiUtils {

    /** Authentication Success. */
    public static final String AUTHENTICATION_SUCCESS = "AUTHENTICATION_SUCCESS";

    /** Authentication Failure. */
    public static final String AUTHENTICATION_FAILURE = "AUTHENTICATION_FAILURE";

    /** Registration Success. */
    public static final String REGISTRATION_SUCCESS = "REGISTRATION_SUCCESS";

    /** Registration Failure. */
     public static final String ALREADY_REGISTERED = "ALREADY_REGISTERED";

    /** Missing Credentials. */
    public static final String MISSING_CREDENTIALS = "MISSING_CREDENTIALS";

    /** User Does Not Exist. */
    public static final String ACCOUNT_NOT_FOUND = "ACCOUNT_NOT_FOUND";

    /** User Not Confirmed. */
    public static final String USER_NOT_CONFIRMED = "USER_NOT_CONFIRMED";

    /** Contributor Not Claimed. */
    public static final String USER_NOT_CLAIMED = "USER_NOT_CLAIMED";

    /** User Disabled. */
    public static final String USER_DISABLED = "USER_DISABLED";

    /** Invalid User Status. */
    public static final String USER_STATUS_INVALID = "USER_STATUS_INVALID";

    /** Invalid Password. */
    public static final String INVALID_PASSWORD = "INVALID_PASSWORD";

    /** Invalid Verification Key. */
    public static final String INVALID_VERIFICATION_KEY = "INVALID_VERIFICATION_KEY";

    /** Invalid One Time Password. */
    public static final String INVALID_ONE_TIME_PASSWORD = "INVALID_ONE_TIME_PASSWORD";

    /** Two Factor Authentication Required. */
    public static final String TWO_FACTOR_AUTHENTICATION_REQUIRED = "TWO_FACTOR_AUTHENTICATION_REQUIRED";
}
