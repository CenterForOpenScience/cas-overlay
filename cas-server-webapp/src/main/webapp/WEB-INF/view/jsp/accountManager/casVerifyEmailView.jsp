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

        <c:set var="externalIdP" value="${credential.getNonInstitutionExternalIdProvider()}"/>
        <c:set var="externalId" value="${credential.getNonInstitutionExternalId()}" />

        <div id="msg" class="success">
            <c:if test="${flowExecutionUrl.contains('/account/register?')}">
                <p><spring:message code="screen.verifyEmail.register.success.message" /></p>
            </c:if>
            <c:if test="${flowExecutionUrl.contains('/account/findAccount?')}">
                <p><spring:message code="screen.verifyEmail.resend.success.message" /></p>
            </c:if>
            <c:if test="${flowExecutionUrl.contains('/account/createOrLinkOsfAccount?')}">
                <c:if test="${accountManagerContext.getTarget() == 'CREATE'}">
                    <p><spring:message code="screen.verifyEmail.externalAuthCreate.success.message" arguments="${externalIdP}" /></p>
                </c:if>
                <c:if test="${accountManagerContext.getTarget() == 'LINK'}">
                    <p><spring:message code="screen.verifyEmail.externalAuthLink.success.message" arguments="${externalIdP}"/></p>
                </c:if>
            </c:if>
        </div>

        <h2><spring:message code="screen.challenge.header" /></h2>

        <section class="row">
            <label for="emailToVerify"><spring:message code="screen.challenge.label.primaryEmail" /></label><br/>
            <spring:message code="screen.challenge.label.email.primary.accesskey" var="emailAccessKey" />
            <form:input disabled="true" value="${accountManagerContext.getEmailToVerify()}" cssClass="required" cssErrorClass="error" id="emailToVerify" size="25" tabindex="1" accesskey="${emailAccessKey}" path="emailToVerify" autocomplete="off" htmlEscape="true" />
        </section>

        <section class="row">
            <label for="verificationCode"><spring:message code="screen.challenge.label.verificationCode" /></label><br/>
            <spring:message code="screen.challenge.label.verificationCode.accesskey" var="verificationCodeAccessKey" />
            <form:input cssClass="required" cssErrorClass="error" id="verificationCode" size="25" tabindex="1" accesskey="${verificationCodeAccessKey}" path="verificationCode" autocomplete="off" htmlEscape="true" />
            <form:errors path="verificationCode" id="msg" cssClass="errors" element="div" htmlEscape="false" />
        </section>

        <form:errors path="action" id="msg" cssClass="errors" element="div" htmlEscape="false" />

        <section class="row btn-row">
            <input type="hidden" name="action" value="${accountManagerContext.getAction()}" />
            <input type="hidden" name="lt" value="${loginTicket}" />
            <input type="hidden" name="execution" value="${flowExecutionKey}" />
            <input type="hidden" name="_eventId" value="submit" />
            <input class="btn-submit" name="submit" accesskey="l" value="<spring:message code="screen.challenge.button.submit" />" tabindex="4" type="submit" />
        </section>

    </form:form>
</div>

<c:set var="alternativeBottomLogin" value="true"/>

<jsp:directive.include file="includes/bottom.jsp" />
