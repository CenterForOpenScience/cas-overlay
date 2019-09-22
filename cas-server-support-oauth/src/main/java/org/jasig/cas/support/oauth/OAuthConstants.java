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
 * This class has the main constants for the OAuth 2.0 implementation.
 *
 * These constants are used in many places such as OAuth request URLs, redirect response URLs, HTTP headers and body,
 * OAuth session data, error name and messages, etc. However, the naming of the constants is not accurate and at times
 * confusing. Thus, refer to the JavaDoc comments and the actual usage for what they are and how they are used.
 *
 * @author Jerome Leleu
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public interface OAuthConstants {

    /** The name of the HTTP authorization header. */
    String AUTHORIZATION_HEADER = "Authorization";

    /** The name of the redirect uri parameter. */
    String REDIRECT_URI = "redirect_uri";

    /** The name of the response type parameter. */
    String RESPONSE_TYPE = "response_type";

    /** The name of the client id parameter. */
    String CLIENT_ID = "client_id";

    /** The name of the client secret parameter. */
    String CLIENT_SECRET = "client_secret";

    /** The name of the scope parameter (OAuth protocol, as opposed to the CAS protocol one). */
    String SCOPE = "scope";

    /** The name of the principal id parameter. */
    String PRINCIPAL_ATTRIBUTES = "attributes";

    /** The name of the principal id parameter. */
    String PRINCIPAL_ID = "id";

    /** The name of the service name parameter. */
    String SERVICE_NAME = "name";

    /** The name of the service users parameter. */
    String SERVICE_USERS = "users";

    /** The name of the service description parameter. */
    String SERVICE_DESCRIPTION = "description";

    /** The name of the access type parameter. */
    String ACCESS_TYPE = "access_type";

    /** The name of the approval prompt parameter. */
    String APPROVAL_PROMPT = "approval_prompt";

    /** One value for the approval prompt parameter: force. */
    String APPROVAL_PROMPT_FORCE = "force";

    /** The other value for the approval prompt parameter: auto. */
    String APPROVAL_PROMPT_AUTO = "auto";

    /** The name of the code parameter, of which the value stores an authorization code. */
    String CODE = "code";

    /** The name of the service parameter, of which the value is a full callback authorize URL. */
    String SERVICE = "service";

    /** The name of the service ticket parameter. */
    String TICKET = "ticket";

    /** The name of the token parameter. */
    String TOKEN = "token";

    /** The name of the state parameter. */
    String STATE = "state";

    /** The name of access token parameter (OAuth protocol, as opposed to the CAS protocol one). */
    String ACCESS_TOKEN = "access_token";

    /** The name of the refresh token parameter OR one value for the grant type parameter for refresh token. */
    String REFRESH_TOKEN = "refresh_token";

    /** The name of the grant type parameter. */
    String GRANT_TYPE = "grant_type";

    /** The other value of the grant type parameter for authorization code. */
    String AUTHORIZATION_CODE = "authorization_code";

    /** The bearer token prefix in the authorization header OR one of the value for the token type parameter. */
    String BEARER_TOKEN = "Bearer";

    /** The name of the OAuth approval prompt action parameter. */
    String OAUTH20_APPROVAL_PROMPT_ACTION = "action";

    /** The only valid value for the OAuth approval prompt action parameter: allow. */
    String OAUTH20_APPROVAL_PROMPT_ACTION_ALLOW = "allow";

    /** OAuth session parameter: redirect uri. */
    String OAUTH20_REDIRECT_URI = "oauth20_redirect_uri";

    /** OAuth session parameter: login ticket id. */
    String OAUTH20_LOGIN_TICKET_ID = "oauth20_login_ticket_id";

    /** OAuth session parameter: service name. */
    String OAUTH20_SERVICE_NAME = "oauth20_service_name";

    /** OAuth session parameter: state. */
    String OAUTH20_STATE = "oauth20_state";

    /** OAuth session parameter: scope. */
    String OAUTH20_SCOPE = "oauth20_scope";

    /** OAuth session parameter: scope map. */
    String OAUTH20_SCOPE_SET = "oauth20_scope_set";

    /** OAuth session parameter: response type. */
    String OAUTH20_RESPONSE_TYPE = "oauth20_response_type";

    /** OAuth session parameter: client id. */
    String OAUTH20_CLIENT_ID = "oauth20_client_id";

    /** OAuth session parameter: token type. */
    String OAUTH20_TOKEN_TYPE = "oauth20_token_type";

    /** OAuth session parameter: approval prompt. */
    String OAUTH20_APPROVAL_PROMPT = "oauth20_approval_prompt";

    /** OAuth session parameter: bypass approval prompt. */
    String BYPASS_APPROVAL_PROMPT = "bypass_approval_prompt";

    /** Error name for missing access token. */
    String MISSING_ACCESS_TOKEN = "missing_access_token";

    /** Error name for expired access token. */
    String EXPIRED_ACCESS_TOKEN = "expired_access_token";

    /** Bean configuration: the name of the OAuth confirm view. */
    String CONFIRM_VIEW = "oauthConfirmView";

    /** Bean configuration: the name of the OAuth failure view. */
    String ERROR_VIEW = "oauthFailureView";

    /** Error name for invalid requests, e.g. invalid or missing redirect uri, client id and secret. */
    String INVALID_REQUEST = "invalid_request";

    /** Error name for invalid access token. */
    String UNAUTHORIZED_REQUEST = "unauthorized";

    /** Error name for invalid grants, e.g. invalid grant type, expired ST and TGT. */
    String INVALID_GRANT = "invalid_grant";

    /** The OAuth authorize endpoint. */
    String AUTHORIZE_URL = "authorize";

    /** The path for OAuth callback authorize endpoint. */
    String CALLBACK_AUTHORIZE_URL = "callbackAuthorize";

    /** The path for OAuth callback authorize action endpoint. */
    String CALLBACK_AUTHORIZE_ACTION_URL = "callbackAuthorizeAction";

    /** The path for OAuth 2.0 access token endpoint. */
    String TOKEN_URL = "token";

    /** The path for OAuth 2.0 revoke token endpoint. */
    String REVOKE_URL = "revoke";

    /** The path for OAuth 2.0 profile endpoint. */
    String PROFILE_URL = "profile";

    /** The path for OAuth 2.0 metadata endpoint. */
    String METADATA_URL = "metadata";

    /** The name of the expiration time parameter. */
    String EXPIRES_IN = "expires_in";

    /** The name of the token type parameter. */
    String TOKEN_TYPE = "token_type";

    /** The name of the error parameter in the denied callback URL. */
    String ERROR = "error";

    /** The value for the error parameter in the denied callback URL for access denied. */
    String ACCESS_DENIED = "access_denied";

    /** The attribute name of the access token (CAS protocol, as opposed to the OAuth protocol one. */
    String CAS_PROTOCOL_ACCESS_TOKEN = "accessToken";

    /** The attribute name of access token scope (CAS protocol, as opposed to the OAuth protocol one. */
    String CAS_PROTOCOL_ACCESS_TOKEN_SCOPE = "accessTokenScope";

    /** Error description for invalid authorization code. */
    String INVALID_CODE_DESCRIPTION = "Invalid Code";

    /** Error description for missing authorization code. */
    String MISSING_CODE_DESCRIPTION = "Missing Code";

    /** Error description for invalid refresh token. */
    String INVALID_REFRESH_TOKEN_DESCRIPTION = "Invalid Refresh Token";

    /** Error description for missing refresh token. */
    String MISSING_REFRESH_TOKEN_DESCRIPTION = "Missing Refresh Token";

    /** Error description for invalid access token. */
    String INVALID_ACCESS_TOKEN_DESCRIPTION = "Invalid Access Token";

    /** Error description for invalid access token type. */
    String INVALID_ACCESS_TOKEN_TYPE_DESCRIPTION = "Invalid Access Token Type";

    /** Error description for missing access token. */
    String MISSING_ACCESS_TOKEN_DESCRIPTION = "Missing Access Token";

    /** Error description for the invalid token. */
    String INVALID_TOKEN_DESCRIPTION = "Invalid Token";

    /** Error description for the missing token. */
    String MISSING_TOKEN_DESCRIPTION = "Missing Token";

    /** Error description for ticket granting ticket expired. */
    String EXPIRED_TGT_DESCRIPTION = "Ticket Granting Ticket Expired";

    /** Error description for service ticket expired. */
    String EXPIRED_ST_DESCRIPTION = "Service Ticket Expired";

    /** Error description for missing client id. */
    String MISSING_CLIENT_ID_DESCRIPTION = "Missing Client ID";

    /** Error description for unknown client id. */
    String UNKNOWN_CLIENT_ID_DESCRIPTION = "Unknown Client ID";

    /** Error description for missing client secret. */
    String MISSING_CLIENT_SECRET_DESCRIPTION = "Missing Client Secret";

    /** Error description for invalid client secret. */
    String INVALID_CLIENT_SECRET_DESCRIPTION = "Invalid Client Secret";

    /** Error description for invalid client id or client secret. */
    String INVALID_CLIENT_ID_OR_SECRET_DESCRIPTION = "Invalid Client ID or Client Secret";

    /** Error description for invalid redirect uri. */
    String INVALID_REDIRECT_URI_DESCRIPTION = "Invalid Redirect URI";

    /** Error description for missing redirect uri. */
    String MISSING_REDIRECT_URI_DESCRIPTION = "Missing Redirect URI";

    /** Error description for invalid grant type. */
    String INVALID_GRANT_TYPE_DESCRIPTION = "Invalid Grant Type";

    /** Error description for missing grant type. */
    String MISSING_GRANT_TYPE_DESCRIPTION = "Missing Grant Type";

    /** Error description for failed token revocation. */
    String FAILED_TOKEN_REVOCATION_DESCRIPTION = "Token Revocation Failed";
}
