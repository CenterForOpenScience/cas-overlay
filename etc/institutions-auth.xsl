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
            <xsl:when test="$idp='https://login.circle.edu/idp/shibboleth'">
                <id>CIR</id>
                <user>
                    <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                    <!--<email><xsl:value-of select="//attribute[@name='mail']/@value"/></email>-->
                    <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                    <familyName/>
                    <givenName/>
                    <middleNames/>
                    <suffix/>
                </user>
            </xsl:when>
            <xsl:otherwise>
                <error>unknown identity provider</error>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
