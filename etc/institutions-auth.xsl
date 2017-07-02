<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <provider>
            <xsl:apply-templates/>
        </provider>
    </xsl:template>

    <xsl:template match="auth">
        <xsl:variable name="delegation-protocol" select="//attribute[@name='Delegation-Protocol']/@value" />
        <xsl:choose>
            <!--  Institutions that use Shibboleth for Authentication  -->
            <xsl:when test="$delegation-protocol = 'saml-shib'">
                <xsl:variable name="idp" select="//attribute[@name='Shib-Identity-Provider']/@value" />
                <idp><xsl:value-of select="$idp"/></idp>
                <xsl:choose>
                    <!--  Example Shib University -->
                    <xsl:when test="$idp='https://login.exampleshibuniv.edu/idp/shibboleth'">
                        <id>esu</id>
                        <user>
                            <!--  Each institution has customized mapping of attributes  -->
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayedName']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenName']/@value"/></givenName>
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
            <!--  Institutions that use CAS for Authentication  -->
            <xsl:when test="$delegation-protocol = 'cas-pac4j'">
                <xsl:variable name="idp" select="//attribute[@name='Cas-Identity-Provider']/@value" />
                <idp><xsl:value-of select="$idp"/></idp>
                <xsl:choose>
                    <!--  Example CAS University  -->
                    <xsl:when test="$idp='ecu'">
                        <id>ecu</id>
                        <user>
                            <!--  Each institution has customized mapping of attributes  -->
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayedName']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenName']/@value"/></givenName>
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
