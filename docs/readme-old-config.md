# Deprecated README.md

Information on this page are outdated and may no longer be correct. Please see the [README.md](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/README.md) of the repository for latest information.

---

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

### Service Registry

* Merging Service Registry Loader
* JSON Service Registry
* Open Science Framework Service Registry (MongoDB & OAuth)

### Jetty 9.x Web Server

* Startup Server Command
  * `mvn -pl cas-server-webapp/ jetty:run`
* Optimized for faster builds

If you have trouble building CAS via `mvn clean install`, you may need to install the "Java Cryptography Extension (JCE) Unlimited Strength
Jurisdiction Policy Files". Follow
[these instructions](http://bigdatazone.blogspot.com/2014/01/mac-osx-where-to-put-unlimited-jce-java.html) to unpack
the zip file, back up existing policy files, and install the new, stronger cryptography policy files.

### TODO

* Request Throttling
* Jetty JPA Shared Sessions
