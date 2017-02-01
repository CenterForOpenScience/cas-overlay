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

<%-- TODO: Remove this div after fix merge conflicts --%>
    <div id="msg" class="errors">
        <c:choose>
            <c:when test="${osfLoginContext.getHandleErrorName() == 'UserNotConfirmedException'}">
                <spring:eval var="osfResendConfirmationUrl" expression="@casProperties.getProperty('osf.resendConfirmation.url')" />
                <h2><spring:message code="screen.loginnotallowed.usernotconfirmed.heading" /></h2>
                <p><spring:message code="screen.loginnotallowed.usernotconfirmed.message" arguments="${osfResendConfirmationUrl}" /></p>
            </c:when>
            <c:when test="${osfLoginContext.getHandleErrorName() == 'AccountDisabledException'}">
                <h2><spring:message code="screen.loginnotallowed.userdisabled.heading" /></h2>
                <p><spring:message code="screen.loginnotallowed.userdisabled.message" /></p>
            </c:when>
            <c:when test="${osfLoginContext.getHandleErrorName() == 'UserNotClaimedException'}">
                <h2><spring:message code="screen.loginnotallowed.usernotclaimed.heading" /></h2>
                <p><spring:message code="screen.loginnotallowed.usernotclaimed.message" /></p>
            </c:when>
            <c:when test="${osfLoginContext.getHandleErrorName() == 'UserAlreadyMergedException'}">
                <h2><spring:message code="screen.loginnotallowed.usermerged.heading" /></h2>
                <p><spring:message code="screen.loginnotallowed.usermerged.message" /></p>
            </c:when>
            <c:when test="${osfLoginContext.getHandleErrorName() == 'UserNotActiveException'}">
                <h2><spring:message code="screen.loginnotallowed.usernotactive.heading" /></h2>
                <p><spring:message code="screen.loginnotallowed.usernotactive.message" /></p>
            </c:when>
            <c:otherwise>
                <h2><spring:message code="screen.loginnotallowed.unknown.heading" /></h2>
                <p><spring:message code="screen.loginnotallowed.unknown.message" /></p>
            </c:otherwise>
        </c:choose>
    </div>

<%-- TODO: Keep and update this div after fix merge conflicts --%>
    <div id="msg" class="errors">
        <spring:eval var="osfResendConfirmationUrl" expression="@casProperties.getProperty('osf.resendConfirmation.url')" />
        <h2><spring:message code="screen.loginnotallowed.heading" /></h2>
        <p><spring:message code="screen.loginnotallowed.message" arguments="${osfResendConfirmationUrl}" /></p>
    </div>

<c:set var="alternativeBottomLogin" value="true"/>

<jsp:directive.include file="includes/bottom.jsp" />
