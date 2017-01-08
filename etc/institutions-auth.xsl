<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <provider>
            <xsl:apply-templates/>
        </provider>
    </xsl:template>

    <xsl:template match="auth">
        <xsl:variable name="idp" select="//attribute[@name='Shib-Identity-Provider']/@value" />
        <idp><xsl:value-of select="$idp"/></idp>
        <xsl:choose>
            <!-- New York University (NYU) -->
            <xsl:when test="$idp='https://shibbolethqa.es.its.nyu.edu/idp/shibboleth'">
                <id>nyu</id>
                <user>
                    <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                    <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                    <familyName/>
                    <givenName/>
                    <middleNames/>
                    <suffix/>
                </user>
            </xsl:when>
            <!-- University of Notre Dame (ND) -->
            <xsl:when test="$idp='https://login-test.cc.nd.edu/idp/shibboleth'">
                <id>nd</id>
                <user>
                    <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                    <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                    <familyName/>
                    <givenName/>
                    <middleNames/>
                    <suffix/>
                </user>
            </xsl:when>
            <!-- University of California Riverside (UCR) -->
            <xsl:when test="$idp='urn:mace:incommon:ucr.edu'">
                <id>ucr</id>
                <user>
                    <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                    <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                    <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                    <givenName><xsl:value-of select="//attribute[@name='givenName']/@value"/></givenName>
                    <middleNames/>
                    <suffix/>
                </user>
            </xsl:when>
            <!-- University of Southern California (USC) -->
            <xsl:when test="$idp='https://shibboleth.usc.edu/shibboleth-idp'">
                <id>usc</id>
                <user>
                    <username><xsl:value-of select="//attribute[@name='wuscEmailPrimaryAddress']/@value"/></username>
                    <fullname><xsl:value-of select="//attribute[@name='uscDisplayGivenName']/@value"/><xsl:text> </xsl:text><xsl:value-of select="//attribute[@name='uscDisplaySn']/@value"/></fullname>
                    <familyName><xsl:value-of select="//attribute[@name='uscDisplaySn']/@value"/></familyName>
                    <givenName><xsl:value-of select="//attribute[@name='uscDisplayGivenName']/@value"/></givenName>
                    <middleNames><xsl:value-of select="//attribute[@name='uscDisplayMiddleName']/@value"/></middleNames>
                    <suffix/>
                </user>
            </xsl:when>
            <!-- Universiteit Gent (UGENT) -->
            <xsl:when test="$idp='https://identity.ugent.be/simplesaml/saml2/idp/metadata.php'">
                <id>ugent</id>
                <user>
                    <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                    <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                    <familyName/>
                    <givenName/>
                    <middleNames/>
                    <suffix/>
                </user>
            </xsl:when>
            <!-- University of Virginia (UVA) -->
            <xsl:when test="$idp='https://shibidp-test.its.virginia.edu/idp/shibboleth'">
                <id>uva</id>
                <user>
                    <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                    <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                    <familyName/>
                    <givenName/>
                    <middleNames/>
                    <suffix/>
                </user>
            </xsl:when>
            <!-- Unknown Identity Provider -->
            <xsl:otherwise>
                <xsl:message terminate="yes">Error: Unknown Identity Provider '<xsl:value-of select="$idp"/>'</xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
