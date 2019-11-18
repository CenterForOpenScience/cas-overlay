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

<%-- CAS OAuth Authorize Page --%>

<jsp:directive.include file="../../default/ui/includes/top.jsp"/>

<div class="question" id="login">
    <form id="fm1" method="GET" action="<c:url value="${callbackUrl}" />">
        <div class="oauth-confirm-header"><spring:message code="screen.oauth.confirm.header"/></div>
        <br/>
        <br/>
        <div>
            <spring:message code="screen.oauth.confirm.message" arguments="${fn:escapeXml(serviceName)}"/>
        </div>
        <p>
        <ul style="padding-left: 15px;">
            <c:forEach items="${scopeMap}" var="scope">
                <li class="scope scope-${fn:escapeXml(scope.key)}">
                    <h3>${fn:escapeXml(scope.value.description)}</h3>
                </li>
            </c:forEach>
        </ul>
        </p>
        <section class="row btn-row">
            <input type="submit" class="btn-oauth-submit" name="action" accesskey="a"
                   value="<spring:message code="screen.oauth.confirm.allow" />"/>
            <input type="submit" class="btn-oauth-reset" name="action" accesskey="d"
                   value="<spring:message code="screen.oauth.confirm.deny" />"/>
        </section>
    </form>
</div>

<c:set var="linkSignIn" value="false"/>
<c:set var="linkSignOut" value="false"/>
<c:set var="linkCreateAccount" value="false"/>
<c:set var="linkBackToOsf" value="false"/>

<jsp:directive.include file="../../default/ui/includes/bottom.jsp"/>
