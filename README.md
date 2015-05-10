# Center for Open Science CAS Overlay

`Master` Build Status: [![Build Status](https://travis-ci.org/CenterForOpenScience/cas-overlay.svg?branch=master)](https://travis-ci.org/CenterForOpenScience/cas-overlay)

Official Docs can be found [here](https://jasig.github.io/cas/)

CAS 4.1 [Roadmap](https://wiki.jasig.org/display/CAS/CAS+4.1+Roadmap)

## Configuration

### JPA Ticket Registry

* [Postgres](https://github.com/CenterForOpenScience/cas-overlay/blob/master/cas-server-webapp/src/main/webapp/WEB-INF/spring-configuration/dataSource.xml)
* [Apache Common2 Database Connection Pooling](https://github.com/CenterForOpenScience/cas-overlay/blob/master/cas-server-webapp/src/main/webapp/WEB-INF/spring-configuration/dataSource.xml)

### Jetty 9.x Web Server

* Startup Server Command
  * `-pl cas-server-webapp/ jetty:run`
* Optimized for fast builds
  * [pom.xml](https://github.com/CenterForOpenScience/cas-overlay/blob/master/cas-server-webapp/pom.xml#L96)
  * [HTTPS & Build Optimizations](https://github.com/CenterForOpenScience/cas-overlay/tree/master/cas-server-webapp/test/resources)

### Custom Application Authentication

* [Multi-Factor Authentication](https://github.com/CenterForOpenScience/cas-overlay/tree/master/cas-server-webapp/src/main/java/org/jasig/cas/authentication)
  * Time-based One Time Passwords (TOTP), e.g. Google Authenticator
* [MongoDB authentication backend](https://github.com/CenterForOpenScience/cas-overlay/blob/master/cas-server-webapp/src/main/java/org/jasig/cas/adaptors/mongodb/OpenScienceFrameworkAuthenticationHandler.java)
* [Customized login web flow prompts](https://github.com/CenterForOpenScience/cas-overlay/blob/master/cas-server-webapp/src/main/webapp/WEB-INF/webflow/login/login-webflow.xml)

### OAuth2 Provider (Server) - [Roadmap 4.1 Oauth server support](https://wiki.jasig.org/display/CAS/CAS+4.1+Roadmap#CAS4.1Roadmap-Oauthserversupport)

* [Service specific Attribute Release](https://github.com/CenterForOpenScience/cas-overlay/blob/master/cas-server-webapp/src/main/java/org/jasig/cas/support/oauth/web/OAuth20ProfileController.java)
* Access Tokens and Refresh Tokens
  * [Delegated Ticket Expiration (90 days for Access Token, never-expire Refresh Token)](https://github.com/CenterForOpenScience/cas-overlay/blob/master/cas-server-webapp/src/main/webapp/WEB-INF/spring-configuration/ticketExpirationPolicies.xml)
  * Grant Types
    * [Authorization Code](https://github.com/CenterForOpenScience/cas-overlay/blob/master/cas-server-webapp/src/main/java/org/jasig/cas/support/oauth/web/OAuth20GrantTypeAuthorizationCodeController.java)
    * [Refresh Token](https://github.com/CenterForOpenScience/cas-overlay/blob/master/cas-server-webapp/src/main/java/org/jasig/cas/support/oauth/web/OAuth20GrantTypeRefreshTokenController.java)
  * [Revoke Access Tokens & Refresh Tokens](https://github.com/CenterForOpenScience/cas-overlay/blob/master/cas-server-webapp/src/main/java/org/jasig/cas/support/oauth/web/OAuth20RevokeController.java)
* OAuth Specific Tokens (WIP)

### JSON Service Registry

### TODO

* Request Throttling
* JPA Service Registry w/ OAuth support
