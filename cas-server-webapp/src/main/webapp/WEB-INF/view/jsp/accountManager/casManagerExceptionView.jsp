<jsp:directive.include file="includes/top.jsp" />

<div id="msg" class="errors">
    <h2><spring:message code="screen.exception.invalidRequest.heading" /></h2>
    <p><spring:message code="screen.exception.invalidRequest.message" /></p>
</div>

<c:set var="alternativeBottomLogin" value="true"/>

<jsp:directive.include file="includes/bottom.jsp" />
