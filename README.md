# Center for Open Science CAS Overlay

`Master` Build Status: [![Build Status](https://travis-ci.org/CenterForOpenScience/cas-overlay.svg?branch=master)](https://travis-ci.org/CenterForOpenScience/cas-overlay)

Official Docs can be found [here](https://jasig.github.io/cas/)

[CAS 4.1 Roadmap](https://wiki.jasig.org/display/CAS/CAS+4.1+Roadmap)

[Docker Server](https://github.com/CenterForOpenScience/docker-library/tree/master/cas)

## Configuration

### JPA Ticket Registry

* Postgres
* Apache DBCP2 (Database Connection Pooling v2)

### Custom Application Authentication

* Multi-Factor Authentication
  * Time-based One Time Passwords (TOTP), e.g. Google Authenticator
* MongoDB authentication backend
* Customized login web flow prompts
  * Login, Logout, One Time Password, Verification Key
  * OAuth Application Approval
* [Login from external form](https://wiki.jasig.org/display/CAS/Using+CAS+from+external+link+or+custom+external+form)

### OAuth2 Provider

[Roadmap 4.1 OAuth Server Support](https://wiki.jasig.org/display/CAS/CAS+4.1+Roadmap#CAS4.1Roadmap-Oauthserversupport)

* Token Registry
  * JPA
* Scope Managers
  * Simple OAuth Scope Handler
  * Open Science Framework Scope Handler (MongoDB)
* Tokens
  * Client Side & Web Application Server Response Types
    * Authorization Code
    * Token
  * Refresh Token Support
  * Revoke Access Tokens & Refresh Tokens
  * Personal Access Tokens (Optional)
  * CAS Login Access Tokens (Optional)
* Service Specific Attribute Release
* Delegated Ticket Expiration
  * Access Token: 60 minutes
  * Refresh Token: never-expire
* Application Integration & Maintenance Actions
  * User Actions
    * List Authorized Applications
    * Revoke Application Tokens
  * User Owned Applications & Stats
    * Active User Count
    * Revoke All Tokens

#### Profile

Provides the user's principal id, any released attributes and a list of granted scopes.

GET: /oauth2/profile

###### Request

```
https://accounts.osf.io/oauth2/profile

Authorization: Bearer AT-1-...
```

###### Response

```json
{
    "id": "unique-user-identifier",
    "scope": ["user.email", "user.profile"]
}
```

#### Web Server Authorization

Secure server authorization of scopes, will need to follow up with the Authorization Code exchange.

GET: /oauth2/authorize

###### Request

```
https://accounts.osf.io/oauth2/authorize?client_id=gJgfkHAtz&redirect_uri=https://my-application/oauth/callback/osf/&scope=user.profile&state=FSyUOBgWiki_hyaBsa
```

Parameter | Value | Description
------------- | ------------- | -------------
response_type | code | ...
client_id | ... | ...
redirect_uri | ... | ...
scope | ... | ...
state | ... | ...
access_type	 | **online** / offline | ...
approval_prompt	 | **auto** / force | ...

###### Response

```
https://my-application/oauth/callback/osf/?code=AC-1-3BfTHEimiGXAQPerA6Zq6cvOszjXAhzHLNQnVJhv3UPifgwVpn&state=FSyUOBgWiki_hyaBsa
```

Parameter | Value | Description
------------- | ------------- | -------------
code | code | ...
state | ... | ...

#### Client Side Authorization

GET: /oauth2/authorize

Allows client side javascript the ability to request specified scopes for authorization and directly return an Access Token.

###### Request

```
https://accounts.osf.io/oauth2/authorize?response_type=token&client_id=gJgfkHAtz&redirect_uri=https://my-application/oauth/callback/osf/&scope=user.profile&state=FSyUOBgWiki_hyaBsa
```

Parameter | Value | Description
------------- | ------------- | -------------
response_type | token | ...
client_id | ... | ...
redirect_uri | ... | ...
scope | ... | ...
state | ... | ...
approval_prompt	 | **auto** / force | ...

###### Response

```
https://my-application/oauth/callback/osf/#access_token=AT-1-E9wpSxcUatFazdGtFFVO21i4exU9RypHbhcacgoktZ7TPUGGVf3KDuMq2RxGzKXZ6FO6if&expires_in=3600&token_type=Bearer&state=FSyUOBgWiki_hyaBsa
```

Parameter | Value | Description
------------- | ------------- | -------------
access_token | ... | ...
expires_in | ... | ...
token_type | Bearer | ...
state | ... | ...


#### Authorization Code Exchange

Exchange of an Authorization Code for an Access Token and potentially a Refresh Token if **offline** mode was specified.  

POST: /oauth2/token

###### Request

```
https://accounts.osf.io/oauth2/token
```

Parameter | Value | Description
------------- | ------------- | -------------
code | ... | ...
client_id | ... | ...
client_secret | ... | ...
redirect_uri | ... | ...
grant_type | authorization_code | ...

###### Response

```json
{
    "token_type": "Bearer",
    "expires_in": 3600,
    "refresh_token":"RT-1-SjLa4ReI4KxcxKzEj1TtIWMTEwcMY26pSy6SftrObikpsbtInb",
    "access_token":"AT-1-adg7yMBUbyO4zSPVqFj2HZzOsTqNtJ5ebgk25y5UbTt4HV5W1EQ45b6PvpDtEABsaXXFBS"
}
```

Parameter | Value | Description
------------- | ------------- | -------------
token_type | Bearer | ...
expires_in | ... | ...
refresh_token | ... | Included only when the authorization request was made with access_type **offline**.
access_token | ... | ...

#### Access Token Refresh

An authorized **offline** application may obtain a new Access Token from this endpoint.

POST: /oauth2/token

###### Request

```
https://accounts.osf.io/oauth2/token
```

Parameter | Value | Description
------------- | ------------- | -------------
refresh_token | ... | ...
client_id | ... | ...
client_secret | ... | ...
grant_type | refresh_token | ...

###### Response

```json
{
    "token_type": "Bearer",
    "expires_in": 3600,
    "access_token":"AT-2-adg7yMBUbyO4zSPVqFj2HZzOsTqNtJ5ebgk25y5UbTt4HV5W1EQ45b6PvpDtEABsaXXFBS"
}
```

Parameter | Value | Description
------------- | ------------- | -------------
token_type | Bearer | ...
expires_in | ... | ...
access_token | ... | ...

#### Revoke a Token

Handles revocation of Refresh and Access Tokens.

POST: /oauth2/revoke

###### Request

```
https://accounts.osf.io/oauth2/revoke
```

Parameter | Value | Description
------------- | ------------- | -------------
token | ... | ...

###### Response

```
HTTP 204 NO CONTENT
```

#### Revoke All Tokens Issued to a Principal

*e.g. user revokes application access*

Revocation of all Tokens associated with the given user's Principal ID.

POST: /oauth2/revoke

###### Request

```
https://accounts.osf.io/oauth2/revoke

Authorization: Bearer AT-1-...
```

###### Response

```
HTTP 204 NO CONTENT
```

#### Revoke All Client Tokens

*e.g. application administrator revokes all tokens*

Revocation of all Tokens associated with the given Client ID.

POST: /oauth2/revoke

###### Request

```
https://accounts.osf.io/oauth2/revoke
```

Parameter | Value | Description
------------- | ------------- | -------------
client_id | ... | ...
client_secret | ... | ...

###### Response

```
HTTP 204 NO CONTENT
```

#### Principal Metadata

*e.g. list applications authorized to access the user's account*

Gathers metadata regarding token's associated withe the Principal ID specified.

POST: /oauth2/metadata

###### Request

```
https://accounts.osf.io/oauth2/metadata

Authorization: Bearer AT-1-...
```

###### Response

```json
[
    {
        "id": "gJgfkHAtz",
        "name": "Application #1",
        "description": "An simple oauth application",
        "scopes": [
            "user.email",
            "profile.basic"
        ]
    },
    {
        "id": "Joiuhwkjsl",
        "name": "Third Party Application #2",
        "description": "An oauth application",
        "scopes": [
            "nodes.create"
        ]
    }
]
```

#### Client Metadata

*e.g. application information, user count, etc...*

Provides metadata about the Client ID specified.

POST: /oauth2/metadata

###### Request

```
https://accounts.osf.io/oauth2/metadata
```

Parameter | Value | Description
------------- | ------------- | -------------
client_id | ... | ...
client_secret | ... | ...


###### Response

```json
{
    "id": "gJgfkHAtz",
    "name": "Application #1",
    "description": "An simple oauth application",
    "users": 9000
}
```

### Service Registry

* Merging Service Registry Loader
* JSON Service Registry
* Open Science Framework Service Registry (MongoDB & OAuth)

### Jetty 9.x Web Server

* Startup Server Command
  * `mvn -pl cas-server-webapp/ jetty:run`
* Optimized for faster builds

### TODO

* Request Throttling
* Jetty JPA Shared Sessions
