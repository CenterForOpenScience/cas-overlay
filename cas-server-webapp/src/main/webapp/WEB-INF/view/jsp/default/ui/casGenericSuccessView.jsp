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

<%-- Generic no-service successful login page --%>

<jsp:directive.include file="includes/top.jsp"/>

<div id="msg" class="success">
    <h2><spring:message code="screen.success.header"/></h2>
    <br/>
    <c:set var="displayName" value="${principal.attributes.username}"/>
    <c:if test="${empty displayName}">
        <c:set var="displayName" value="${principal.id}"/>
    </c:if>
    <p><spring:message code="screen.success.success" arguments="${displayName}"/></p>
    <p><spring:message code="screen.success.security"/></p>
</div>

<spring:message code="screen.osf.login.message.continue" arguments="${osfUrl}" var="successDescription"/>
<script>
    description = document.getElementById("description");
    if (description != null) {
        description.innerHTML = "<br><br>${successDescription}";
    }
</script>

<c:set var="linkSignIn" value="false"/>
<c:set var="linkSignOut" value="true"/>
<c:set var="linkCreateAccount" value="false"/>
<c:set var="linkBackToOsf" value="false"/>

<jsp:directive.include file="includes/bottom.jsp"/>
