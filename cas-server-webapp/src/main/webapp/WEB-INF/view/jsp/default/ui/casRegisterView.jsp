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
<jsp:directive.include file="includes/top.jsp" />

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
        <p/>
    </c:if>
</c:if>

<div class="box" id="login">
    <form:form method="post" id="fm1" commandName="${commandName}" htmlEscape="true">

        <section class="row">
            <label for="fullname"><spring:message code="screen.welcome.label.netname" /></label>
            <spring:message code="screen.welcome.label.netname.accesskey" var="fullnameAccessKey" />
            <form:input cssClass="required" cssErrorClass="error" id="fullname" size="25" tabindex="1" accesskey="${fullnameAccessKey}" path="fullname" autocomplete="off" htmlEscape="true" />
        </section>

        <section class="row">
            <label for="username"><spring:message code="screen.welcome.label.netid" /></label>
            <spring:message code="screen.welcome.label.netid.accesskey" var="usernameAccessKey" />
            <form:input cssClass="required" cssErrorClass="error" id="username" size="25" tabindex="1" accesskey="${usernameAccessKey}" path="username" autocomplete="off" htmlEscape="true" />

        </section>

        <section class="row">
            <label for="usernameConfirm"><spring:message code="screen.welcome.label.netid.confirm" /></label>
            <spring:message code="screen.welcome.label.netid.confirm.accesskey" var="usernameConfirmAccessKey" />
            <form:input cssClass="required" cssErrorClass="error" id="usernameConfirm" size="25" tabindex="1" accesskey="${usernameConfirmAccessKey}" path="usernameConfirm" autocomplete="off" htmlEscape="true" />
        </section>

        <section class="row">
            <label for="password"><spring:message code="screen.welcome.label.password" />
            </label><spring:message code="screen.welcome.label.password.accesskey" var="passwordAccessKey" />
            <form:password cssClass="required" cssErrorClass="error" id="password" size="25" tabindex="2" path="password"  accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" />
            <span id="capslock-on" style="display:none;"><p><img src="images/warning.png" valign="top"> <spring:message code="screen.capslock.on" /></p></span>
        </section>

        <section class="row">
            <label for="passwordConfirm"><spring:message code="screen.welcome.label.password.confirm" />
            </label><spring:message code="screen.welcome.label.password.confirm.accesskey" var="passwordConfirmAccessKey" />
            <form:password cssClass="required" cssErrorClass="error" id="passwordConfirm" size="25" tabindex="2" path="passwordConfirm"  accesskey="${passwordConfirmAccessKey}" htmlEscape="true" autocomplete="off" />
            <span id="capslock-on" style="display:none;"><p><img src="images/warning.png" valign="top"> <spring:message code="screen.capslock.on" /></p></span>
        </section>

        <form:errors path="*" id="msg" cssClass="errors" element="div" htmlEscape="false" />

        <section class="row btn-row">
            <input type="hidden" name="lt" value="${loginTicket}" />
            <input type="hidden" name="execution" value="${flowExecutionKey}" />
            <input type="hidden" name="createAccount" value="true" />
            <input type="hidden" name="campaign" value="${osfLoginContext.getCampaign()}" />
            <input type="hidden" name="_eventId" value="submit" />


            <input type="submit" class="btn-submit" name="submit" accesskey="l" value="<spring:message code="screen.welcome.button.register" />" tabindex="4"  />
        </section>

        <%-- TODO: Only display OAuth Client options for the OSF service, due to a limitation of our OAuth Provider implementation as it does not support non-existing OSF accounts. --%>
        <c:if test="${not empty registeredService}">
            <c:if test="${not empty registeredService.id && registeredService.id == 203948234207230}">
                <hr/>
                <section class="row">
                    <a class="btn-oauth" href="${OrcidClientUrl}"><img class="orcid-logo" src="../images/orcid-logo.png">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="screen.welcome.button.register.orcid" /></a>
                </section>
            </c:if>
        </c:if>

    </form:form>
</div>

<jsp:directive.include file="includes/bottom.jsp" />
