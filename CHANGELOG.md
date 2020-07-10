# Changelog

We follow the CalVer (https://calver.org/) versioning scheme: YY.MINOR.MICRO.

20.2.1 (2020-07-10)
===================

- Fixed broken OKState SSO due to multi-factor update at their end
- Ignored non-String and multi-value attributes for institutions using CAS-pac4j based SSO
- Improved logging for pac4j auth delegation

20.2.0 (2020-06-04)
===================

- Added automatic institution selection
- Fixed branded sign-in for frenxiv
- Fixed heading for authorization failure page
- Improved generic login success page

20.1.0 (2020-04-14)
===================

- Move all OSF-customized auth exceptions to their decidated package
- Split the general institution login exception into three specialized ones
- Fixed an issue where the institution exception may be thrown for general OAuth failures
- Rewrite JavaDoc, comments, log messages and auth exceptions for non-interative login actions
- OSF TOTP model change: the "deleted" field is now a timestamp (which was a boolean)

20.0.3 (2020-02-20)
===================

- Update institutions-auth.xsl to normalize BT attributes

20.0.2 (2020-02-10)
===================

- Add Concordia College to institution SSO via CAS

20.0.1 (2020-02-06)
===================

- Branded sign-in for HSRxiv

20.0.0 (2020-02-03)
===================

- Fixed user status check for new unconfirmed ORCiD user
- Updated the institution SSO guide for both SAML and CAS
- Added a guide for common apache / shibboleth errors

19.3.3 (2020-01-02)
===================

- Update copyright year: 2020

19.3.2 (2019-12-18)
===================

- Branded sign-in for BioHackrXiv

19.3.1 (2019-12-05)
===================

- Add UBC Prod and Test to institutions-auth.xsl

19.3.0 (2019-11-18)
===================

- Gracefully handle exceptions during delegated login using pac4j-1.7.x
- Add / Update / Fix Apache 2.0 license header

19.2.2 (2019-11-13)
===================

- Update SSO for California Lutheran University
- Fix change log for 19.2.1

19.2.1 (2019-11-12)
===================

- Add callutheran2 to institutions-auth.xsl
- Manually set Prefix URL for pac4j CAS clients

19.2.0 (2019-10-16)
===================

- Update the column name for OSF TOTP / 2FA model: `deleted` -> `is_deleted`
- Refactor JavaDoc, comments and code style for the OAuth module
- Refactor the main readme and add several new guides
- Fixed ORCiD login for local development
- Enable TODO comments

19.1.2 (2019-10-07)
===================

- Update CAS login URL for callutheran
- Add callutheran to inst attr map

19.1.1 (2019-08-21)
===================

Update inst attr map (base and unc) and tweak PR template

19.1.0 (2019-08-19)
===================

Update CAS for OSF token-scope relationship model change.

- Add M2M relationship between PAT and scope
- Add scopeId and isPublic to the scope model
- Remove scopes from the PAT model
- Update OSF DAO and its implementation
  - Query token-scope by token's PK
  - Query scope by scope's PK Update PAT handler

19.0.0 (2019-08-19)
===================

- Update the PR template
- Add the authors list
- Fixed type in readme
- Update TCI to switch from oraclejdk8 to openjdk8

18.1.7 (2019-08-15)
===================

- Fix oraclejdk8 build failure on travis

18.1.6 (2019-06-06)
===================

- Add branded login support for indiarxiv

18.1.5 (2019-04-25)
===================

- Add branded login support for edarxiv

18.1.4 (2019-03-04)
===================

- Add branded login support for metaarxiv

18.1.3 (2019-02-28)
===================

- Update logo for bodoarxiv

18.1.2 (2019-02-05)
===================

- Add branded login support for bodoarxiv

18.1.1 (2019-01-02)
===================

- Update copyright year for 2019

18.1.0 (2018-12-20)
===================

- Improve login context and login handler

18.0.6 (2018-12-18)
===================

- Add branded login support for mediarxiv

18.0.5 (2018-12-12)
===================

- Allow empty REMOTE\_UESR header during institution auth

18.0.4 (2018-11-20)
===================

- Support OSF signup via ORCiD login

18.0.3 (2018-11-09)
===================

- Add branded login support for ecoevorxiv and banglarxiv

18.0.2 (2018-11-02)
===================

- Fix typo in CHANGELOG.md

18.0.1 (2018-11-02)
===================

- Add CHANGELOG.md

18.0.0 (2018-10-26)
===================

- Fix the infinite loop caused by invalid verification key
