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
<jsp:directive.include file="./includes/top.jsp" />

<div id="login" style="width: 100%;">
    <form:form method="post" id="fm1" commandName="${commandName}" htmlEscape="true">

        <c:choose>
            <c:when test="${accountManagerContext.getTarget() == 'RESET_PASSWORD'}">
                <spring:message code="screen.findAccount.resetPassword.header" var="helpHeader"/>
                <spring:message code="screen.findAccount.resetPassword.message" var="helpMessage"/>
            </c:when>
            <c:when test="${accountManagerContext.getTarget() == 'VERIFY_EMAIL'}">
                <spring:message code="screen.findAccount.verifyEmail.header" var="helpHeader"/>
                <spring:message code="screen.findAccount.verifyEmail.message" var="helpMessage"/>
            </c:when>
        </c:choose>

        <h2>${helpHeader}</h2>

        <section class="row">
            <label for="email">${helpMessage}</label><br/>
            <spring:message code="screen.findAccount.label.email.accesskey" var="emailAccessKey" />
            <form:input cssClass="required" cssErrorClass="error" id="email" size="25" tabindex="1" accesskey="${emailAccessKey}" path="email" autocomplete="off" htmlEscape="true" />
        </section>

        <form:errors path="email" id="msg" cssClass="errors" element="div" htmlEscape="false" />
        <form:errors path="action" id="msg" cssClass="errors" element="div" htmlEscape="false" />

        <section class="row btn-row">
            <input type="hidden" name="action" value="${accountManagerContext.getAction()}" />
            <input type="hidden" name="lt" value="${loginTicket}" />
            <input type="hidden" name="execution" value="${flowExecutionKey}" />
            <input type="hidden" name="_eventId" value="submit" />
            <input class="btn-submit" name="submit" accesskey="l" value="<spring:message code="screen.findAccount.button.send" />" tabindex="4" type="submit" />
        </section>

    </form:form>
</div>

<c:set var="alternativeBottomLogin" value="true"/>

<jsp:directive.include file="./includes/bottom.jsp" />
