package io.cos.cas.mock;

import io.cos.cas.AbstractTestUtils;
import io.cos.cas.authentication.OpenScienceFrameworkCredential;

import org.jasig.cas.CentralAuthenticationService;

import javax.security.auth.login.AccountException;

/**
 * This class mocks the {@code OpenScienceFrameworkPrincipalFromRequestRemoteUserNonInteractiveCredentialsAction} class.
 *
 * @author Longze Chen
 * @since  19.3.0
 */
public class MockNotifyRemotePrincipalAuthenticated extends MockOsfRemoteAuthenticateAction {

    /** Constructor. */
    public MockNotifyRemotePrincipalAuthenticated(final CentralAuthenticationService centralAuthenticationService) {
        super(centralAuthenticationService);
    }

    @Override
    protected PrincipalAuthenticationResult notifyRemotePrincipalAuthenticated(
            final OpenScienceFrameworkCredential credential) throws AccountException {
        return new PrincipalAuthenticationResult(AbstractTestUtils.CONST_MAIL, AbstractTestUtils.CONST_INSTITUTION_ID);
    }
}
