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

        <%-- <h2><spring:message code="screen.welcome.instructions" /></h2> --%>

        <section class="row">
            <label for="username"><spring:message code="screen.welcome.label.netid" /></label>
            <c:choose>
                <c:when test="${not empty sessionScope.openIdLocalId}">
                    <strong><c:out value="${sessionScope.openIdLocalId}" /></strong>
                    <input type="hidden" id="username" name="username" value="<c:out value="${sessionScope.openIdLocalId}" />" />
                </c:when>
                <c:otherwise>
                    <spring:message code="screen.welcome.label.netid.accesskey" var="userNameAccessKey" />
                    <form:input cssClass="required" cssErrorClass="error" id="username" size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username" autocomplete="off" htmlEscape="true" />
                </c:otherwise>
            </c:choose>
        </section>

        <section class="row">
            <label for="password"><spring:message code="screen.welcome.label.password" /></label>
                <%--
                NOTE: Certain browsers will offer the option of caching passwords for a user.  There is a non-standard attribute,
                "autocomplete" that when set to "off" will tell certain browsers not to prompt to cache credentials.  For more
                information, see the following web page:
                http://www.technofundo.com/tech/web/ie_autocomplete.html
                --%>
            <spring:message code="screen.welcome.label.password.accesskey" var="passwordAccessKey" />
            <form:password cssClass="required" cssErrorClass="error" id="password" size="25" tabindex="2" path="password"  accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" />
            <span id="capslock-on" style="display:none;"><p><img src="images/warning.png" valign="top"> <spring:message code="screen.capslock.on" /></p></span>
        </section>

        <%-- <section class="row check">
            <input id="warn" name="warn" value="true" tabindex="3" accesskey="<spring:message code="screen.welcome.label.warn.accesskey" />" type="checkbox" />
            <label for="warn"><spring:message code="screen.welcome.label.warn" /></label>
        </section> --%>

        <form:errors path="*" id="msg" cssClass="errors" element="div" htmlEscape="false" />

        <section class="row btn-row">
            <input type="hidden" name="lt" value="${loginTicket}" />
            <input type="hidden" name="loginAction" value="login" />
            <input type="hidden" name="execution" value="${flowExecutionKey}" />
            <input type="hidden" name="_eventId" value="submit" />

            <input type="submit" class="btn-submit" name="submit" accesskey="l" value="<spring:message code="screen.welcome.button.login" />" tabindex="4"  />
            <%-- <input class="btn-reset" name="reset" accesskey="c" value="<spring:message code="screen.welcome.button.clear" />" tabindex="5" type="reset" /> --%>
        </section>
        <section class="row check">
            <%-- <input id="warn" name="warn" value="true" tabindex="3" accesskey="<spring:message code="screen.welcome.label.warn.accesskey" />" type="checkbox" />
            <label for="warn"><spring:message code="screen.welcome.label.warn" /></label>
            <br/>
            <input id="publicWorkstation" name="publicWorkstation" value="false" tabindex="4" type="checkbox" />
            <label for="publicWorkstation"><spring:message code="screen.welcome.label.publicstation" /></label>
            <br/> --%>
            <input type="checkbox" name="rememberMe" id="rememberMe" value="true" checked tabindex="5" />
            <label for="rememberMe"><spring:message code="screen.rememberme.checkbox.title" /></label>
            <spring:eval var="forgotPasswordUrl" expression="@casProperties.getProperty('cas.osf.resetPassword.url')" />
            <a id="forgot-password" class='need-help' href="${forgotPasswordUrl}&service=${loginManagerContext.getServiceUrl()}" title="<spring:message code="logo.title" />">Forgot Your Password?</a>
        </section>

        <%-- TODO: Only display OAuth Client options for the OSF service, due to a limitation of our OAuth Provider implementation as it does not support non-existing OSF accounts. --%>
        <c:if test="${not empty registeredService and not empty loginManagerContext.getServiceUrl()}">
            <hr/>
            <section class="row">
                <a class="btn-oauth" href="${OrcidClientUrl}"><img class="orcid-logo" src="../images/orcid-logo.png"><spring:message code="screen.welcome.button.login.orcid" /></a>
            </section>
        </c:if>

    </form:form>
</div>

<jsp:directive.include file="includes/bottom.jsp" />
