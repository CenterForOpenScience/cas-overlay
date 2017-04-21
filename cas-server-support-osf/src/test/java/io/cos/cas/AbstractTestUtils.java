package io.cos.cas;

import io.cos.cas.authentication.OpenScienceFrameworkCredential;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.validation.BindException;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.HashMap;
import java.util.Map;

/**
 *  Abstract Test Util Class.
 *
 *  @author Longze Chen
 *  @since  4.1.5
 */
public abstract class AbstractTestUtils {

    public static final String CONST_CAS_IDENTITY_PROVIDER = "Cas-Identity-Provider";

    public static final String CONST_SHIB_IDENTITY_PROVIDER = "Shib-Identity-Provider";

    public static final String CONST_CREDENTIAL = "credential";

    public static final String CONST_MAIL = "james@steward.com";

    public static final String CONST_INSTITUTION_ID = "stewardu";

    public static final String CONST_INSTITUTION_IDP = "http://institutionidp/";

    public static final String CONST_NOT_EMPTY_STRING = "a_string_that_is_not_empty";

    private static final String REMOTE_USER = "REMOTE_USER";

    private static final String ATTRIBUTE_PREFIX = "AUTH-";

    private static final String SHIBBOLETH_SESSION_HEADER = ATTRIBUTE_PREFIX + "Shib-Session-ID";

    private static final String CONST_WEBFLOW_BIND_EXCEPTION =
            "org.springframework.validation.BindException.credentials";

    private static final String CONST_TICKET_GRANTING_TICKET = "ticketGrantingTicketId";

    private static final String CONST_GIVEN_NAME = "James";

    private static final String CONST_FAMILY_NAME = "Steward";

    private static final String CONST_DISPLAY_NAME = "Jimmy Steward";

    public static Principal getPrincipal(final String name) {
        return new DefaultPrincipalFactory().createPrincipal(name, generateAttributesMap(""));
    }

    public static Map<String, Object> getAuthenticationAttributes(final String clientName) {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("clientName", clientName);
        return attributes;
    }

    public static MockHttpServletRequest getRequestWithUsernameAndVerificationKey() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("username", CONST_MAIL);
        request.addParameter("verification_key", CONST_NOT_EMPTY_STRING);
        return request;
    }

    public static MockHttpServletRequest getRequestWithShibbolethHeaders() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(SHIBBOLETH_SESSION_HEADER, CONST_NOT_EMPTY_STRING);
        request.addHeader(REMOTE_USER, AbstractTestUtils.CONST_NOT_EMPTY_STRING);
        request.addHeader(ATTRIBUTE_PREFIX + CONST_SHIB_IDENTITY_PROVIDER, CONST_INSTITUTION_IDP);
        for (final Map.Entry<String, Object> entry : generateAttributesMap(ATTRIBUTE_PREFIX).entrySet()) {
            request.addHeader(entry.getKey(), entry.getValue());
        }
        return request;
    }

    public static MockRequestContext getContextWithCredentials(final MockHttpServletRequest request) {
        return getContextWithCredentials(request, new MockHttpServletResponse());
    }

    public static MockRequestContext getContextWithCredentials(
            final MockHttpServletRequest request,
            final String ticketGrantingTicketId
    ) {
        final MockRequestContext context = getContextWithCredentials(request);
        context.getFlowScope().put(CONST_TICKET_GRANTING_TICKET, ticketGrantingTicketId);
        return context;
    }

    private static OpenScienceFrameworkCredential getCredential() {
        return new OpenScienceFrameworkCredential();
    }

    private static Map<String, Object> generateAttributesMap(final String prefix) {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(prefix + "displayName", CONST_DISPLAY_NAME);
        attributes.put(prefix + "givenName", CONST_GIVEN_NAME);
        attributes.put(prefix + "familyName", CONST_FAMILY_NAME);
        attributes.put(prefix + "mail", CONST_MAIL);
        return attributes;
    }

    private static MockRequestContext getContext(
            final MockHttpServletRequest request,
            final MockHttpServletResponse response
    ) {
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        return context;
    }

    private static MockRequestContext getContextWithCredentials(
            final MockHttpServletRequest request,
            final MockHttpServletResponse response
    ) {
        final MockRequestContext context = getContext(request, response);
        context.getFlowScope().put(CONST_CREDENTIAL, AbstractTestUtils.getCredential());
        context.getFlowScope().put(CONST_WEBFLOW_BIND_EXCEPTION, new BindException(
                AbstractTestUtils.getCredential(),
                CONST_CREDENTIAL
        ));
        return context;
    }
}
