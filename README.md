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

### OAuth2 Provider (Server)
*[Roadmap 4.1 OAuth Server Support](https://wiki.jasig.org/display/CAS/CAS+4.1+Roadmap#CAS4.1Roadmap-Oauthserversupport)*

* Profile Service specific Attribute Release
* Tokens (Access and Refresh Tokens)
  * Delegated Ticket Expiration (60 minutes for Access Token, never-expire Refresh Token)
  * Grant Types
    * Authorization Code
    * Refresh Token
    * Approval Prompt (Auto or Force)
  * Revoke Tokens
    * User Tokens
    * User Application Token
    * Application Tokens
  * List User Authorized Applications
* Metadata
  * Application Service Information (including number of users)
* Login Session Access Token via augmented CAS 3 Protocol (Optional)

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
