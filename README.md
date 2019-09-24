# Center for Open Science - CAS Overlay

`Master` Build Status: [![Build Status](https://travis-ci.org/CenterForOpenScience/cas-overlay.svg?branch=master)](https://travis-ci.org/CenterForOpenScience/cas-overlay)

`Develop` Build Status: [![Build Status](https://travis-ci.org/CenterForOpenScience/cas-overlay.svg?branch=develop)](https://travis-ci.org/CenterForOpenScience/cas-overlay)

Versioning Scheme:  [![CalVer Scheme](https://img.shields.io/badge/calver-YY.MINOR.MICRO-22bfda.svg)](http://calver.org)

## About

"Center for Open Science - CAS Overlay" is often referred to as **CAS** or **OSF CAS**. It is the centralized authentication and authorization system for [the OSF](https://osf.io/) and its services such as [Preprints](https://osf.io/preprints/), [Registries](https://osf.io/registries) and [SHARE](https://share.osf.io/).

### Features

* OSF Username and Password Login
* OSF Username and Verification Key Login
* OSF Two-Factor Authentication
* OSF Authentication Delegation
  * [ORCiD Login with OAuth](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/docs/osf-cas-as-an-oauth-client.md)
  * [Institution Login with CAS](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/docs/osf-cas-as-a-cas-client.md)
  * [Institution Login with SAML](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/docs/osf-cas-as-a-saml-sp.md)
* [OSF OAuth Provider](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/docs/osf-cas-as-an-oauth-server.md)
* Login Request Throttling

### References

The implementation of OSF CAS is based on [Yale/Jasig/Apereo CAS 4.1.x](https://github.com/apereo/cas/tree/4.1.x) using [CAS Overlay Template 4.1.x](https://github.com/apereo/cas-overlay-template/tree/4.1). Official docs from [Apereo CAS](https://www.apereo.org/projects/cas) can be found [here](https://apereo.github.io/cas/4.1.x). Learn more about the CAS protocol [here](https://apereo.github.io/cas/4.1.x/protocol/CAS-Protocol.html) or refer to [the full specification](https://apereo.github.io/cas/4.1.x/protocol/CAS-Protocol-Specification.html).

## Running OSF CAS for Development

### Java 8

* Install Java Development Kit 8 (JDK 1.8) either from [Oracle](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or [OpenJDK](https://openjdk.java.net/install/). For macOS, it is recommended to use *THE* package manager [Homebrew](https://brew.sh/) with command `brew cask install adoptopenjdk8`.

### JCE with Unlimited Strength

* Download and install [Unlimited Strength Jurisdiction Policy Files](https://www.oracle.com/java/technologies/jce-all-downloads.html) for Java Cryptography Extension (JCE). [Here](https://www.oracle.com/java/technologies/jce8-downloads.html) is the version for JDK 1.8. Unpack the ZIP file and follow the `README.txt` in the folder to back up existing cryptography policy files and install the new stronger ones.

### Apache Maven

* See [here](https://maven.apache.org/install.html) for how to install `maven` and [here](https://maven.apache.org/ide.html) for IDE integrations.

### A Working OSF

* CAS requires a working OSF (more specifically, its database server) running locally. See [Running the OSF For Development](https://github.com/CenterForOpenScience/osf.io/blob/develop/README-docker-compose.md) for how to run OSF locally with `docker-compose`.

### Database

* CAS requires [Postgres](https://www.postgresql.org/docs/9.6/index.html) as its backend database. Use a port other than `5432` since this default one has already been taken by OSF. Update `database.url`, `database.user` and `database.password` in the [`cas.properties`](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/etc/cas.properties#L141).

* CAS also requires read-only access to OSF's database. No extra Postgres setup or CAS configuration is needed when running OSF locally with `docker-compose` as mentioned above. The [default](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/etc/cas.properties#L94) one works as it is.

### Run CAS

* Refer to the [`Dockerfile`](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/Dockerfile) in the repository for how to run CAS with the [Jetty Maven Plugin](https://www.eclipse.org/jetty/documentation/current/jetty-maven-plugin.html). Only the `app` and `dev` stages are relevant in this case since the `dist` one is used for production and staging servers. Take a look at the [`.travis.yml`](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/.travis.yml) on how to run unit tests. You can skip `package` and go for `clean` and `install` directly; in addition, toggle the profile `nocheck` to turn unit tests on and off.

* TL;DR, here are the commands that you need:

    ```bash
    # clean and install w/ test
    mvn clean install -P !nocheck
    # clean and install w/o test
    mvn clean install -P nocheck
    # start jetty
    mvn -pl cas-server-webapp/ jetty:run
    ```
* With default settings, CAS runs on port `8080` at IP address `192.168.168.167` locally. Change `server.name` here in [`cas.properties`](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/etc/cas.properties#L117) if you want a different IP or port.

### A Few Extra Notes

* To use the "Sign in with ORCiD" feature, create an application at [ORCiD Developer Tools](https://orcid.org/developer-tools) with **Redirect URI** set as `http://192.168.168.167:8080/login`. Alternatively, COS developers can use the credentials provided in https://osf.io/m2hig/wiki/home/. Update `oauth.orcid.client.id` and `oauth.orcid.client.secret` accordingly here in the [`cas.properties`](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/etc/cas.properties#L68). ORCiD login will not work if CAS is run on a different `server.name` without updating 1) OSF `docker-compose` settings and 2) the **Redirect URI** of the ORCiD developer application.

* The "Sign in through institution" feature is not available for local development. It requires a Shibboleth server sitting in front of CAS handling both SAML 2.0 authentication and TLS.

* Installing `java8` with [Homebrew](https://brew.sh/) on macOS (i.e. `brew cask install java8`) [no longer works](https://github.com/ashishb/dotfiles/pull/14) due to [Oracle's new license for Java SE](https://www.oracle.com/downloads/licenses/javase-license1.html). [Here](https://github.com/Homebrew/homebrew-cask-versions/issues/7253) is the discussion. Instead, use the alternative [AdoptOpenJDK](https://adoptopenjdk.net/) and here is the [Tap](https://github.com/AdoptOpenJDK/homebrew-openjdk).

* We recommend using an IDE (e.g. [IntelliJ IDEA](https://www.jetbrains.com/idea/), [Eclipse IDE](https://www.eclipse.org/downloads/), etc.) for local development.
