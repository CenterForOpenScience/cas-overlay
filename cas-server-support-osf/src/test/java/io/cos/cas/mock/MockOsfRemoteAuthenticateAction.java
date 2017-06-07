package io.cos.cas.mock;

import io.cos.cas.AbstractTestUtils;
import io.cos.cas.api.handler.ApiEndpointHandler;
import io.cos.cas.authentication.OpenScienceFrameworkCredential;
import io.cos.cas.web.flow.OpenScienceFrameworkPrincipalFromRequestRemoteUserNonInteractiveCredentialsAction;

import org.jasig.cas.CentralAuthenticationService;

import javax.security.auth.login.AccountException;

/**
 * This class mocks the {@link OpenScienceFrameworkPrincipalFromRequestRemoteUserNonInteractiveCredentialsAction} class.
 *
 * @author Longze Chen
 * @since  4.1.5
 */
public class MockOsfRemoteAuthenticateAction
    extends OpenScienceFrameworkPrincipalFromRequestRemoteUserNonInteractiveCredentialsAction {

    private static final String CAS_LOGIN_URL = "http://caslogin/";
    private static final String CAS_CREATE_OR_LINK_ACCOUNT_URL = "http://casCreateOrLink/";
    private static final String INSTITUTION_AUTH_URL = "http://institutionauth/";
    private static final String INSTITUTION_AUTH_JWE_SECRET = "osf_api_cas_login_jwe_secret_32b";
    private static final String INSTITUTION_AUTH_JWT_SECRET = "osf_api_cas_login_jwt_secret_32b";
    private static final String INSTITUTION_AUTH_XSL_LOCATION = "file:mock-institutions-auth.xsl";

    /** Constructor. */
    public MockOsfRemoteAuthenticateAction(
            final CentralAuthenticationService centralAuthenticationService
    ) {

        this.setCentralAuthenticationService(centralAuthenticationService);
        this.setApiEndpointHandler(
                new ApiEndpointHandler(
                        CAS_LOGIN_URL,
                        CAS_CREATE_OR_LINK_ACCOUNT_URL,
                        INSTITUTION_AUTH_URL,
                        INSTITUTION_AUTH_JWE_SECRET,
                        INSTITUTION_AUTH_JWT_SECRET
                )
        );
        this.setInstitutionsAuthXslLocation(INSTITUTION_AUTH_XSL_LOCATION);
    }

    @Override
    protected PrincipalAuthenticationResult notifyRemotePrincipalAuthenticated(
            final OpenScienceFrameworkCredential credential
    ) throws AccountException {
        return new PrincipalAuthenticationResult(AbstractTestUtils.CONST_MAIL, AbstractTestUtils.CONST_INSTITUTION_ID, null, null);
    }
}
