# Connecting to the Open Science Framework (OSF) via CAS-based Single Sign-On (SSO)

COS's CAS-based SSO has limited functionality since it is just an alternative for institutions that can not use the Shibboleth-based SSO. Before proceeding, read [Connecting to the Open Science Framework (OSF) via Shibboleth-based Single Sign-On (SSO)](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/docs/osf-institutions-sso-via-saml.md) first for non-technical information on connecting to OSF via SSO.

## Technical Implementation

This SSO is based on [`cas-4.1.x`](https://github.com/apereo/cas/tree/4.1.x) and [`pac4j-1.7.x`](https://github.com/pac4j/pac4j/tree/1.7.x). Refer to the [CAS protocol](https://apereo.github.io/cas/4.1.x/protocol/CAS-Protocol.html) and the [complete specification](https://apereo.github.io/cas/4.1.x/protocol/CAS-Protocol-Specification.html) for how CAS works.

When connecting to the OSF via CAS-based SSO, COS's CAS system (OSF CAS) acts as the **CAS Client** and your institution's CAS system acts as the **CAS Server**. To implement and test SSO for your institution, please follow the steps below.

### Registered Service

Add OSF CAS domain / URL to your CAS system's **Registered Service** list and allow wildcard matching for query parameters.

* Production: `https://accounts.osf.io/login?`
* Test / Staging: `https://accounts.test.osf.io/login?`

### Authentication Endpoints

Inform COS of the domain of your CAS system. More specifically, OSF CAS (as a client) expects the following endpoints to be available and functional.

* Login: `<your CAS system domain>/login`
* Logout: `<your CAS system domain>/logout`
* Validation and attribute release: `<your CAS system domain>/samlValidate`

### Service Validation and Attribute Release

OSF CAS makes a `POST` request to your CAS system's [`/samlValidate`](https://apereo.github.io/cas/4.1.x/protocol/CAS-Protocol-Specification.html#42-samlvalidate-cas-30) endpoint for ticket validation and attribute release. Release the following required attributes (optional ones are highly recommended if possible) and inform us of the attribute name for each.

* Required
  * Unique identifier for the user (e.g. `eppn`)
  * User's institutional email (e.g. `mail`)
  * User's full name (e.g. `displayName`)
* Optional
  * User's first and last name (e.g. a pair of `givenName` and `sn`)
  * User's department(s) at your institution (e.g. `eduPersonOrgUnitDN` or `eduPersonPrimaryOrgUnitDN`)
  * User's relationship(s) (e.g. student, faculty, staff, alum, etc.) to the institution (e.g. `eduPersonAffiliation` or `eduPersonPrimaryAffiliation`)

Please note that OSF CAS can not use your [`/p3/serviceValidate`](https://apereo.github.io/cas/4.1.x/protocol/CAS-Protocol-Specification.html#28-p3servicevalidate-cas-30) endpoint due to an old version of the library it uses, namely [`pac4j-1.7.x`](https://github.com/pac4j/pac4j/tree/1.7.x) and [`cas-server-support-pac4j-1.7.x`](https://github.com/apereo/cas/tree/4.1.x/cas-server-support-pac4j). In addition, OSF CAS does not use your [`/validate`](https://apereo.github.io/cas/4.1.x/protocol/CAS-Protocol-Specification.html#24-validate-cas-10) and [`/serviceValidate`](https://apereo.github.io/cas/4.1.x/protocol/CAS-Protocol-Specification.html#25-servicevalidate-cas-20) endpoints since these two can not release required attributes.

### Test Accounts

It is highly recommended that you can create a temporary institution test account for COS engineers (if possible), which will significantly aid and accelerate the process.
