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

    public static final String CONST_CAS_CLIENT_NAME = "MockCasClient";

    public static final String CONST_CREDENTIAL = "credential";

    private static final String CONST_WEBFLOW_BIND_EXCEPTION =
            "org.springframework.validation.BindException.credentials";

    private static final String CONST_TICKET_GRANTING_TICKET = "ticketGrantingTicketId";

    private static final String CONST_GIVEN_NAME = "James";

    private static final String CONST_FAMILY_NAME = "Steward";

    private static final String CONST_DISPLAY_NAME = "Jimmy Steward";

    private static final String CONST_MAIL = "james@steward.com";

    public static Principal getPrincipal(final String name) {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("displayName", CONST_DISPLAY_NAME);
        attributes.put("givenName", CONST_GIVEN_NAME);
        attributes.put("familyName", CONST_FAMILY_NAME);
        attributes.put("mail", CONST_MAIL);
        return new DefaultPrincipalFactory().createPrincipal(name, attributes);
    }

    public static Map<String, Object> getAuthenticationAttributes() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("clientName", CONST_CAS_CLIENT_NAME);
        return attributes;
    }

    public static MockRequestContext getContextWithCredentials(
            final MockHttpServletRequest request,
            final String ticketGrantingTicketId
    ) {
        final MockRequestContext context = getContextWithCredentials(request, new MockHttpServletResponse());
        context.getFlowScope().put(CONST_TICKET_GRANTING_TICKET, ticketGrantingTicketId);
        return context;
    }

    private static OpenScienceFrameworkCredential getCredential() {
        return new OpenScienceFrameworkCredential();
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
            final MockHttpServletResponse response) {
        final MockRequestContext context = getContext(request, response);
        context.getFlowScope().put(CONST_CREDENTIAL, AbstractTestUtils.getCredential());
        context.getFlowScope().put(CONST_WEBFLOW_BIND_EXCEPTION, new BindException(
                AbstractTestUtils.getCredential(),
                CONST_CREDENTIAL
        ));

        return context;
    }
}
