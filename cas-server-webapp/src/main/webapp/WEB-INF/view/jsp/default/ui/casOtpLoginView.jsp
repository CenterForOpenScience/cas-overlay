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
                        <td><img id="service-logo" src="${registeredService.logo}"> </td>
                        <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
                        <td><span class="service-name">${registeredService.name} Preprints</span></td>
                    </c:if>
                </tr>
            </table>
        </div>
        <p/>
    </c:if>
</c:if>

<div id="login" style="width: 100%;">
    <form:form method="post" id="fm1" commandName="${commandName}" htmlEscape="true">

        <h2>Two Factor Authentication</h2>
        <%-- <div>
            The purpose of this policy is to establish acceptable and unacceptable use of electronic devices and network resources in conjunction with the established culture of ethical and lawful behavior, openness, trust, and integrity.

            <p>
                By using these resources, you agree to abide by the Acceptable Usage Policy.
            </p>

            <p>Click '<spring:message code="screen.aup.button.accept" />' to continue. Otherwise, click '<spring:message code="screen.aup.button.cancel" />'.</p>
        </div> --%>

        <section class="row">
            <label for="oneTimePassword"><spring:message code="screen.welcome.label.passcode" /></label>
            <spring:message code="screen.welcome.label.passcode.accesskey" var="passcodeAccessKey" />
            <form:input cssClass="required" cssErrorClass="error" id="oneTimePassword" size="25" tabindex="1" accesskey="${passcodeAccessKey}" path="oneTimePassword" autocomplete="off" htmlEscape="true" />
        </section>

        <%-- <section class="row check">
            <input id="warn" name="warn" value="true" tabindex="3" accesskey="<spring:message code="screen.welcome.label.warn.accesskey" />" type="checkbox" />
            <label for="warn"><spring:message code="screen.welcome.label.warn" /></label>
        </section> --%>

        <form:errors path="*" id="msg" cssClass="errors" element="div" htmlEscape="false" />

        <section class="row btn-row">
            <input type="hidden" name="lt" value="${loginTicket}" />
            <input type="hidden" name="execution" value="${flowExecutionKey}" />
            <input type="hidden" name="_eventId" value="submit" />

            <input class="btn-submit" name="submit" accesskey="l" value="<spring:message code="screen.welcome.button.verify" />" tabindex="4" type="submit" />
            <%-- <input class="btn-reset" name="reset" accesskey="c" value="<spring:message code="screen.welcome.button.clear" />" tabindex="5" type="reset" /> --%>
        </section>

        <%-- <section class="row btn-row">
            <input type="hidden" name="execution" value="${flowExecutionKey}" />
            <input type="hidden" name="_eventId" value="submit" />
            <input class="btn-submit" name="submit" accesskey="s" value="<spring:message code="screen.mfa.button.verify" />"  type="submit" />
            <input class="btn-reset" name="cancel" accesskey="c"
                   value="<spring:message code="screen.mfa.button.cancel" />" type="button"
                   onclick="location.href = location.href;" />
        </section> --%>
    </form:form>
</div>

<c:set var="alternativeBottomNone" value="true"/>

<jsp:directive.include file="includes/bottom.jsp" />
