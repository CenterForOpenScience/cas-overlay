<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<!DOCTYPE html>

<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html lang="en">
    <head>
        <meta charset="UTF-8" />

        <%-- <title>CAS &#8211; Central Authentication Service</title> --%>
        <title>Open Science Framework | Sign In </title>

        <spring:theme code="standard.custom.css.file" var="customCssFile" />
        <link rel="stylesheet" href="<c:url value="${customCssFile}" />" />
        <link rel="icon" href="<c:url value="/favicon.ico" />" type="image/x-icon" />

        <!--[if lt IE 9]>
            <script src="//cdnjs.cloudflare.com/ajax/libs/html5shiv/3.6.1/html5shiv.js" type="text/javascript"></script>
        <![endif]-->

        <link href='https://fonts.googleapis.com/css?family=Open+Sans:400,600,300,700' rel='stylesheet' type='text/css'>
    </head>

    <body id="cas" onload="selectFocus()">
        <div id="container">
            <br>
            <header>
                <div class="center">
                    <spring:eval var="osfUrl" expression="@casProperties.getProperty('osf.url')" />
                    <a id="logo" class="center" href="${osfUrl}" title="<spring:message code="logo.title" />">Open Science Framework | Sign In</a>
                </div>
                <br>
                <div class="center">
                    <span id="title">
                        <c:if test="${empty serviceCampaign}">
                            <c:choose>
                                <c:when test="${campaign eq 'INSTITUTION'}">
                                    <span>OSF Institutions</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="title-full">Open&nbsp;Science&nbsp;Framework</span>
                                    <span class="title-abbr">OSF</span>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <c:if test="${serviceCampaign eq 'PREREG'}">
                            <span class="title-full">Open&nbsp;Science&nbsp;Framework&nbsp;|&nbsp;Preregistration&nbsp;Challenge</span>
                            <span class="title-long">Open&nbsp;Science&nbsp;Framework&nbsp;|&nbsp;Preregistration&nbsp;Challenge</span>
                            <span class="title-short">Open&nbsp;Science&nbsp;Framework<br><br>Preregistration&nbsp;Challenge</span>
                            <span class="title-abbr">OSF<br><br>Preregistration<br>Challenge</span>
                        </c:if>
                        <c:if test="${serviceCampaign eq 'ERPC'}">
                            <span class="title-full">Open&nbsp;Science&nbsp;Framework&nbsp;|&nbsp;Election&nbsp;Research&nbsp;Preacceptance&nbsp;Competition</span>
                            <span class="title-long">Open&nbsp;Science&nbsp;Framework<br><br>Election&nbsp;Research&nbsp;Preacceptance&nbsp;Competition</span>
                            <span class="title-short">Open&nbsp;Science&nbsp;Framework<br><br>Election&nbsp;Research<br>Preacceptance&nbsp;Competition</span>
                            <span class="title-abbr">OSF<br><br>Election&nbsp;Research<br>Preacceptance<br>Competition</span>
                        </c:if>
                        <c:if test="${serviceCampaign eq 'OSF-PREPRINTS'}">
                            <span class="title-full">Open&nbsp;Science&nbsp;Framework&nbsp;|&nbsp;Preprints</span>
                            <span class="title-long">Open&nbsp;Science&nbsp;Framework&nbsp;|&nbsp;Preprints</span>
                            <span class="title-short">OSF&nbsp;|&nbsp;Preprints</span>
                            <span class="title-abbr">OSF&nbsp;|&nbsp;Preprints</span>
                        </c:if>
                    </span>
                </div>
                <div class="responsive">
                    <c:if test="${campaign eq 'INSTITUTION'}">
                        <div id="description">
                            <br><br><spring:message code="screen.institution.login.message" />
                        </div>
                    </c:if>
                </div>
            </header>
            </br>
            <div id="content">
