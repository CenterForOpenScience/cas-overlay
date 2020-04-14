package io.cos.cas.mock;

import io.cos.cas.authentication.OpenScienceFrameworkCredential;

import org.jasig.cas.CentralAuthenticationService;

import org.json.JSONObject;

/**
 * This class mocks the {@code OpenScienceFrameworkPrincipalFromRequestRemoteUserNonInteractiveCredentialsAction} class.
 *
 * @author Longze Chen
 * @since  19.3.0
 */
public class MockNormalizeRemotePrincipal extends MockOsfRemoteAuthenticateAction {

    private String fullname;

    /** Default Constructor. */
    public MockNormalizeRemotePrincipal(final CentralAuthenticationService centralAuthenticationService) {
        super(centralAuthenticationService);
        this.fullname = "";
    }

    @Override
    protected JSONObject normalizeRemotePrincipal(final OpenScienceFrameworkCredential credential) {

        final JSONObject provider = new JSONObject();
        final JSONObject user = new JSONObject();
        user.put("username", credential.getUsername());
        user.put("fullname", this.fullname);
        provider.put("id", credential.getInstitutionId());
        provider.put("user", user);

        return new JSONObject().put("provider", provider);
    }

    public void setFullname(final String fullname) {
        this.fullname = fullname;
    }
}
