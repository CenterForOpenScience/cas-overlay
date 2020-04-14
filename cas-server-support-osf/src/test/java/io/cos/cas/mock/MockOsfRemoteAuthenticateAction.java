package io.cos.cas.mock;

import io.cos.cas.authentication.handler.support.OpenScienceFrameworkPrincipalFromRequestRemoteUserNonInteractiveCredentialsAction;

import org.jasig.cas.CentralAuthenticationService;

/**
 * This class mocks the {@link OpenScienceFrameworkPrincipalFromRequestRemoteUserNonInteractiveCredentialsAction} class.
 *
 * @author Longze Chen
 * @since  19.3.0
 */
public class MockOsfRemoteAuthenticateAction
        extends OpenScienceFrameworkPrincipalFromRequestRemoteUserNonInteractiveCredentialsAction {

    protected static final String INSTITUTION_AUTH_URL = "institution_auth_url";
    protected static final String INSTITUTION_AUTH_JWE_SECRET = "osf_api_cas_login_jwe_secret_32b";
    protected static final String INSTITUTION_AUTH_JWT_SECRET = "osf_api_cas_login_jwt_secret_32b";
    protected static final String INSTITUTION_AUTH_XSL_LOCATION = "file:mock-institutions-auth.xsl";

    /** Constructor. */
    public MockOsfRemoteAuthenticateAction(
            final CentralAuthenticationService centralAuthenticationService
    ) {
        this.setCentralAuthenticationService(centralAuthenticationService);
        this.setInstitutionsAuthUrl(INSTITUTION_AUTH_URL);
        this.setInstitutionsAuthJweSecret(INSTITUTION_AUTH_JWE_SECRET);
        this.setInstitutionsAuthJwtSecret(INSTITUTION_AUTH_JWT_SECRET);
        this.setInstitutionsAuthXslLocation(INSTITUTION_AUTH_XSL_LOCATION);
    }
}
