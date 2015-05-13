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
  * Login, Logout, One Time Password
  * OAuth Application Approval

### OAuth2 Provider (Server)
*[Roadmap 4.1 OAuth Server Support](https://wiki.jasig.org/display/CAS/CAS+4.1+Roadmap#CAS4.1Roadmap-Oauthserversupport)*

* Profile Service specific Attribute Release
* Access Tokens and Refresh Tokens
  * Delegated Ticket Expiration (60 minutes for Access Token, never-expire Refresh Token)
  * Grant Types
    * Authorization Code
    * Refresh Token
  * Approval Prompt (Auto or Force)
  * Revoke Access Tokens & Refresh Tokens
  * Encrypted JSON Web Tokens (JWT)
* Login Session Access Token via augmented CAS 3 Protocol (Optional)

### Service Registry

* Multiple Service Registry Loader
* JSON Service Registry
* Open Science Framework Service Registry (MongoDB & OAuth)

### Jetty 9.x Web Server

* Startup Server Command
  * `mvn -pl cas-server-webapp/ jetty:run`
* Optimized for fast builds

### TODO

* Request Throttling
* Jetty JPA Shared Sessions
* Open Science Framework
  * OAuth Endpoints
    * Revoke All User Tokens
    * Application User Counts
  * User
    * Active Login Sessions
    * Approved OAuth Applications
    * Revoke OAuth Application
