<%--

    Copyright (c) 2021. Center for Open Science

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

<%-- Unsupported Institution information page for OSF --%>

<jsp:directive.include file="includes/top.jsp"/>

<div id="inst-login">

    <script>resizeCasContent();</script>

    <section class="row">
        <span class="unsupported-inst-body-header"><spring:message code="screen.unsupportedinstitution.login.existing.heading"/></span>
    </section>
    <br/>
    <section class="row">
        <span><spring:message code="screen.unsupportedinstitution.login.existing.osf.message"/></span>
    </section>
    <br/>
    <section class="row">
        <spring:eval var="osfLoginUrl" expression="@casProperties.getProperty('cas.osf.login.url')"/>
        <c:set var="serviceParam" value="service=${osfLoginContext.isServiceUrl() ? osfLoginContext.getServiceUrl() : ''}"/>
        <a id="alt-login-osf" class="btn-alt-login" href="${osfLoginUrl}${serviceParam}">
            <img class="osf-alt-logo" src="../images/osf-logo.png">
            <span class="label-login"><spring:message code="screen.unsupportedinstitution.login.existing.osf.button"/></span>
        </a>
    </section>
    <br/>
    <spring:eval var="osfForgotPasswordInstitutionURL" expression="@casProperties.getProperty('osf.forgotPasswordInstitution.url')"/>
    <form id="fm1" method="post" action="${osfForgotPasswordInstitutionURL}">
        <section class="row">
            <span><spring:message code="screen.unsupportedinstitution.login.existing.institution.message"/></span>
        </section>
        <br/>
        <section id="login" class="row">
            <input id="username" name="forgot_password-email" class="required" tabindex="1" placeholder="Email" type="email" autofocus="autofocus" accesskey="e" value="" size="25" autocomplete="off">
        </section>
        <br/>
        <section class="row btn-row">
            <input type="submit" class="btn-submit" name="submit" accesskey="l" tabindex="4" value="Set Password">
        </section>
    </form>
</div>

<c:set var="linkSignIn" value="false"/>
<c:set var="linkSignOut" value="false"/>
<c:set var="linkCreateAccount" value="true"/>
<c:set var="linkBackToOsf" value="true"/>

<jsp:directive.include file="includes/bottom.jsp"/>
