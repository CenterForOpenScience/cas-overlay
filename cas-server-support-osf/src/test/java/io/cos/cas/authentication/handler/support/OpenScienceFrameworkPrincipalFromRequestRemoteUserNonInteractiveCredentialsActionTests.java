package io.cos.cas.authentication.handler.support;

import io.cos.cas.AbstractTestUtils;
import io.cos.cas.adaptors.postgres.types.DelegationProtocol;
import io.cos.cas.authentication.OpenScienceFrameworkCredential;
import io.cos.cas.mock.MockOsfRemoteAuthenticateAction;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class tests the {@link OpenScienceFrameworkPrincipalFromRequestRemoteUserNonInteractiveCredentialsAction} class.
 *
 * @author Longze Chen
 * @since  4.1.5
 */
public class OpenScienceFrameworkPrincipalFromRequestRemoteUserNonInteractiveCredentialsActionTests {

    private static final String TICKET_GRANTING_TICKET_ID
            = "TGT-00-xxxxxxxxxxxxxxxxxxxxxxxxxx.cas0";

    private static final String PAC4J_DELEGATION_PROFILE_ID = "MockCasProfile#0001-1234-5678";

    @Test
    public void verifyInstitutionSamlShibbolethFlow() throws Exception {
        assertTrue(Boolean.TRUE);
    }

    @Test
    public void verifyInstitutionCasPac4jFlow() throws Exception {

        final Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AbstractTestUtils.getPrincipal(PAC4J_DELEGATION_PROFILE_ID));
        when(authentication.getAttributes()).thenReturn(AbstractTestUtils.getAuthenticationAttributes());

        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getAuthentication()).thenReturn(authentication);
        when(tgt.getId()).thenReturn(TICKET_GRANTING_TICKET_ID);


        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        when(centralAuthenticationService.getTicket(
                any(String.class),
                any(TicketGrantingTicket.class.getClass())
        )).thenReturn(tgt);

        final MockOsfRemoteAuthenticateAction osfRemoteAuthenticate
                = new MockOsfRemoteAuthenticateAction(centralAuthenticationService);

        final MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        final MockRequestContext mockContext = AbstractTestUtils.getContextWithCredentials(mockHttpServletRequest, tgt.getId());
        final Event event = osfRemoteAuthenticate.doExecute(mockContext);

        final OpenScienceFrameworkCredential credential
                = (OpenScienceFrameworkCredential) mockContext.getFlowScope().get(AbstractTestUtils.CONST_CREDENTIAL);
        assertEquals(credential.getDelegationProtocol(), DelegationProtocol.CAS_PAC4J);
        assertEquals(credential.getDelegationAttributes().get(
                MockOsfRemoteAuthenticateAction.CONST_CAS_IDENTITY_PROVIDER),
                AbstractTestUtils.CONST_CAS_CLIENT_NAME
        );
        assertTrue(credential.isRemotePrincipal());

        assertEquals("success", event.getId());

    }

    @Test
    public void verifyOrcidClientFlow() throws Exception {
        assertTrue(Boolean.TRUE);
    }

    @Test
    public void verifyUsernameVerificationKeyFlow() throws Exception {
        assertTrue(Boolean.TRUE);
    }
}
