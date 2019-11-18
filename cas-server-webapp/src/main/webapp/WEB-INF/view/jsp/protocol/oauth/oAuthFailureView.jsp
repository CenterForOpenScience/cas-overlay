<%--

    Copyright (c) 2015. Center for Open Science

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

<jsp:directive.include file="../../default/ui/includes/top.jsp"/>

<div id="msg" class="errors">
    <h2><spring:message code="screen.oauth.error.header"/></h2>
    <p><spring:message code="screen.oauth.error.message"/></p>
    <p><spring:message code="${rootCauseException.code}"/><br/><spring:message code="${rootCauseException.message}"/></p>
</div>

<c:set var="linkSignIn" value="false"/>
<c:set var="linkSignOut" value="true"/>
<c:set var="linkCreateAccount" value="false"/>
<c:set var="linkBackToOsf" value="true"/>

<jsp:directive.include file="../../default/ui/includes/bottom.jsp"/>
