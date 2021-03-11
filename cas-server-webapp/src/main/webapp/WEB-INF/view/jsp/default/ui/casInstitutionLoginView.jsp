<%--

    Copyright (c) 2016. Center for Open Science

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

<%-- Institution login page for OSF --%>

<jsp:directive.include file="includes/top.jsp"/>

<div id="inst-login">

    <c:set var="serviceParam" value="&service=${osfLoginContext.isServiceUrl() ? osfLoginContext.getServiceUrl() : ''}"/>

    <script>resizeCasContent();</script>

    <section class="row">
        <div class="heading">
            <span><spring:message code="screen.institution.login.heading"/></span>
        </div>
    </section><br/>

    <c:choose>
        <c:when test="${osfLoginContext.getInstitutionId() != null}">
            <section class="row">
                <div class="select">
                    <label for="select-institution"><spring:message code="screen.institution.login.select.auto" /></label>
                </div>
            </section>
            <section class="row">
                <div class="select">
                    <form:select class="select" id="institution-form-select" name="select-institution" path="institutions" items="${institutions}" onchange="checkSelect()" autofocus="autofocus" disabled="true"/>
                </div>
            </section>
            <spring:eval var="defaultInstitutionLoginURL" expression="@casProperties.getProperty('cas.institution.login.url')"/>
            <a id="not-your-institution" class='need-help' href="${defaultInstitutionLoginURL}${serviceParam}"><spring:message code="screen.institution.login.select.all"/></a>
            <br/>
            <c:set var="institutionIdParam" value="&institutionId=${osfLoginContext.getInstitutionId()}"/>
        </c:when>
        <c:otherwise>
            <section class="row">
                <div class="select">
                    <label for="select-institution"><spring:message code="screen.institution.login.select" /></label>
                </div>
            </section>
            <section class="row">
                <div class="select">
                    <form:select class="select" id="institution-form-select" name="select-institution" path="institutions" items="${institutions}" onchange="checkSelect()" autofocus="autofocus" disabled="false"/>
                </div>
            </section>
            <spring:eval var="unsupportedInstitutionUrl" expression="@casProperties.getProperty('cas.institution.unsupported.url')"/>
            <a id="not-your-institution" class='need-help' href="${unsupportedInstitutionUrl}${serviceParam}${institutionIdParam}"><spring:message code="screen.institution.login.select.unsupported"/></a>
            <br/>
            <c:set var="institutionIdParam" value="&institutionId="/>
        </c:otherwise>
    </c:choose>
    <br/>

    <section class="row">
        <div class="inst-message">
            <input type="checkbox" id="consent-checkbox" value="true" tabindex="5" onchange="checkConsent(this)" />
            <label for="consent-checkbox"><spring:message code="screen.institution.login.consent.checkbox" /></label>
        </div>
    </section><br/>

    <section class="row">
        <div class="inst-errors">
            <span id="select-error-message" style="display: none;"><spring:message code="screen.institution.login.select.error.message" /><br/></span>
            <span id="consent-error-message" style="display: none;"><spring:message code="screen.institution.login.consent.error.message" /></span>
        </div>
    </section><br>

    <section class="row">
        <div class="btn-submit">
            <input type="button" id="institution-login" name="submit" value="<spring:message code="screen.welcome.button.login" />" onclick="institutionLogin()">
        </div>
    </section><br/>

    <%-- OSF Username and Password Login --%>
    <hr/><br/>
    <spring:eval var="osfLoginUrl" expression="@casProperties.getProperty('cas.osf.login.url')"/>
    <section class="row">
        <a id="alt-login-osf" class="btn-alt-login" href="${osfLoginUrl}${serviceParam}${institutionIdParam}">
            <img class="osf-alt-logo" src="../images/osf-logo.png">
            <span class="label-login"><spring:message code="screen.institution.login.osf"/></span>
        </a>
    </section>

    <script>

        function institutionLogin () {

            let institutionForm = document.getElementById("institution-form-select");
            let institutionLoginUrl = institutionForm.options[institutionForm.selectedIndex].value;
            let selectErrorMessage = document.getElementById("select-error-message");

            if(institutionLoginUrl == null || institutionLoginUrl === "") {
                selectErrorMessage.style.display = "inline";
                return;
            } else if (institutionLoginUrl === "callutheran2") {
                institutionLoginUrl = "${callutheranUrl}";
            } else if (institutionLoginUrl === "okstate") {
                institutionLoginUrl = "${okstateUrl}";
            } else if (institutionLoginUrl === "cord") {
                institutionLoginUrl = "${cordUrl}"
            } else {
                // Removing the fragment part of the login URL is not necessary but a good-to-have. Thus, neither will
                // we run into nor do we need to worry about weird corner cases if somehow it breaks login endpoints.
                let lastIndexOfHash = institutionLoginUrl.lastIndexOf("#");
                if (lastIndexOfHash >= 0) {
                    institutionLoginUrl = institutionLoginUrl.substring(0, lastIndexOfHash);
                }
            }

            let consentCheckbox = document.getElementById('consent-checkbox');
            let consentErrorMessage = document.getElementById("consent-error-message");
            if (consentCheckbox != null && !consentCheckbox.checked) {
                consentErrorMessage.style.display = "inline";
            } else {
                window.location = institutionLoginUrl;
            }
        }

        function checkConsent(checkbox) {

            let consentErrorMessage = document.getElementById("consent-error-message");
            if (consentErrorMessage != null) {
                consentErrorMessage.style.display = checkbox.checked ? "none" : "inline";
            }
        }

        function checkSelect() {

            let selectErrorMessage = document.getElementById("select-error-message");
            if (selectErrorMessage != null) {
                selectErrorMessage.style.display = "none";
            }

            let consentErrorMessage = document.getElementById("consent-error-message");
            if (consentErrorMessage != null) {
                consentErrorMessage.style.display = "none";
            }
        }

    </script>
</div>

<c:set var="linkSignIn" value="false"/>
<c:set var="linkSignOut" value="false"/>
<c:set var="linkCreateAccount" value="true"/>
<c:set var="linkBackToOsf" value="true"/>

<jsp:directive.include file="includes/bottom.jsp"/>
