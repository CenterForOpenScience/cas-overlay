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

package io.cos.cas.api.util;

/**
 * Abstract Utility Class for API Endpoint.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public abstract class AbstractApiEndpointUtils {

    /** User Does Not Exist. */
    public static final String ACCOUNT_NOT_FOUND = "ACCOUNT_NOT_FOUND";

    /** User Not Confirmed. */
    public static final String ACCOUNT_NOT_VERIFIED = "ACCOUNT_NOT_VERIFIED";

    /** User Disabled. */
    public static final String ACCOUNT_DISABLED = "ACCOUNT_DISABLED";

    /** Invalid User Status. */
    public static final String INVALID_ACCOUNT_STATUS = "INVALID_ACCOUNT_STATUS";

    /** Invalid Password. */
    public static final String INVALID_PASSWORD = "INVALID_PASSWORD";

    /** Invalid Verification Key. */
    public static final String INVALID_KEY = "INVALID_VERIFICATION_KEY";

    /** Invalid One Time Password. */
    public static final String INVALID_TOTP = "INVALID_TIME_BASED_ONE_TIME_PASSWORD";

    /** Two Factor Authentication Required. */
    public static final String TFA_REQUIRED = "TWO_FACTOR_AUTHENTICATION_REQUIRED";
}
