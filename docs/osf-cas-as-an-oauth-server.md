# OSF CAS as an OAuth Server

## About

OSF CAS serves as an OAuth 2.0 authorization server for OSF in addition to its primary role as a CAS authentication server. 

### The OAuth 2.0 Protocol

* [RFC 6749](https://tools.ietf.org/html/rfc6749)
* [OAuth 2.0](https://oauth.net/2/)
* [OAuth 2.0 Simplified](https://aaronparecki.com/oauth-2-simplified/)

### Parties and Roles

Party                   | Who               | Role
----------------------- | ----------------- | ----
Client Application      | A web application | 
Authorization Server    | OSF CAS           | 
Resource Owner          | OSF users         | 
Resource Server         | OSF API           | 

### Enable OAuth 2.0 Server Support

* [Apereo CAS: OAuth Protocol](https://apereo.github.io/cas/4.1.x/protocol/OAuth-Protocol.html)
* [Apereo CAS: CAS as OAuth Server](https://apereo.github.io/cas/4.1.x/installation/OAuth-OpenId-Authentication.html)

</br>

## Features

### General

* Authorize client applications
* Exchange authorization code for access and refresh token
* Request access token using refresh token
* Revoke access and refresh tokens

### Client Application Owners

* Get the number of users who have authorized the application
* Revoke all tokens of the application for all users 

### Resource Owners

* List all authorized applications
* Revoke all tokens of an authorized application for the owner

</br>

## Design and Implementation

For implementation details, please refer to the [`cas-server-support-oauth`](https://github.com/CenterForOpenScience/cas-overlay/tree/develop/cas-server-support-oauth/src/main/java/org/jasig/cas) module.

### Tokens and Token Management

* Token
  * [Authorization Code](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-oauth/src/main/java/org/jasig/cas/support/oauth/token/AuthorizationCodeImpl.java) (AC)
  * [Access Token](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-oauth/src/main/java/org/jasig/cas/support/oauth/token/AccessTokenImpl.java) (AT)
  * [Refresh Token](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-oauth/src/main/java/org/jasig/cas/support/oauth/token/RefreshTokenImpl.java) (RT)
* [Token Type](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-oauth/src/main/java/org/jasig/cas/support/oauth/token/TokenType.java)
  * `OFFLINE`: AC, AC-exchanged RT and RT-granted AT
  * `ONLINE`: AC, AC-exchanged AT
  * `PERSONAL`: AT granted based on existing [OSF Personal Access Token](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-osf/src/main/java/io/cos/cas/adaptors/postgres/models/OpenScienceFrameworkApiOauth2PersonalAccessToken.java) (OSF PAT)
  * `CAS`: AT granted as attributes during service validation
* Token Access and Storage
  * [JPA Token Registry](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-oauth/src/main/java/org/jasig/cas/support/oauth/token/registry/JpaTokenRegistry.java)

### Scope and Scope Management

In OSF CAS, [Scope](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-oauth/src/main/java/org/jasig/cas/support/oauth/scope/Scope.java) is not stored by itself in the CAS DB but as an [LOB property](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-oauth/src/main/java/org/jasig/cas/support/oauth/token/AbstractToken.java#L65) of the associated [Token](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-oauth/src/main/java/org/jasig/cas/support/oauth/token/AbstractToken.java). The [Scope Manager](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-oauth/src/main/java/org/jasig/cas/support/oauth/scope/ScopeManager.java) uses 1) [Simple OAuth Scope Handler](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-oauth/src/main/java/org/jasig/cas/support/oauth/scope/handler/SimpleScopeHandler.java) to handle scopes associated with `CAS` ATs only and 2) the [OSF Scope Handler](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-osf/src/main/java/io/cos/cas/adaptors/postgres/handlers/OpenScienceFrameworkScopeHandler.java) to handle scopes for all other types that use [OSF Scope]([OSF Scope](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-osf/src/main/java/io/cos/cas/adaptors/postgres/models/OpenScienceFrameworkApiOauth2Scope.java).

### Service and Service Management

In OSF CAS, client applications are loaded (and periodically updated) from the [OSF DB](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-osf/src/main/java/io/cos/cas/adaptors/postgres/models/OpenScienceFrameworkApiOauth2Application.java) into the memory as [OAuth Registered Service](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-oauth/src/main/java/org/jasig/cas/support/oauth/services/OAuthRegisteredService.java). The main [OAuth Service](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-oauth/src/main/java/org/jasig/cas/support/oauth/CentralOAuthServiceImpl.java) accesses them via the [Services Manager](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-oauth/src/main/java/org/jasig/cas/support/oauth/CentralOAuthServiceImpl.java#L154).

### Delegated Ticket Expiration

The expiration time for a token is determined by [the expiration policy](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-oauth/src/main/java/org/jasig/cas/support/oauth/ticket/support/OAuthDelegatingExpirationPolicy.java) its ticket uses.

* `ONLINE` / `OFFLINE` AC: 1 minute
* `ONLINE` / `OFFLINE` AT: 1 hour
* `OFFLINE` RT: never-expire
* `PERSONAL` AT: never-expire
* `CAS` AT: varies - 30-day if *Remember Me* is enabled; otherwise 8-hour maximum lifetime with a 2-hour sliding window.

### CAS Service Validation

The [OAuth 2.0 Service Validate Controller](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/cas-server-support-oauth/src/main/java/org/jasig/cas/support/oauth/web/OAuth20ServiceValidateController.java) replaces the default [CAS Service Validate Controller](https://github.com/apereo/cas/blob/4.1.x/cas-server-webapp-support/src/main/java/org/jasig/cas/web/ServiceValidateController.java). In addition to performing default duties, it injects a `CAS` AT into the attributes released to OSF users during primary authentication.

</br>

## Endpoints

### `GET /oauth2/authorize`

#### Authorize a Client Application

Securely allows or denies the client application's request to access the resource user's information with specified scopes. It returns a one-time and short-lived authorization code, which will be used to follow up with the token-code exchange.

##### Request

```
https://accounts.osf.io/oauth2/authorize
```

##### Request Query Parameters

Parameter       | Value / Example                   | Description
--------------- | --------------------------------- | -----------
response_type   | code                              | 
client_id       | ffe5247b810045a8a9277d3b3b4edc7a  | 
redirect_uri    | https://my.app.io/oauth2/callback | 
scope           | osf.full_read                     | 
state           | OQlHiyBY                          | 
access_type     | **online** / offline              | 
approval_prompt | **auto** / force`                 | 

##### Authorization Web Flow

0. The client application issues the initial authorization request to `oauth2/authorize`.
1. If the user is not logged in, redirects to the primary CAS login with `oauth2/callbackAuthorize` as service.
2. Both step 0 and 1 end up redirecting the user to `oauth2/callbackAuthorize` for service validation.
3. After validation, redirects to `oauth2/callbackAuthorize` one more time, which checks previous decisions and asks the user to allow or deny the authorization if necessary.
4. If denied, redirects to `/oauth2/callbackAuthorizeAction?action=DENY`; if allowed, redirects to `/oauth2/callbackAuthorizeAction?action=ALLOW`
5. Finally for both decisions in step 4, redirects the user to the *Redirect URI*: `https://my.app.io/oauth2/callback?` with different query parameters as shown below.

##### Query Parameters: ALLOW

Parameter   | Value / Example
----------- | -------------------------------------------------------
code        | AC-1-mFs7MrWvaQy1fiidWGwXTw4dbAH30wk39cAELJnxizjGCUXYJl
state       | OQlHiyBY

##### Query Parameters: DENY

Parameter   | Value / Example
----------- | ---------------
error       | access_denied

</br>

### `POST /oauth2/token`

#### Exchange Code for Token

Exchanges the authorization code for an access token and potentially a refresh token if *offline* mode was specified.

##### Request

```
https://accounts.osf.io/oauth2/token
```

##### `POST` Body Parameters

Parameter       | Value / Example                                           | Description
--------------- | --------------------------------------------------------- | -----------
code            | AC-1-mFs7MrWvaQy1fiidWGwXTw4dbAH30wk39cAELJnxizjGCUXYJl   | 
client_id       | ffe5247b810045a8a9277d3b3b4edc7a                          | 
client_secret   | 5PgE96R3Z53dBuwBDkJfbK6ItDXvGhaxYpQ6r4cU                  | 
redirect_uri    | https://my.app.io/oauth2/callback                         | 
grant_type      | authorization_code                                        | 

##### Response

```
HTTP 200 OK
```

###### ONLINE Mode

```json
{
    "access_token": "AT-2-p5jtVLATgft5EHqqbCTagg5i3q9e1htdcGEBvcpq0l1b2RyQav4bItEKPcDh94c5z7d7EK",
    "token_type": "Bearer",
    "expires_in": 3600
}
```

###### OFFLINE Mode

```json
{
    "access_token": "AT-1-IBGuzWBdencAMz74LQkIuNcbLuu9WM3TYyacadkecrHUlcivs1GnWHjFmlkZPYg4TTAUM4",
    "refresh_token": "RT-1-xfQXZaqXSQIJykCg2vnfdQjc5efVKdtteXaPo0OwCxWzIAacfC",
    "token_type": "Bearer",
    "expires_in": 3600
}
```

#### Refresh Access Token

In *offline* mode, the client application may request for a new access token by presenting the previously granted refresh token.

##### Request

```
https://accounts.osf.io/oauth2/token
```

##### `POST` Body Parameters

Parameter       | Value / Example                                           | Description
--------------- | --------------------------------------------------------- | -----------
refresh_token   | RT-1-xfQXZaqXSQIJykCg2vnfdQjc5efVKdtteXaPo0OwCxWzIAacfC   | 
client_id       | ffe5247b810045a8a9277d3b3b4edc7a                          | 
client_secret   | 5PgE96R3Z53dBuwBDkJfbK6ItDXvGhaxYpQ6r4cU                  | 
grant_type      | refresh_token                                             | 

##### Response

```
HTTP 200 OK
```

```json
{
    "access_token": "AT-3-WbBmXVTsPlhUatrs5sQmilVLnA30wVv3holmfFCbIfePRjzQ6UXCb7LwJHGbFqmad3wNXu",
    "token_type": "Bearer",
    "expires_in": 3600
}
```

</br>

### `GET /oauth2/profile`

#### Profile

Provides the user's principal ID, any released attributes and a list of granted scopes.

##### Request

```
https://accounts.osf.io/oauth2/profile
```

##### Authorization Header

Name            | Value / Example
--------------- | ----------------------------------------------------------------------------------
Authorization   | Bearer AT-4-IdanI4hWiybRzARBiLrlMdeMTlDJIqo1UgVLb4MHzbF13pNIT5POrfQTMW5yEyVD1oXXcz

##### Response

```
HTTP 200 OK
```

```json
{
    "scope": [
        "osf.full_read"
    ],
    "id": "f2t7d"
}
```

</br>

### `POST /oauth2/metadata`

#### Metadata about a Client Application

Provides metadata about an application specified by the given client ID.

##### Request

```
https://accounts.osf.io/oauth2/metadata
```

##### `POST` Body Parameters

Parameter       | Value / Example                           | Description
--------------- | ----------------------------------------- | -----------
client_id       | ffe5247b810045a8a9277d3b3b4edc7a          | 
client_secret   | 5PgE96R3Z53dBuwBDkJfbK6ItDXvGhaxYpQ6r4cU  | 

##### Response

```
HTTP 204 NO CONTENT
```

```json
{
    "name": "An OAuth 2.0 Developer App",
    "description": "See https://my.app.io/about.",
    "client_id": "ffe5247b810045a8a9277d3b3b4edc7a",
    "users": 1023
}
```

#### Metadata about a Resource User

Gathers metadata regarding a user specified by the principal ID associated with with the access token, *which must be of token type `CAS`*.

##### Request

```
https://accounts.osf.io/oauth2/metadata
```

##### `POST` Body Parameters

Name            | Value / Example
--------------- | ----------------------------------------------------------------------------------
Authorization   | Bearer AT-5-OQlHiyBYZwnwqI9Qu6o6Z1fZl7rbx6TzTZB9yPay6SOcbXwfdvpjc6FTbBpgwrj6PMF9GX

##### Response

```
HTTP 200 OK
```

```json
[
    {
        "name": "An OAuth 2.0 Developer App",
        "description": "See https://my.app.io/about.",
        "client_id": "ffe5247b810045a8a9277d3b3b4edc7a",
        "scope": [
            "osf.full_read"
        ]
    },
    {
        "name": "Another OAuth 2.0 Developer App",
        "description": "See https://my.staging.app.io/about.",
        "client_id": "3b4edc7aa9277d3b810045a8ffe5247b",
        "scope": [
            "osf.full_write"
        ]
    }
]
```

</br>

### `POST /oauth2/revoke`

#### Revoke One Token

Handles revocation of refresh and access tokens.

##### Request

```
https://accounts.osf.io/oauth2/revoke
```

##### `POST` Body Parameters

Name    | Value / Example
------- | ---------------------------------------------------------------------------
token   | AT-6-0ckMxjkBHgs5PMqbCtg9BgFo49Y60A1bC5QxFnQeWdiWe9ZfvKwWS52jyIwLrrwVMGFxfa

##### Response

```
HTTP 204 NO CONTENT
```

#### Revoke Tokens for a Client Application

Revokes all tokens associated with a client application specified by the given client ID.

##### Request

```
https://accounts.osf.io/oauth2/revoke
```

##### `POST` Body Parameters

Parameter       | Value / Example                           | Description
--------------- | ----------------------------------------- | -----------
client_id       | ffe5247b810045a8a9277d3b3b4edc7a          | 
client_secret   | 5PgE96R3Z53dBuwBDkJfbK6ItDXvGhaxYpQ6r4cU  | 

##### Response

```
HTTP 204 NO CONTENT
```

#### Revoke Tokens for a Resource User

Revokes all tokens of a client application that have been issued to a resource user. The application is specified by the client ID and the user is specified by the principal ID associated with the access token. The token used for authorization must have been generated by the application *unless it is of token type `CAS`*.

##### Request

```
https://accounts.osf.io/oauth2/revoke
```

##### Authorization Header

Name            | Value / Example
--------------- | ----------------------------------------------------------------------------------
Authorization   | Bearer AT-7-PvVw9wIcTOZYXFCVWbFhwsf9Q3idASiJeBdiWmLExcXSG54lCycokgCefWsy2Nzds4LoAW

##### `POST` Body Parameters

Parameter       | Value / Example                           | Description
--------------- | ----------------------------------------- | -----------
client_id       | ffe5247b810045a8a9277d3b3b4edc7a          | 

##### Response

```
HTTP 204 NO CONTENT
```
