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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.cos.cas.api.handler.ApiEndpointHandler;
import io.cos.cas.api.util.AbstractApiEndpointUtils;
import io.cos.cas.authentication.OpenScienceFrameworkCredential;
import io.cos.cas.authentication.exceptions.InvalidVerificationKeyException;
import io.cos.cas.authentication.exceptions.OneTimePasswordFailedLoginException;
import io.cos.cas.authentication.exceptions.OneTimePasswordRequiredException;

import io.cos.cas.authentication.exceptions.ShouldNotHappenException;
import io.cos.cas.authentication.exceptions.AccountNotVerifiedException;
import io.cos.cas.api.type.ApiEndpoint;
import org.apache.http.HttpStatus;
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

    @NotNull
    private PrincipalNameTransformer principalNameTransformer = new NoOpPrincipalNameTransformer();

    @NotNull
    private ApiEndpointHandler apiEndpointHandler;

    /** Default Constructor. */
    public OpenScienceFrameworkAuthenticationHandler() {}

    @Override
    public void afterPropertiesSet() throws Exception {}

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

        final JSONObject user = new JSONObject();
        final JSONObject data = new JSONObject();
        final JSONObject payload = new JSONObject();

        user.put("email", username);
        user.put("password", plainTextPassword);
        user.put("verificationKey", verificationKey);
        user.put("remoteAuthenticated", credential.isRemotePrincipal());
        user.put("oneTimePassword", oneTimePassword);
        data.put("type", "LOGIN");
        data.put("user", user);
        payload.put("data", data);

        final JSONObject response = apiEndpointHandler.handle(
                ApiEndpoint.AUTH_LOGIN,
                apiEndpointHandler.encryptPayload("data", data.toString())
        );

        if (response == null) {
            throw new ShouldNotHappenException("Null response from API login endpoint.");
        }
        final int statusCode = response.getInt("status");

        // login success
        if (statusCode == HttpStatus.SC_OK) {
            final JSONObject responseBody = response.getJSONObject("body");
            if (responseBody == null || !responseBody.has("userId") || !responseBody.has("attributes")) {
                throw new ShouldNotHappenException("Invalid" + statusCode +  "response body from API login endpoint");
            }
            final String userId = responseBody.getString("userId");
            final JSONObject attributes = responseBody.getJSONObject("attributes");
            final Map<String, Object> attributesMap = new HashMap<>();
            final Iterator<String> keys = attributes.keys();
            while (keys.hasNext()) {
                final String key = keys.next();
                attributesMap.put(key, attributes.getString(key));
            }
            return createHandlerResult(credential, this.principalFactory.createPrincipal(userId, attributesMap), null);
        }
        // login failure
        if (statusCode == HttpStatus.SC_UNAUTHORIZED || statusCode == HttpStatus.SC_FORBIDDEN) {
            final JSONObject responseBody = response.getJSONObject("body");
            final String errorDetail = apiEndpointHandler.getErrorMessageFromResponseBody(responseBody);
            if (errorDetail == null) {
                throw new ShouldNotHappenException("Invalid" + statusCode +  "response body from API login endpoint");
            }

            if (AbstractApiEndpointUtils.ACCOUNT_NOT_FOUND.equals(errorDetail)) {
                throw new AccountNotFoundException();
            } else if (AbstractApiEndpointUtils.TFA_REQUIRED.equals(errorDetail)) {
                throw new OneTimePasswordRequiredException();
            } else if (AbstractApiEndpointUtils.INVALID_PASSWORD.equals(errorDetail)) {
                throw new FailedLoginException();
            } else if (AbstractApiEndpointUtils.INVALID_KEY.equals(errorDetail)) {
                throw new InvalidVerificationKeyException();
            } else if (AbstractApiEndpointUtils.INVALID_TOTP.equals(errorDetail)) {
                throw new OneTimePasswordFailedLoginException();
            } else if (AbstractApiEndpointUtils.ACCOUNT_NOT_VERIFIED.equals(errorDetail)) {
                throw new AccountNotVerifiedException();
            } else if (AbstractApiEndpointUtils.ACCOUNT_DISABLED.equals(errorDetail)) {
                throw new AccountDisabledException();
            } else if (AbstractApiEndpointUtils.INVALID_ACCOUNT_STATUS.equals(errorDetail)) {
                throw new ShouldNotHappenException();
            }
        }

        throw new ShouldNotHappenException();
    }

    public void setPrincipalNameTransformer(final PrincipalNameTransformer principalNameTransformer) {
        this.principalNameTransformer = principalNameTransformer;
    }

    public void setApiEndpointHandler(final ApiEndpointHandler apiEndpointHandler) {
        this.apiEndpointHandler = apiEndpointHandler;
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
