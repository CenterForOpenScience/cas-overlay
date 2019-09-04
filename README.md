# Center for Open Science - CAS Overlay (OSF CAS)

`Master` Build Status: [![Build Status](https://travis-ci.org/CenterForOpenScience/cas-overlay.svg?branch=master)](https://travis-ci.org/CenterForOpenScience/cas-overlay)

`Develop` Build Status: [![Build Status](https://travis-ci.org/CenterForOpenScience/cas-overlay.svg?branch=develop)](https://travis-ci.org/CenterForOpenScience/cas-overlay)

Versioning Scheme:  [![CalVer Scheme](https://img.shields.io/badge/calver-YY.MINOR.MICRO-22bfda.svg)](http://calver.org)

## About OSF CAS

"Center for Open Science - CAS Overlay" is often referred to as **CAS** internally or **OSF CAS** externally. It is the centralized authentication and authorization system for the [OSF](https://osf.io/) and its services such as [Preprints](https://osf.io/preprints/), [Registries](https://osf.io/registries) and [SHARE](https://share.osf.io/).

### Features

* Username (email) and password login
* Username and verification key login
* Authentication delegation (see [osf-cas-as-oauth-client.md](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/docs/osf-cas-as-oauth-client.md)):
  * ORCiD login with OAuth
  * Institution login with CAS
* Institution login with SAML 2.0 / Shibboleth 2.0 based SSO 
  * This feature requires a Shibboleth server sitting in front and serving as a SAML 2.0 SP.
* OAuth provider for OSF (see [osf-cas-as-oauth-server.md](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/docs/osf-cas-as-oauth-server.md))
* Login request throttling

The implementation of OSF CAS is based on [Yale/Jasig/Apereo CAS 4.1.x](https://github.com/apereo/cas/tree/4.1.x) using [CAS Overlay Template 4.1.x](https://github.com/apereo/cas-overlay-template/tree/4.1). Official docs from [Apereo CAS](https://www.apereo.org/projects/cas) can be found [here](https://apereo.github.io/cas/4.1.x). Learn more about the CAS protocol [here](https://apereo.github.io/cas/4.1.x/protocol/CAS-Protocol.html) or refer to [the full specification](https://apereo.github.io/cas/4.1.x/protocol/CAS-Protocol-Specification.html).

## Configuration

### JPA Ticket Registry

* Postgres
* Apache DBCP2 (Database Connection Pooling v2)

### Custom Application Authentication

* Two-factor Authentication (2FA) with Time-based One Time Passwords (TOTP)
* Postgres authentication backend
* Customized login web flow prompts
  * Login, institution login, ORCiD login, verification key login, logout, 2FA w/ TOTP
  * OAuth Application Approval

### Service Registry

* Merging Service Registry Loader
* JSON Service Registry
* Open Science Framework Service Registry (Postgres & OAuth)

### Jetty 9.x Web Server

* Optimized for faster builds

## Running OSF CAS for Development

OSF CAS requires read-only access to OSF's database and thus must be developed and run with a working local OSF (see [Running the OSF For Development](https://github.com/CenterForOpenScience/osf.io/blob/develop/README-docker-compose.md)).

As for OSF CAS, refer to the [`Dockerfile`](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/Dockerfile) for how to run CAS with [Jetty Maven Plugin](https://www.eclipse.org/jetty/documentation/current/jetty-maven-plugin.html). The [`Dev`](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/Dockerfile#L94) stage is used for local development. Ignore the [`Dist`](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/Dockerfile#L15) stage for now which is only used for production and staging servers. In addition, take a look that [`.travis.yml`](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/.travis.yml) for how to run unit tests locally. In short, here is all the commands that you need.

```bash
mvn clean install -P nocheck
mvn test -P !nocheck
mvn -pl cas-server-webapp/ jetty:run
```

If you have trouble building CAS via `mvn clean install`, you may need to install the "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files". Follow [these instructions](http://bigdatazone.blogspot.com/2014/01/mac-osx-where-to-put-unlimited-jce-java.html) to unpack the zip file, back up existing policy files, and install the new, stronger cryptography policy files.

At [COS](https://cos.io/), we use [IntelliJ IDEA](https://www.jetbrains.com/idea/) for local development.
