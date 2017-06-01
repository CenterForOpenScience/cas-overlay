package io.cos.cas.web.flow;

import io.cos.cas.AbstractTestUtils;
import io.cos.cas.authentication.OpenScienceFrameworkCredential;
import io.cos.cas.mock.MockOsfRemoteAuthenticateAction;
import io.cos.cas.types.DelegationProtocol;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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

    private static final String PAC4J_DELEGATION_PROFILE_ID = "MockProfile#0001-1234-5678";

    private static final String CONST_CAS_CLIENT_NAME = "CasClient";

    private static final String CONST_ORCID_CLIENT_NAME = "OrcidClient";

    @Test
    public void verifyInstitutionSamlShibbolethFlow() throws Exception {

        final MockHttpServletRequest mockHttpServletRequest = AbstractTestUtils.getRequestWithShibbolethHeaders();
        final MockRequestContext mockContext = AbstractTestUtils.getContextWithCredentials(mockHttpServletRequest);

        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        final MockOsfRemoteAuthenticateAction osfRemoteAuthenticate
                = new MockOsfRemoteAuthenticateAction(centralAuthenticationService);
        final Event event = osfRemoteAuthenticate.doExecute(mockContext);

        final OpenScienceFrameworkCredential credential
                = (OpenScienceFrameworkCredential) mockContext.getFlowScope().get(AbstractTestUtils.CONST_CREDENTIAL);
        assertTrue(credential.isRemotePrincipal());
        assertEquals(credential.getUsername(), AbstractTestUtils.CONST_MAIL);
        assertEquals(credential.getInstitutionId(), AbstractTestUtils.CONST_INSTITUTION_ID);
        assertEquals(credential.getDelegationProtocol(), DelegationProtocol.SAML_SHIB);
        assertEquals(
                credential.getDelegationAttributes().get(AbstractTestUtils.CONST_SHIB_IDENTITY_PROVIDER),
                AbstractTestUtils.CONST_INSTITUTION_IDP
        );
        assertEquals("success", event.getId());
    }

    @Test
    public void verifyInstitutionCasPac4jFlow() throws Exception {

        final Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AbstractTestUtils.getPrincipal(PAC4J_DELEGATION_PROFILE_ID));
        when(authentication.getAttributes()).thenReturn(AbstractTestUtils.getAuthenticationAttributes(CONST_CAS_CLIENT_NAME));

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

        assertTrue(credential.isRemotePrincipal());
        assertEquals(credential.getUsername(), AbstractTestUtils.CONST_MAIL);
        assertEquals(credential.getInstitutionId(), AbstractTestUtils.CONST_INSTITUTION_ID);
        assertEquals(credential.getDelegationProtocol(), DelegationProtocol.CAS_PAC4J);
        assertEquals(
                credential.getDelegationAttributes().get(AbstractTestUtils.CONST_CAS_IDENTITY_PROVIDER),
                CONST_CAS_CLIENT_NAME
        );
        assertEquals("success", event.getId());
    }

    @Test
    public void verifyOrcidClientFlow() throws Exception {

        final Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AbstractTestUtils.getPrincipal(PAC4J_DELEGATION_PROFILE_ID));
        when(authentication.getAttributes()).thenReturn(AbstractTestUtils.getAuthenticationAttributes(CONST_ORCID_CLIENT_NAME));

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

//        assertTrue(credential.isRemotePrincipal());
        assertEquals(credential.getDelegationProtocol(), DelegationProtocol.OAUTH_PAC4J);
//        assertEquals("success", event.getId());

//        assertNull(credential.getUsername());
        assertNull(credential.getInstitutionId());
//        assertEquals(credential.getDelegationAttributes(), Collections.EMPTY_MAP);
    }

    @Test
    public void verifyUsernameVerificationKeyFlow() throws Exception {
        final MockHttpServletRequest mockHttpServletRequest = AbstractTestUtils.getRequestWithUsernameAndVerificationKey();
        final MockRequestContext mockContext = AbstractTestUtils.getContextWithCredentials(mockHttpServletRequest);
        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        final MockOsfRemoteAuthenticateAction osfRemoteAuthenticate
                = new MockOsfRemoteAuthenticateAction(centralAuthenticationService);
        final Event event = osfRemoteAuthenticate.doExecute(mockContext);

        final OpenScienceFrameworkCredential credential
                = (OpenScienceFrameworkCredential) mockContext.getFlowScope().get(AbstractTestUtils.CONST_CREDENTIAL);

        assertEquals(credential.getUsername(), AbstractTestUtils.CONST_MAIL);
        assertEquals(credential.getVerificationKey(), AbstractTestUtils.CONST_NOT_EMPTY_STRING);
        assertEquals("success", event.getId());

        assertFalse(credential.isRemotePrincipal());
        assertNull(credential.getInstitutionId());
        assertNull(credential.getDelegationProtocol());
        assertEquals(credential.getDelegationAttributes(), Collections.EMPTY_MAP);
    }
}
