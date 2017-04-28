/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.cos.cas.authentication.handler;

import java.security.GeneralSecurityException;
import java.util.Map;

import io.cos.cas.api.OpenScienceFrameworkApiCasEndpoint;
import io.cos.cas.api.AbstractOpenScienceFrameworkApiUtils;
import io.cos.cas.authentication.OpenScienceFrameworkCredential;
import io.cos.cas.authentication.exceptions.OneTimePasswordFailedLoginException;
import io.cos.cas.authentication.exceptions.OneTimePasswordRequiredException;
import io.cos.cas.authentication.exceptions.RegistrationFailureUserAlreadyExistsException;
import io.cos.cas.authentication.exceptions.RegistrationSuccessConfirmationRequiredException;

import io.cos.cas.authentication.exceptions.ShouldNotHappenException;
import io.cos.cas.authentication.exceptions.UserNotClaimedException;
import io.cos.cas.authentication.exceptions.UserNotConfirmedException;
import io.cos.cas.types.ApiEndpoint;
import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.NoOpPrincipalNameTransformer;
import org.jasig.cas.authentication.handler.PrincipalNameTransformer;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;

import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;

/**
 * The Open Science Framework Authentication handler.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public class OpenScienceFrameworkAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler
        implements InitializingBean {

    /** The Principle Name Transformer instance. */
    @NotNull
    private PrincipalNameTransformer principalNameTransformer = new NoOpPrincipalNameTransformer();

    /** The Open Science Framework API CAS Endpoint instance. */
    @NotNull
    private OpenScienceFrameworkApiCasEndpoint osfApiCasEndpoint;

    /**
     * Default Constructor.
     */
    public OpenScienceFrameworkAuthenticationHandler() {}

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    protected final HandlerResult doAuthentication(final Credential credential)
            throws GeneralSecurityException, PreventedException {
        final OpenScienceFrameworkCredential osfCredential = (OpenScienceFrameworkCredential) credential;
        if (osfCredential.getUsername() == null) {
            throw new AccountNotFoundException("Username is null.");
        }
        final String transformedUsername = this.principalNameTransformer.transform(osfCredential.getUsername());
        if (transformedUsername == null) {
            throw new AccountNotFoundException("Transformed username is null.");
        }
        osfCredential.setUsername(transformedUsername);
        return authenticateInternal(osfCredential);
    }

    /**
     * Authenticates or Register a Open Science Framework credential.
     *
     * @param credential the credential object bearing the username, password, etc...
     * @return HandlerResult resolved from credential on authentication success, or
     *         null if no principal could be resolved from the credential.
     * @throws GeneralSecurityException On authentication failure.
     * @throws PreventedException On the indeterminate case when authentication is prevented.
     */
    protected final HandlerResult authenticateInternal(final OpenScienceFrameworkCredential credential)
            throws GeneralSecurityException, PreventedException {

        final String username = credential.getUsername().toLowerCase();
        final String plainTextPassword = credential.getPassword();
        final String verificationKey = credential.getVerificationKey();
        final String oneTimePassword = credential.getOneTimePassword();
        final Boolean createAccount = credential.getCreateAccount();
        final String fullname = credential.getFullname();
        final String campaign = credential.getCampaign();

        final JSONObject user = new JSONObject();
        final JSONObject data = new JSONObject();
        final JSONObject payload = new JSONObject();

        final ApiEndpoint endpoint;
        user.put("email", username);
        user.put("password", plainTextPassword);

        if (createAccount) {
            user.put("fullname", fullname);
            user.put("campaign", campaign);
            data.put("type", "REGISTER");
            endpoint = ApiEndpoint.AUTH_REGISTER;
        } else {
            user.put("verificationKey", verificationKey);
            user.put("remoteAuthenticated", credential.isRemotePrincipal());
            user.put("oneTimePassword", oneTimePassword);
            data.put("type", "LOGIN");
            endpoint = ApiEndpoint.AUTH_LOGIN;
        }

        data.put("user", user);
        payload.put("data", data);
        final String encryptedPayload = osfApiCasEndpoint.encryptPayload("data", data.toString());

        final Map<String, Object> response = osfApiCasEndpoint.apiCasAuthentication(endpoint, username, encryptedPayload);
        if (response == null || !response.containsKey("status")) {
            throw new FailedLoginException("I/O Exception: an error has occurred during the api authentication process.");
        }

        final String status = (String) response.get("status");
        if (AbstractOpenScienceFrameworkApiUtils.AUTHENTICATION_SUCCESS.equals(status)) {
            // authentication success, create principle with user's guid and attributes
            final String userId = (String) response.get("userId");
            final Map<String, Object> attributes = (Map<String, Object>) response.get("attributes");
            return createHandlerResult(credential, this.principalFactory.createPrincipal(userId, attributes), null);
        } else if (AbstractOpenScienceFrameworkApiUtils.REGISTRATION_SUCCESS.equals(status)) {
            // registration success, requires confirmation
            throw new RegistrationSuccessConfirmationRequiredException();
        } else if (AbstractOpenScienceFrameworkApiUtils.AUTHENTICATION_FAILURE.equals(status)) {
            // authentication or registration failure
            final String errorDetail = (String) response.get("detail");
            if (AbstractOpenScienceFrameworkApiUtils.ALREADY_REGISTERED.equals(errorDetail)) {
                throw new RegistrationFailureUserAlreadyExistsException();
            } else if (AbstractOpenScienceFrameworkApiUtils.TWO_FACTOR_AUTHENTICATION_REQUIRED.equals(errorDetail)) {
                throw new OneTimePasswordRequiredException("Time-based One Time Password required.");
            } else if (AbstractOpenScienceFrameworkApiUtils.INVALID_ONE_TIME_PASSWORD.equals(errorDetail)) {
                throw new OneTimePasswordFailedLoginException();
            } else if (AbstractOpenScienceFrameworkApiUtils.ACCOUNT_NOT_FOUND.equals(errorDetail)) {
                throw new AccountNotFoundException();
            } else if (AbstractOpenScienceFrameworkApiUtils.INVALID_PASSWORD.equals(errorDetail)
                    || AbstractOpenScienceFrameworkApiUtils.INVALID_VERIFICATION_KEY.equals(errorDetail)) {
                throw new FailedLoginException();
            } else if (AbstractOpenScienceFrameworkApiUtils.USER_NOT_CONFIRMED.equals(errorDetail)) {
                throw new UserNotConfirmedException(username + " is registered but not confirmed");
            } else if (AbstractOpenScienceFrameworkApiUtils.USER_NOT_CLAIMED.equals(errorDetail)) {
                throw new UserNotClaimedException(username + " is not claimed");
            } else if (AbstractOpenScienceFrameworkApiUtils.USER_STATUS_INVALID.equals(errorDetail)) {
                throw new ShouldNotHappenException(username + " is not active");
            } else if (AbstractOpenScienceFrameworkApiUtils.USER_DISABLED.equals(errorDetail)) {
                throw new AccountDisabledException(username + "account is disabled");
            } else {
                // unknown authentication exception
                throw new FailedLoginException("I/O Exception: an error has occurred during the api authentication process.");
            }
        } else {
            // unknown authentication status
            throw new FailedLoginException("I/O Exception: an error has occurred during the api authentication process.");
        }
    }

    public void setPrincipalNameTransformer(final PrincipalNameTransformer principalNameTransformer) {
        this.principalNameTransformer = principalNameTransformer;
    }

    public void setOsfApiCasEndpoint(final OpenScienceFrameworkApiCasEndpoint osfApiCasEndpoint) {
        this.osfApiCasEndpoint = osfApiCasEndpoint;
    }

    /**
     * {@inheritDoc}
     * @return True if credential is a {@link OpenScienceFrameworkCredential}, false otherwise.
     */
    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OpenScienceFrameworkCredential;
    }
}
