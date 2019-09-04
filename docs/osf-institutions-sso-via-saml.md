# Connecting to the Open Science Framework (OSF) via Shibboleth-based Single Sign-On (SSO)

This article provides general information about the COS's Shibboleth-based (SSO) integration for organizations who have signed the OSF for Institutions Offer of Services letter.

## What is Single Sign-On?

In general, Single Sign-On, or SSO, allows users authenticated with one trusted system (e.g. university network) to also authenticate using those same “home” credentials with another trusted network (e.g. OSF service). In the case of the second authentication, users are not asked to log in again, but instead the authenticated credentials are shared between systems.

## Who can use Single Sign-On with Open Science Framework?

Any organization that has implemented a SAML 2.0 Identity Provider (IdP) and signed the OSF for Institutions Offer for Services can offer SSO to OSF accounts.

### A few notes:

* Current OSF users who have already set up accounts with a different login, will be able to retain those credentials and choose to login with personal or institutional credentials.

* Users’ authentication to the OSF service using SSO cannot also use the “forgot Password” link on the OSF website to remind them of their credentials, as their user credentials are specific to and managed by their organization.

## Technical Implementation

### InCommon Research & Scholarship Institutions

COS is an [Research & Scholarship Entity Category (R&S)](https://refeds.org/category/research-and-scholarship) Service Provider (SP) registered by the [InCommon Federation](https://www.incommon.org/federation/).

* Entity ID: `https://accounts.osf.io/shibboleth`
* Requested Attributes: `eduPersonPrincipalName` (SAML2), `mail` (SAML2) and `displayName` (SAML2)

Full technical details can be found at https://www.incommon.org/federation/research-scholarship-adopters/.

Please note that only COS's production SP is registered by InCommon. If you want to connect to COS's test / staging SP, here is the [SP metadata](https://accounts.test.osf.io/Shibboleth.sso/Metadata) as mentioned in **Other Institutions** below.

### Other Institutions

COS offers a Service Provider (SP) based on [SAML 2.0](https://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html) (the protocol) and [Shibboleth 2.0](https://wiki.shibboleth.net/confluence/display/SHIB2/Home) (the implementation). To implement and test SSO for your institution:

* Ensure that your IT administrators have loaded COS's SP metadata into your IdP.
  * Production: https://accounts.osf.io/Shibboleth.sso/Metadata
  * Test and/or staging: https://accounts.test.osf.io/Shibboleth.sso/Metadata

* Ensure that your IT administrators are releasing the three required pieces of information listed below and inform COS of the attributes you use for each of them.
  * Unique identifier for the user (e.g. `eppn`)
  * User's institutional email (e.g. `mail`)
  * User's full name (e.g. `displayName` or **a pair of** `givenName` and `sn`)


### For All Institutions

Inform COS of the user you would like to test with; your COS contact will ensure your account is ready to go and will send you a link to test the SSO configuration setup for your institution.


## Alternative SSO Options

COS strongly recommends using this Shibboleth-based SSO when connecting to the OSF. However, if this is not available at your institution, please inform COS of alternative SSO options you have. We may support them in the future.

One alternative that COS currently supports is the CAS-based SSO, please refer to [Connecting to the Open Science Framework (OSF) via CAS-based Single Sign-On (SSO)](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/docs/osf-institutions-sso-via-cas.md) for technical details.
