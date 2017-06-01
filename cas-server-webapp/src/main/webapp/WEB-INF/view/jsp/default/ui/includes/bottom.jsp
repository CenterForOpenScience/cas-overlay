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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

    <c:if test= "${empty alternativeBottomNone}">
        <div class="row" style="text-align: center;">
            <hr><br>
            <c:choose>
                <c:when test="${not empty alternativeBottomLogout}">
                    <spring:eval var="casLogoutUrl" expression="@casProperties.getProperty('cas.logout.url')" />
                    <a id="alternative-osf" href="${casLogoutUrl}">Sign Out</a>
                </c:when>
                <c:when test="${not empty alternativeBottomLogin}">
                    <spring:eval var="osfLoginUrl" expression="@casProperties.getProperty('cas.osf.login.url')" />
                    <a id="alternative-osf" href="${osfLoginUrl}service=${serviceUrl}">Sign In</a>
                </c:when>
                <c:otherwise>
                    <c:choose>
                        <c:when test="${loginManagerContext.isInstitutionLogin()}">
                            <spring:eval var="defaultLoginUrl" expression="@casProperties.getProperty('cas.osf.login.url')" />
                            <a id="alternative-osf" href="${defaultLoginUrl}service=${serviceUrl}">Non-institution&nbsp;Login</a>
                        </c:when>
                        <c:otherwise>
                            <spring:eval var="institutionLoginUrl" expression="@casProperties.getProperty('cas.institution.login.url')" />
                            <a id="alternative-institution" href="${institutionLoginUrl}&service=${serviceUrl}">Login&nbsp;through&nbsp;Your&nbsp;Institution</a>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>
            <spring:eval var="osfUrl" expression="@casProperties.getProperty('osf.url')" />
            &nbsp;&nbsp;&nbsp;&nbsp;<a id="back-to-osf" href="${osfUrl}">Back&nbsp;to&nbsp;OSF</a><br>
        </div>
    </c:if>

</div>

<!-- END #content -->

<div class="row" style="text-align: center;">
    <br>
    <c:if test= "${empty alternativeBottomNone}">
        <c:if test="${empty alternativeBottomLogout}">
            <div class="row" style="text-align: center;">
                <br>
                <spring:eval var="createAccountUrl" expression="@casProperties.getProperty('cas.osf.register.url')" />
                <a id="create-account" href="${createAccountUrl}service=${serviceUrl}">Create&nbsp;Account</a>
            </div>
        </c:if>
    </c:if>
</div>

<footer>
    <%-- <div id="copyright">
        <p><spring:message code="copyright" /></p>
        <p>Powered by <a href="http://www.apereo.org/cas">Apereo Central Authentication Service <%=org.jasig.cas.CasVersion.getVersion()%></a></p>
    </div> --%>
    <div class="copyright">
        <div class="row">
            <p>Copyright &copy; 2011-2017 <a href="https://cos.io">Center for Open Science</a> |
                <a href="https://github.com/CenterForOpenScience/centerforopenscience.org/blob/master/TERMS_OF_USE.md">Terms of Use</a> |
                <a href="https://github.com/CenterForOpenScience/centerforopenscience.org/blob/master/PRIVACY_POLICY.md">Privacy Policy</a>
            </p>
        </div>
    </div>
</footer>

</div> <!-- END #container -->

<script>
    function selectFocus() {
        var fullname = document.getElementById("fullname");
        var username = document.getElementById("username");
        var email = document.getElementById("email");
        var verificationCode = document.getElementById("verificationCode");
        var institutionSelect = document.getElementById("institution-form-select")
        if (fullname) {
            fullname.focus();
        } else if (username) {
            username.focus();
        } else if (verificationCode) {
            verificationCode.focus();
        } else if (email) {
            email.focus();
        } else if (institutionSelect) {
            institutionSelect.focus();
        }
    }
</script>

<script src="https://cdnjs.cloudflare.com/ajax/libs/headjs/1.0.3/head.min.js"></script>
<spring:theme code="cas.javascript.file" var="casJavascriptFile" text="" />
<script type="text/javascript" src="<c:url value="${casJavascriptFile}" />"></script>
<spring:eval var="googleAnalyticsId" expression="@casProperties.getProperty('google.analytics.id')" />
<c:if test="${not empty googleAnalyticsId}">
    <script>
        (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
        m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
        })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

        ga('create', "${googleAnalyticsId}", 'auto', {'allowLinker': true});
        ga('require', 'linker');
        <spring:eval var="googleAutoLink" expression="@casProperties.getProperty('google.analytics.autoLink')" />
        <c:if test="${not empty googleAutoLink}">
            ga('linker:autoLink', ['${googleAutoLink}'] );
        </c:if>
        ga('send', 'pageview');
    </script>
</c:if>
</body>
</html>
