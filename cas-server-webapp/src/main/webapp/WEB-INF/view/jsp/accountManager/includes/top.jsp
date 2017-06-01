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

<%@ page import="io.cos.cas.account.flow.AccountManager" %>
<c:set var="accountManagerContext" value="${accountManager}" />
<%
    AccountManager accountManager = AccountManager.fromJson((String) pageContext.getAttribute("accountManagerContext"));
    pageContext.setAttribute("accountManagerContext", accountManager);
%>
<c:set var="serviceUrl" value="${not empty accountManagerContext.getServiceUrl() ? accountManagerContext.getServiceUrl() : fn:escapeXml(param.service)}" />

<html lang="en">
    <head>
        <meta charset="UTF-8" />

        <title>OSF | Central Authentication Service</title>

        <spring:theme code="standard.custom.css.file" var="customCssFile" />
        <link rel="stylesheet" href="<c:url value="${customCssFile}" />" />
        <link rel="icon" href="<c:url value="/favicon.ico" />" type="image/x-icon" />

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
                        <c:choose>
                            <c:when test="${not empty registeredService}">
                                <span class="title-full">${registeredService.properties.title.getValue()}</span>
                                <span class="title-abbr">${registeredService.properties.titleAbbr.getValue()}</span>
                            </c:when>
                            <c:otherwise>
                                <span class="title-full">Open Science Framework</span>
                                <span class="title-abbr">OSF CAS</span>
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>
                <div class="responsive">
                    <div id="description">
                        <br><br>
                        <c:choose>
                            <c:when test="${accountManagerContext.isRegister()}">
                                <spring:message code="screen.osf.register.message" />
                            </c:when>
                            <c:otherwise>
                                <spring:message code="screen.cas.login.message" />
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </header>
            <br>
            <div id="content">
                <spring:eval var="tgcCookieSecure" expression="@casProperties.getProperty('tgc.cookie.secure')" />
                <c:if test="${not pageContext.request.secure && tgcCookieSecure}">
                    <div id="msg" class="errors">
                        <h2><spring:message code="screen.nonsecure.title" /></h2>
                        <p><spring:message code="screen.nonsecure.message" /></p>
                    </div>
                </c:if>

                <c:if test="${not empty registeredService}">
                    <c:if test="${not empty registeredService.logo || not empty registeredService.name}">
                        <div id="service-ui" class="service-info">
                            <table>
                                <tr>
                                    <c:if test="${not empty registeredService.logo && empty registeredService.name}">
                                        <td><img class="service-logo-full" src="${registeredService.logo}"> </td>
                                    </c:if>
                                    <c:if test="${empty registeredService.logo && not empty registeredService.name}">
                                        <td><span class="service-name">${registeredService.name}</span></td>
                                    </c:if>
                                    <c:if test="${not empty registeredService.logo && not empty registeredService.name}">
                                        <td><img id="service-logo" class="service-logo-${registeredService.name}" src="${registeredService.logo}"> </td>
                                        <td>&nbsp;&nbsp;&nbsp;</td>
                                        <td><span class="service-name">${registeredService.name} Preprints</span></td>
                                    </c:if>
                                </tr>
                            </table>
                        </div>
                    </c:if>
                </c:if>
                <br/>