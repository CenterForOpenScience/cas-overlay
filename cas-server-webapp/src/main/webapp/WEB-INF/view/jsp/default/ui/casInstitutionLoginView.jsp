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

<div id="inst-login">
    <section class="row">
        <div class="heading">
            <span><spring:message code="screen.institution.login.heading" /></span>
        </div>
    </section><br>

    <section class="row">
        <div class="select">
            <label for="select-institution">Select Your Institution</label>
        </div>
    </section>

    <section class="row">
        <div class="select">
            <form:select class="select" id="institution-form-select" disabled="true" name="select-institution" path="institutions" items="${institutions}" />
        </div>
    </section>

    <section class="row check">
        <input type="checkbox" name="consent" id="consent" value="true" tabindex="5" onchange="checkConsent(this)" />
        <label for="consent"><spring:message code="screen.register.checkbox.consent.title" /></label>
    </section><br>

    <section class="row">
        <input id="inst-submit" type="button" class="btn-submit" name="submit" disabled value="<spring:message code="screen.welcome.button.login"/>" onclick="institutionLogin()">
    </section><br>

    <script>
        function institutionLogin () {
            var form = document.getElementById('institution-form-select');
            var login_url = form.options[form.selectedIndex].value;
            if(login_url == null || login_url == "") {
                return;
            } else if (login_url == "okstate") {
                login_url = "${okstateUrl}";
            }
            window.location = login_url;
        }

        function checkConsent(checkbox) {
            var selectForm = document.getElementById('institution-form-select');
            var submitButton = document.getElementById('inst-submit');
            selectForm.disabled = !checkbox.checked;
            submitButton.disabled = !checkbox.checked;
        }
    </script>
</div>

<jsp:directive.include file="includes/bottom.jsp" />
