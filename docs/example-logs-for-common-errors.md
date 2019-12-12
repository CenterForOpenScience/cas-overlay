# Example Logs for Common CAS / Shibboleth Errors

## Apache Shibboleth

### Metadata File

* General Metadata Error

Any metadata related issue will lead to the following `CRIT` level logs.

```
CRIT OpenSAML.Metadata.Chaining : failure initializing MetadataProvider: XML error(s) during parsing, check log for specifics
CRIT OpenSAML.Metadata.Chaining : failure initializing MetadataProvider: XML error(s) during parsing, check log for specifics
```

* Online Resource Failure

This error happens usually due to a typo in the URL or temporary network issues. It is rare that the resource is not available.

```
ERROR XMLTooling.ParserPool : fatal error on line 602394, column 20, message: unable to connect socket for URL 'http://md.incommon.org/InCommon/InCommon-metadata.xml'
ERROR XMLTooling.ParserPool : fatal error on line 602394, column 20, message: unable to connect socket for URL 'http://md.incommon.org/InCommon/InCommon-metadata.xml'
ERROR OpenSAML.Metadata.XML : error while loading resource (http://md.incommon.org/InCommon/InCommon-metadata.xml): XML error(s) during parsing, check log for specifics
ERROR OpenSAML.Metadata.XML : error while loading resource (http://md.incommon.org/InCommon/InCommon-metadata.xml): XML error(s) during parsing, check log for specifics
```

* Backup Resource or Local Resource Failure

For online resource, this happens when there is no back-up metadata file. For locally stored resource, this happens when the metadata file path is incorrect.

```
ERROR XMLTooling.ParserPool : fatal error on line 0, column 0, message: unable to open primary document entity '/var/cache/shibboleth/incommon-full-metadata.xml'
ERROR XMLTooling.ParserPool : fatal error on line 0, column 0, message: unable to open primary document entity '/var/cache/shibboleth/incommon-full-metadata.xml'
ERROR OpenSAML.Metadata.XML : error while loading resource (/var/cache/shibboleth/incommon-full-metadata.xml): XML error(s) during parsing, check log for specifics
ERROR OpenSAML.Metadata.XML : error while loading resource (/var/cache/shibboleth/incommon-full-metadata.xml): XML error(s) during parsing, check log for specifics
```

* Metadata XML Parsing Failure

It is very rare that a metadata file provided by InCommon or institution IdPs has syntax issues by itself. However, a typo or mis-indentation can happen when the file content is added to the Helm Charts.

```
ERROR XMLTooling.ParserPool : fatal error on line 14, column 20, message: unterminated end tag 'Extension'
ERROR XMLTooling.ParserPool : fatal error on line 14, column 20, message: unterminated end tag 'Extension'
ERROR OpenSAML.Metadata.XML : error while loading resource (/etc/shibboleth/example-idp-metadata.xml): XML error(s) during parsing, check log for specifics
ERROR OpenSAML.Metadata.XML : error while loading resource (/etc/shibboleth/example-idp-metadata.xml): XML error(s) during parsing, check log for specifics
```

### Attribute Mapping

* General Attribute Mapping Error

Any attribute mapping related issue will lead to the following `CRIT` level logs.

```
CRIT Shibboleth.Application : error building AttributeExtractor: XML error(s) during parsing, check log for specifics
CRIT Shibboleth.Application : error building AttributeExtractor: XML error(s) during parsing, check log for specifics
```

* Attribute Mapping XML Paring Failure

This error can happen if we accidentally break the XML syntax of `attribute-map.xml` when adding new mappings.

```
ERROR XMLTooling.ParserPool : fatal error on line 181, column 98, message: attribute 'name' is already specified for element 'Attribute'
ERROR XMLTooling.ParserPool : fatal error on line 181, column 98, message: attribute 'name' is already specified for element 'Attribute'
ERROR Shibboleth.AttributeExtractor.XML : error while loading resource (/etc/shibboleth/attribute-map.xml): XML error(s) during parsing, check log for specifics
ERROR Shibboleth.AttributeExtractor.XML : error while loading resource (/etc/shibboleth/attribute-map.xml): XML error(s) during parsing, check log for specifics
```

### Institution Login - SAML Response Parsing

* TODO - Response Authentication Failures

Currently, this is not available due to lack of failure examples; and it is super rare to happen at all.

* Retrieve Attributes from SAML Response

The following warning `removed value at position` and `removing attribute` is usually OK since institutions (IdP) may provide us (SP) with extra attributes that are not mapped. However, it is  worthwhile to take a look upon login failures and check if required attributes are dropped such as `eppn`, `uid`, `mail`, `displayname`, `givenName`, `sn` etc.

```
WARN Shibboleth.AttributeFilter [3]: removed value at position (0) of attribute (eppn) from (login.example.edu)
WARN Shibboleth.AttributeFilter [3]: removed value at position (0) of attribute (eppn) from (login.example.edu)
WARN Shibboleth.AttributeFilter [3]: no values left, removing attribute (eppn) from (login.example.edu)
WARN Shibboleth.AttributeFilter [3]: no values left, removing attribute (eppn) from (login.example.edu)
INFO Shibboleth.SessionCache [3]: new session created: ID (_ba7b8c32d313e2686e1391f40751ee10) IdP (login.example.edu) Protocol(urn:oasis:names:tc:SAML:2.0:protocol) Address (172.17.0.1)
INFO Shibboleth-TRANSACTION [3]: New session (ID: _ba7b8c32d313e2686e1391f40751ee10) with (applicationId: default) for principal from (IdP: login.example.edu) at (ClientAddress: 172.17.0.1) with (NameIdentifier: username@login.example.edu) using (Protocol: urn:oasis:names:tc:SAML:2.0:protocol) from (AssertionID: _104078ef246775d75c4a96b82d5a05dc)
```

At least a few attributes must be cached here by Shibboleth for CAS: one for identity, one for email, and another one or two for name(s). Please see the **Jetty CAS** section for example errors when CAS fails to receive required attributes or receives incorrect ones.

```
INFO Shibboleth-TRANSACTION [3]: Cached the following attributes with session (ID: _ba7b8c32d313e2686e1391f40751ee10) for (applicationId: default) {
INFO Shibboleth-TRANSACTION [3]:    mail (1 values)
INFO Shibboleth-TRANSACTION [3]:    displayName (1 values)
INFO Shibboleth-TRANSACTION [3]: }
```

## Jetty CAS
