package io.cos.cas.mock;

import io.cos.cas.authentication.OpenScienceFrameworkCredential;

import org.jasig.cas.CentralAuthenticationService;

import org.json.JSONObject;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * This class mocks the {@link OpenScienceFrameworkPrincipalFromRequestRemoteUserNonInteractiveCredentialsAction} class.
 *
 * @author Longze Chen
 * @since  4.1.5
 */
public class MockNormalizeRemotePrincipal extends MockOsfRemoteAuthenticateAction {

    /** Constructor. */
    public MockNormalizeRemotePrincipal(final CentralAuthenticationService centralAuthenticationService) {
        super(centralAuthenticationService);
    }

    @Override
    protected JSONObject normalizeRemotePrincipal(final OpenScienceFrameworkCredential credential)
            throws ParserConfigurationException, TransformerException {

        final JSONObject provider = new JSONObject();
        final JSONObject user = new JSONObject();

        user.put("username", credential.getUsername());
        provider.put("id", credential.getInstitutionId());
        provider.put("user", user);

        return new JSONObject().put("provider", provider);
    }
}
