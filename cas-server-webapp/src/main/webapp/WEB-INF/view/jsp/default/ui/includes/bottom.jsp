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

    <div class="row" style="text-align: center;">
        <hr>
        <spring:eval var="osfUrl" expression="@casProperties.getProperty('osf.url')" />
        <a href="${osfUrl}">Back to OSF</a>
    </div>
</div> <!-- END #content -->


<footer>
    <%-- <div id="copyright">
        <p><spring:message code="copyright" /></p>
        <p>Powered by <a href="http://www.apereo.org/cas">Apereo Central Authentication Service <%=org.jasig.cas.CasVersion.getVersion()%></a></p>
    </div> --%>
    <div class="copyright">
        <div class="row">
            <p>Copyright &copy; 2011-2015 <a href="http://centerforopenscience.org">Center for Open Science</a> |
                <a href="https://github.com/CenterForOpenScience/centerforopenscience.org/blob/master/TERMS_OF_USE.md">Terms of Use</a> |
                <a href="https://github.com/CenterForOpenScience/centerforopenscience.org/blob/master/PRIVACY_POLICY.md">Privacy Policy</a>
            </p>
        </div>
    </div>
</footer>

</div> <!-- END #container -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/headjs/1.0.3/head.min.js"></script>
<spring:theme code="cas.javascript.file" var="casJavascriptFile" text="" />
<script type="text/javascript" src="<c:url value="${casJavascriptFile}" />"></script>
</body>
</html>

