<%--

    Copyright (c) 2018. Center for Open Science

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

<jsp:directive.include file="default/ui/includes/top.jsp"/>

<%@ page isErrorPage="true" %>
<%@ page import="org.jasig.cas.web.support.WebUtils"%>
<%
    Object casAccessDeniedKey = request.getAttribute(WebUtils.CAS_ACCESS_DENIED_REASON);
    request.setAttribute("casAccessDeniedKey", casAccessDeniedKey);
    Object casAccessDeniedDetail = request.getAttribute("javax.servlet.error.message");
    request.setAttribute("casAccessDeniedDetail", casAccessDeniedDetail);
%>

<div id="msg" class="errors">
    <h2><spring:message code="screen.blocked.header" /></h2>
    <c:if test="${not empty casAccessDeniedKey}">
        <p><spring:message code="${casAccessDeniedKey}" /></p>
    </c:if>
</div>

<%-- Hide detailed error message
    <hr/><br/>
    <div id="msg" class="info">
        <c:if test="${not empty casAccessDeniedDetail}">
            <p><spring:message code="${casAccessDeniedDetail}" /></p>
        </c:if>
    </div>
 --%>

<spring:message code="screen.osf.login.message.error" var="errorDescription"/>
<script>
    description = document.getElementById("description");
    if (description != null) {
        description.innerHTML = "<br><br>${errorDescription}";
    }
</script>

<c:set var="linkSignOutandBackToSignIn" value="true"/>
<c:set var="linkSignIn" value="false"/>
<c:set var="linkSignOut" value="false"/>
<c:set var="linkCreateAccount" value="false"/>
<c:set var="linkBackToOsf" value="true"/>
<jsp:directive.include file="default/ui/includes/bottom.jsp"/>
