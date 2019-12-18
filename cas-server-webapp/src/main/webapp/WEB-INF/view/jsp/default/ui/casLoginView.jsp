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

<%-- Default login page for OSF --%>

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

<div class="box" id="login">
    <form:form method="post" id="fm1" commandName="${commandName}" htmlEscape="true">

        <%-- ORCiD Login --%>
        <c:if test="${not empty registeredService}">
            <%-- ORCiD login is enabled for the following services: OSF, OSF Preprints and Registries, Branded Preprints
            with OSF or branded domains, Prereg and ERPC.  ORCiD login is disabled for CAS itself and for OAuth. --%>
            <c:if test="${not empty registeredService.id && (
                registeredService.id == 203948234207230 || registeredService.id == 203948234207231 ||
                registeredService.id == 203948234207232 || registeredService.id == 203948234207340 ||
                (registeredService.id >= 203948234207240 && registeredService.id <= 203948234207272)
            )}">
                <section class="row">
                    <a id="alt-login-orcid" class="btn-alt-login" href="${OrcidClientUrl}">
                        <img class="orcid-logo" src="../images/orcid-logo.png">
                        <span class="label-login"><spring:message code="screen.welcome.button.login.orcid"/></span>
                    </a>
                </section>
            </c:if>
        </c:if>

        <%-- Institution Login --%>
        <spring:eval var="institutionLoginUrl" expression="@casProperties.getProperty('cas.institution.login.url')"/>
        <c:set var="serviceParam" value="&service=${osfLoginContext.isServiceUrl() ? osfLoginContext.getServiceUrl() : ''}"/>
        <section class="row">
            <a id="alt-login-inst" class="btn-alt-login" href="${institutionLoginUrl}${serviceParam}">
                <img class="osf-alt-logo" src="../images/institution-logo.png">
                <span class="label-login"><spring:message code="screen.welcome.button.login.institution"/></span>
            </a>
        </section>

        <section class="row">
            <hr class="hr-text" data-content="OR" />
        </section>

        <%-- OSF Username and Password Login --%>
        <section class="row">
            <label for="username" style="display: none"><spring:message code="screen.welcome.label.netid"/></label>
            <c:choose>
                <c:when test="${not empty sessionScope.openIdLocalId}">
                    <strong><c:out value="${sessionScope.openIdLocalId}"/></strong>
                    <input type="hidden" id="username" name="username" value="<c:out value="${sessionScope.openIdLocalId}"/>"/>
                </c:when>
                <c:otherwise>
                    <spring:message code="screen.welcome.label.netid.accesskey" var="userNameAccessKey"/>
                    <form:input type="email" cssClass="required" cssErrorClass="error" id="username" size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username" autocomplete="off" htmlEscape="true" placeholder="Email" autofocus="autofocus"/>
                </c:otherwise>
            </c:choose>
        </section>
        <section class="row">
            <label for="password" style="display: none"><spring:message code="screen.welcome.label.password"/></label>
            <spring:message code="screen.welcome.label.password.accesskey" var="passwordAccessKey"/>
            <form:password cssClass="required" cssErrorClass="error" id="password" size="25" tabindex="2" path="password" accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" placeholder="Password"/>
            <span id="capslock-on" style="display:none;"> <p><img src="images/warning.png" valign="top"><spring:message code="screen.capslock.on"/></p></span>
        </section>
        <form:errors path="*" id="msg" cssClass="errors" element="div" htmlEscape="false"/>
        <section class="row btn-row">
            <input type="hidden" name="lt" value="${loginTicket}"/>
            <input type="hidden" name="execution" value="${flowExecutionKey}"/>
            <input type="hidden" name="_eventId" value="submit"/>
            <input type="submit" class="btn-submit" name="submit" accesskey="l" tabindex="4" value="<spring:message code="screen.welcome.button.login"/>"/>
        </section>
        <section class="row check">
            <input type="checkbox" name="rememberMe" id="rememberMe" value="true" checked tabindex="5"/>
            <label for="rememberMe"><spring:message code="screen.rememberme.checkbox.title"/></label>
            <spring:eval var="forgotPasswordUrl" expression="@casProperties.getProperty('osf.forgotPassword.url')"/>
            <a id="forgot-password" class='need-help' href="${forgotPasswordUrl}" title="<spring:message code="logo.title" />"><spring:message code="screen.general.link.forgotPassword"/></a>
        </section>

    </form:form>
</div>

<c:set var="linkSignIn" value="false"/>
<c:set var="linkSignOut" value="false"/>
<c:set var="linkCreateAccount" value="true"/>
<c:set var="linkBackToOsf" value="true"/>

<jsp:directive.include file="includes/bottom.jsp"/>
