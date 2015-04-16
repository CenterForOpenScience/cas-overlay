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
<div id="login" style="width: 100%;">
    <form:form method="post" id="fm1" commandName="${commandName}" htmlEscape="true">

        <h2>Time-based One-time Password Authentication</h2>
        <%-- <div>
            The purpose of this policy is to establish acceptable and unacceptable use of electronic devices and network resources in conjunction with the established culture of ethical and lawful behavior, openness, trust, and integrity.

            <p>
                By using these resources, you agree to abide by the Acceptable Usage Policy.
            </p>

            <p>Click '<spring:message code="screen.aup.button.accept" />' to continue. Otherwise, click '<spring:message code="screen.aup.button.cancel" />'.</p>
        </div> --%>

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

        <section class="row btn-row">
            <input type="hidden" name="lt" value="${loginTicket}" />
            <input type="hidden" name="execution" value="${flowExecutionKey}" />
            <input type="hidden" name="_eventId" value="submit" />

            <input class="btn-submit" name="submit" accesskey="l" value="<spring:message code="screen.welcome.button.login" />" tabindex="4" type="submit" />
            <input class="btn-reset" name="reset" accesskey="c" value="<spring:message code="screen.welcome.button.clear" />" tabindex="5" type="reset" />
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
<jsp:directive.include file="includes/bottom.jsp" />
