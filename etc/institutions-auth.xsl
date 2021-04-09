<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright (c) 2016. Center for Open Science

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <provider>
            <xsl:apply-templates/>
        </provider>
    </xsl:template>

    <xsl:template match="auth">
        <xsl:variable name="delegation-protocol" select="//attribute[@name='Delegation-Protocol']/@value" />
        <xsl:choose>
            <!--  Institutions that use the SAML protocol for Authentication  -->
            <xsl:when test="$delegation-protocol = 'saml-shib'">
                <xsl:variable name="idp" select="//attribute[@name='Shib-Identity-Provider']/@value" />
                <idp><xsl:value-of select="$idp"/></idp>
                <xsl:choose>
                    <!--  Example SAML-auth University -->
                    <xsl:when test="$idp='example-shib-auth-univeristy-entity-id'">
                        <id>esu</id>
                        <user>
                            <!--  Each institution has its customized mapping of attributes  -->
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayedName']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenName']/@value"/></givenName>
                            <departmentRaw><xsl:value-of select="//attribute[@name='department']/@value"/></departmentRaw>
                            <eduPerson>true</eduPerson>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Boys Town [PROD & Test] (BT) -->
                    <!-- Prod and Test IdP of Boys Town share the entity ID but the metadata itself is different. -->
                    <xsl:when test="$idp='https://sts.windows.net/e2ab7419-36ab-4a95-a19f-ee90b6a9b8ac/'">
                        <id>bt</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenName']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Brown University (BROWN) -->
                    <xsl:when test="$idp='https://sso.brown.edu/idp/shibboleth'">
                        <id>brown</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenName']/@value"/></givenName>
                            <isMemberOf><xsl:value-of select="//attribute[@name='isMemberOf']/@value"/></isMemberOf>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- California Lutheran University [SAML SSO] (CALLUTHERAN)-->
                    <xsl:when test="$idp='login.callutheran.edu'">
                        <id>callutheran</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- KU Leuven Libraries (KULEUVEN)-->
                    <xsl:when test="$idp='urn:mace:kuleuven.be:kulassoc:kuleuven.be'">
                        <id>kuleuven</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenName']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Princeton University (PU) -->
                    <!--
                        The departmentRaw and eduPerson attributes are for local testing purpose only.
                        Princeton does not release such an attribute yet.
                    -->
                    <xsl:when test="$idp='https://idp.princeton.edu/idp/shibboleth'">
                        <id>pu</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenName']/@value"/></givenName>
                            <departmentRaw><xsl:value-of select="//attribute[@name='department']/@value"/></departmentRaw>
                            <eduPerson>true</eduPerson>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of Arizona (UA) -->
                    <!--
                        The departmentRaw and eduPerson attributes are both for production and local testing purposes.
                        University of Arizona is the first institution to release the department attribute to OSF.
                    -->
                    <xsl:when test="$idp='urn:mace:incommon:arizona.edu'">
                        <id>ua</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenName']/@value"/></givenName>
                            <departmentRaw><xsl:value-of select="//attribute[@name='department']/@value"/></departmentRaw>
                            <eduPerson>false</eduPerson>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of British Columbia (UBC) -->
                    <xsl:when test="$idp='https://authentication.ubc.ca'">
                        <id>ubc</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of British Columbia [Test] (UBC) -->
                    <xsl:when test="$idp='https://authentication.stg.id.ubc.ca'">
                        <id>ubc</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of North Carolina at Chapel Hill (UNC) -->
                    <xsl:when test="$idp='urn:mace:incommon:unc.edu'">
                        <id>unc</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenName']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Virginia Commonwealth University (VCU) -->
                    <xsl:when test="$idp='https://shibboleth.vcu.edu/idp/shibboleth'">
                        <id>vcu</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <departmentRaw><xsl:value-of select="//attribute[@name='department']/@value"/></departmentRaw>
                            <eduPerson>false</eduPerson>
                            <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!--  Unknown Identity Provider  -->
                    <xsl:otherwise>
                        <xsl:message terminate="yes">Error: Unknown Identity Provider '<xsl:value-of select="$idp"/>'</xsl:message>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <!--  Institutions that use the CAS protocol for Authentication  -->
            <xsl:when test="$delegation-protocol = 'cas-pac4j'">
                <xsl:variable name="idp" select="//attribute[@name='Cas-Identity-Provider']/@value" />
                <idp><xsl:value-of select="$idp"/></idp>
                <xsl:choose>
                    <!--  Example CAS-auth University  -->
                    <xsl:when test="$idp='ecu'">
                        <id>ecu</id>
                        <user>
                            <!--  Each institution has its customized mapping of attributes  -->
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenName']/@value"/></givenName>
                            <departmentRaw><xsl:value-of select="//attribute[@name='department']/@value"/></departmentRaw>
                            <eduPerson>false</eduPerson>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Concordia College (CORD) -->
                    <xsl:when test="$idp='cord'">
                        <id>cord</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                            <familyName />
                            <givenName />
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- California Lutheran University [CAS SSO] (CALLUTHERAN2)-->
                    <xsl:when test="$idp='callutheran'">
                        <id>callutheran2</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='email']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='familyName']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenName']/@value"/></givenName>
                            <fullname/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!--  Unknown Identity Provider  -->
                    <xsl:otherwise>
                        <xsl:message terminate="yes">Error: Unknown Identity Provider '<xsl:value-of select="$idp"/>'</xsl:message>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <!--  Unknown Delegation Protocol  -->
            <xsl:otherwise>
                <xsl:message terminate="yes">Error: Unknown Delegation Protocol '<xsl:value-of select="$delegation-protocol"/>'</xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
