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
<jsp:directive.include file="../../default/ui/includes/top.jsp" />
<style>
    #login .btn-reset {
        background: #ddd;
        font-weight: bold;
    }
</style>
<div class="question" id="login">
    <form id="fm1" method="GET" action="${callbackUrl}">
        <h2><spring:message code="screen.oauth.confirm.header" /></h2>
        <p>
           <spring:message code="screen.oauth.confirm.message" arguments="${serviceName}" />
        </p>
        <p>
            <ul style="padding-left: 15px;">
                <c:forEach items="${scopeMap}" var="scope">
                    <li class="scope scope-${scope.key}">
                        <h3>${scope.value.description}</h3>
                    </li>
                </c:forEach>
            </ul>
        </p>
        <section class="row btn-row">
            <input class="btn-submit" style="width: inherit;" name="action" accesskey="a" value="<spring:message code="screen.oauth.confirm.allow" />" type="submit" />
            <input class="btn-reset" style="display: inline-block;" name="action" accesskey="d" value="<spring:message code="screen.oauth.confirm.deny" />" type="submit" />
        </section>
    </form>
</div>
<jsp:directive.include file="../../default/ui/includes/bottom.jsp" />
