<%--

    Copyright (c) 2020. Center for Open Science

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>

<%-- Login exception page: account created via external IdP login but not confirmed --%>

<jsp:directive.include file="includes/top.jsp"/>

<div id="msg" class="errors">
    <h2><spring:message code="screen.accountnotconfirmed.idplogin.heading"/></h2>
    <p><spring:message code="screen.accountnotconfirmed.idplogin.message"/></p>
</div>

<spring:message code="screen.osf.login.message.error" var="errorDescription"/>
<script>
    description = document.getElementById("description");
    if (description != null) {
        description.innerHTML = "<br><br>${errorDescription}";
    }
</script>

<c:set var="linkSignIn" value="true"/>
<c:set var="linkSignOut" value="false"/>
<c:set var="linkCreateAccount" value="true"/>
<c:set var="linkBackToOsf" value="true"/>

<jsp:directive.include file="includes/bottom.jsp"/>
