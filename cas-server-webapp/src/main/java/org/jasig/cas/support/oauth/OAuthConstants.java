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
package org.jasig.cas.support.oauth;

/**
 * This class has the main constants for the OAuth implementation.
 *
 * @author Jerome Leleu, Michael Haselton
 * @since 3.5.0
 */
public interface OAuthConstants {

    /** The redirect uri. */
    String REDIRECT_URI = "redirect_uri";

    /** The client id. */
    String CLIENT_ID = "client_id";

    /** The client secret. */
    String CLIENT_SECRET = "client_secret";

    /** The approval prompt. */
    String APPROVAL_PROMPT = "approval_prompt";

    /** The approval prompt force. */
    String APPROVAL_PROMPT_FORCE = "force";

    /** The approval prompt auto. */
    String APPROVAL_PROMPT_AUTO = "auto";

    /** The bypass approval prompt. */
    String BYPASS_APPROVAL_PROMPT = "bypass_approval_prompt";

    /** The code. */
    String CODE = "code";

    /** The service. */
    String SERVICE = "service";

    /** The ticket. */
    String TICKET = "ticket";

    /** The token. */
    String TOKEN = "token";

    /** The state. */
    String STATE = "state";

    /** The access token. */
    String ACCESS_TOKEN = "access_token";

    /** The refresh token. */
    String REFRESH_TOKEN = "refresh_token";

    /** The grant type. */
    String GRANT_TYPE = "grant_type";

    /** The authorization code. */
    String AUTHORIZATION_CODE = "authorization_code";

    /** The bearer token. */
    String BEARER_TOKEN = "bearer";

    /** The OATH h20_ approval prompt action. */
    String OAUTH20_APPROVAL_PROMPT_ACTION = "action";

    /** The OATH h20_ approval prompt allow action. */
    String OAUTH20_APPROVAL_PROMPT_ACTION_ALLOW = "allow";

    /** The OAUT h20_ callbackurl. */
    String OAUTH20_CALLBACKURL = "oauth20_callback_url";

    /** The OAUT h20_ login ticket id. */
    String OAUTH20_LOGIN_TICKET_ID = "oauth20_login_ticket_id";

    /** The OAUT h20_ servic e_ name. */
    String OAUTH20_SERVICE_NAME = "oauth20_service_name";

    /** The OAUT h20_ state. */
    String OAUTH20_STATE = "oauth20_state";

    /** The OAUT h20_ client id. */
    String OAUTH20_CLIENTID = "oauth20_client_id";

    /** The missing access token. */
    String MISSING_ACCESS_TOKEN = "missing_access_token";

    /** The expired access token. */
    String EXPIRED_ACCESS_TOKEN = "expired_access_token";

    /** The confirm view. */
    String CONFIRM_VIEW = "oauthConfirmView";

    /** The error view. */
    String ERROR_VIEW = "viewServiceErrorView";

    /** The invalid request. */
    String INVALID_REQUEST = "invalid_request";

    /** The invalid grant. */
    String INVALID_GRANT = "invalid_grant";

    /** The authorize url. */
    String AUTHORIZE_URL = "authorize";

    /** The callback authorize url. */
    String CALLBACK_AUTHORIZE_URL = "callbackAuthorize";

    /** The callback authorize url. */
    String CALLBACK_AUTHORIZE_ACTION_URL = "callbackAuthorizeAction";

    /** The access token url. */
    String ACCESS_TOKEN_URL = "token";

    /** The revoke token url. */
    String REVOKE_TOKEN_URL = "revoke";

    /** The profile url. */
    String PROFILE_URL = "profile";

    /** The remaining time in seconds before expiration with syntax : expires_in: 3600... */
    String EXPIRES_IN = "expires_in";

    /** The token type. */
    String TOKEN_TYPE = "token_type";
}
