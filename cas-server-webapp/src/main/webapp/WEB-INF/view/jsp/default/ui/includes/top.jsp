<%--

    Copyright (c) 2015. Center for Open Science

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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<%@ page import="io.cos.cas.web.flow.OpenScienceFrameworkLoginHandler.OpenScienceFrameworkLoginContext" %>
<c:set var="pageLoginContext" value="${jsonLoginContext}" />
<%
    String loginContext = (String) pageContext.getAttribute("pageLoginContext");
    OpenScienceFrameworkLoginContext osfLoginContext = OpenScienceFrameworkLoginContext.fromJson(loginContext);
    pageContext.setAttribute("osfLoginContext", osfLoginContext);
%>

<html lang="en">
    <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <title>OSF | Sign In </title>

        <spring:theme code="standard.custom.css.file" var="customCssFile" />
        <link rel="stylesheet" href="<c:url value="${customCssFile}" />" />
        <link rel="icon" href="<c:url value="/favicon.ico" />" type="image/x-icon" />

        <!--[if lt IE 9]>
            <script src="//cdnjs.cloudflare.com/ajax/libs/html5shiv/3.6.1/html5shiv.js" type="text/javascript"></script>
        <![endif]-->

        <link href='https://fonts.googleapis.com/css?family=Open+Sans:400,600,300,700' rel='stylesheet' type='text/css'>
    </head>

    <script>
        function resizeCasContent() {
            if (!document.getElementById("inst-login")) {
                return;
            }
            const casContent = document.getElementById("content");
            if (window.innerWidth >= 420) {
                casContent.style.width = "360px";
            } else {
                casContent.style.width = "90%";
            }
        }
    </script>

    <body id="cas" onresize="resizeCasContent()">

        <div id="container">
            <br/>
            <header>
                <div class="center">
                    <spring:eval var="osfUrl" expression="@casProperties.getProperty('osf.url')" />
                    <a id="logo" class="center" href="${osfUrl}" title="<spring:message code="logo.title" />">OSF | Sign In</a>
                </div>
                <br>
                <div class="center">
                    <span id="title">
                        <c:choose>
                            <c:when test="${osfLoginContext.isInstitutionLogin()}">
                                <span>OSF Institutions</span>
                            </c:when>
                            <c:otherwise>
                                <span>OSF</span>
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>
                <div class="responsive">
                    <div id="description">
                        <br><br>
                        <c:choose>
                            <c:when test="${osfLoginContext.isInstitutionLogin()}">
                                <spring:message code="screen.institution.login.message" />
                            </c:when>
                            <c:otherwise>
                                <spring:message code="screen.osf.login.message" />
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </header>
            <br>
            <div id="content">
