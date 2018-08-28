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

<%-- Two-factor authententication page for OSF --%>

<jsp:directive.include file="includes/top.jsp"/>

<spring:eval var="tgcCookieSecure" expression="@casProperties.getProperty('tgc.cookie.secure')"/>
<c:if test="${not pageContext.request.secure && tgcCookieSecure}">
    <div id="msg" class="errors">
        <h2><spring:message code="screen.nonsecure.title"/></h2>
        <p><spring:message code="screen.nonsecure.message"/></p>
    </div>
</c:if>

<c:if test="${not empty registeredService}">
    <c:if test="${not empty registeredService.logo || not empty registeredService.name}">
        <div id="service-ui" class="service-info">
            <table>
                <tr>
                    <c:if test="${not empty registeredService.logo && empty registeredService.name}">
                        <td><img class="service-logo-full" src="${registeredService.logo}"></td>
                    </c:if>
                    <c:if test="${empty registeredService.logo && not empty registeredService.name}">
                        <td><span class="service-name">${registeredService.name}</span></td>
                    </c:if>
                    <c:if test="${not empty registeredService.logo && not empty registeredService.name}">
                        <td><img id="service-logo" src="${registeredService.logo}"></td>
                        <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
                        <td><span class="service-name">${registeredService.name}</span></td>
                    </c:if>
                </tr>
            </table>
        </div>
        <p/>
    </c:if>
</c:if>

<div id="login" style="width: 100%;">
    <form:form method="post" id="fm1" commandName="${commandName}" htmlEscape="true">

        <%-- 2FA One Time Passcode Verification --%>
        <h2><spring:message code="screen.2fa.login.heading"/></h2>
        <section class="row">
            <label for="oneTimePassword" style="display: none"><spring:message code="screen.welcome.label.passcode"/></label>
            <spring:message code="screen.welcome.label.passcode.accesskey" var="passcodeAccessKey"/>
            <form:input cssClass="required" cssErrorClass="error" id="oneTimePassword" size="25" tabindex="1" accesskey="${passcodeAccessKey}" path="oneTimePassword" autocomplete="off" htmlEscape="true" pattern="[0-9]{6}" maxlength="6" placeholder="6-digit one-time passcode" autofocus="autofocus"/>
        </section>
        <form:errors path="*" id="msg" cssClass="errors" element="div" htmlEscape="false"/>
        <section class="row btn-row">
            <input type="hidden" name="lt" value="${loginTicket}"/>
            <input type="hidden" name="execution" value="${flowExecutionKey}"/>
            <input type="hidden" name="_eventId" value="submit"/>
            <input class="btn-submit" name="submit" accesskey="l" tabindex="4" type="submit" value="<spring:message code="screen.2fa.button.verify" />"/>
        </section>

        <%-- Cancel 2FA and Return to OSF --%>
        <c:set var="serviceParam" value="&service=${osfLoginContext.isServiceUrl() ? osfLoginContext.getServiceUrl() : ''}"/>
        <section class="row">
            <spring:eval var="osfLoginUrl" expression="@casProperties.getProperty('cas.osf.login.url')"/>
            <a id="alt-login-cancel" class="btn-alt-login" href="${osfLoginUrl}${serviceParam}">
                <img class="osf-alt-logo" src="../images/osf-logo.png">
                <span class="label-login"><spring:message code="screen.2fa.button.cancel"/></span>
            </a>
        </section>
    </form:form>
</div>

<c:set var="linkSignIn" value="false"/>
<c:set var="linkSignOut" value="false"/>
<c:set var="linkCreateAccount" value="false"/>
<c:set var="linkBackToOsf" value="true"/>

<jsp:directive.include file="includes/bottom.jsp"/>
