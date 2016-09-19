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

package io.cos.cas.adaptors.postgres.handlers;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkTimeBasedOneTimePassword;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkUser;
import io.cos.cas.adaptors.postgres.daos.OpenScienceFrameworkDaoImpl;
import io.cos.cas.authentication.LoginNotAllowedException;
import io.cos.cas.authentication.OneTimePasswordFailedLoginException;
import io.cos.cas.authentication.OneTimePasswordRequiredException;
import io.cos.cas.authentication.OpenScienceFrameworkCredential;

import io.cos.cas.authentication.oath.TotpUtils;
import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.NoOpPrincipalNameTransformer;
import org.jasig.cas.authentication.handler.PrincipalNameTransformer;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;


/**
 * The Open Science Framework Authentication handler.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.0
 */
public class OpenScienceFrameworkAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler
        implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(OpenScienceFrameworkAuthenticationHandler.class);

    private static final int TOTP_INTERVAL = 30;
    private static final int TOTP_WINDOW = 1;

    @NotNull
    private PrincipalNameTransformer principalNameTransformer = new NoOpPrincipalNameTransformer();

    @NotNull
    private OpenScienceFrameworkDaoImpl openScienceFrameworkDao;

    /** Default Constructor */
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
        final String transformedUsername = principalNameTransformer.transform(osfCredential.getUsername());
        if (transformedUsername == null) {
            throw new AccountNotFoundException("Transformed username is null.");
        }
        osfCredential.setUsername(transformedUsername);
        return authenticateInternal(osfCredential);
    }

    /**
     * Authenticates an Open Science Framework credential.
     *
     * @param credential the credential object bearing the username, password, etc...
     *
     * @return HandlerResult resolved from credential on authentication success or null if no principal could be resolved
     * from the credential.
     *
     * @throws GeneralSecurityException On authentication failure.
     * @throws PreventedException On the indeterminate case when authentication is prevented.
     */
    protected final HandlerResult authenticateInternal(final OpenScienceFrameworkCredential credential)
            throws GeneralSecurityException, PreventedException {

        final String username = credential.getUsername().toLowerCase();
        final String plainTextPassword = credential.getPassword();
        final String verificationKey = credential.getVerificationKey();
        final String oneTimePassword = credential.getOneTimePassword();

        OpenScienceFrameworkUser user = openScienceFrameworkDao.findOneUserByUsername(username);

        if (user == null) {
            throw new AccountNotFoundException(username + " not found with query");
        }

        Boolean validPassphrase = Boolean.FALSE;
        if (credential.isRemotePrincipal()) {
            // remote principals are already verified by a third party (in our case a third party SAML authentication).
            validPassphrase = Boolean.TRUE;
        } else if (verificationKey != null && verificationKey.equals(user.getVerificationKey())) {
            // verification key can substitute as a temporary password.
            validPassphrase = Boolean.TRUE;
        } else if (BCrypt.checkpw(plainTextPassword, user.getPassword())) {
            //  valid password
            validPassphrase = Boolean.TRUE;
        }
        if (!validPassphrase) {
            throw new FailedLoginException(username + " invalid verification key or password");
        }

        final OpenScienceFrameworkTimeBasedOneTimePassword timeBasedOneTimePassword
                = openScienceFrameworkDao.findOneTimeBasedOneTimePasswordByOwnerId(user.getId());

        // if the user has set up two factors authentication
        if (timeBasedOneTimePassword != null
                && timeBasedOneTimePassword.getTotpSecret() != null
                && timeBasedOneTimePassword.isConfirmed()
                && !timeBasedOneTimePassword.isDeleted()) {
            // redirect to `casOtpLoginView`
            if (oneTimePassword == null) {
                throw new OneTimePasswordRequiredException("Time-based One Time Password required");
            }

            try {
                final Long longOneTimePassword = Long.valueOf(oneTimePassword);
                if (!TotpUtils.checkCode(timeBasedOneTimePassword.getTotpSecretBase32(), longOneTimePassword, TOTP_INTERVAL, TOTP_WINDOW)) {
                    throw new OneTimePasswordFailedLoginException(username + " invalid time-based one time password");
                }
            } catch (final Exception e) {
                throw new OneTimePasswordFailedLoginException(username + " invalid time-based one time password");
            }
        }

        // Validate basic information such as username/password and a potential one time password
        // before providing any indication of account status.
        if (!user.isRegistered()) {
            throw new LoginNotAllowedException(username + " is not registered");
        }
        if (!user.isClaimed()) {
            throw new LoginNotAllowedException(username + " is not claimed");
        }
        if (user.isMerged()) {
            throw new LoginNotAllowedException("Cannot log in to a merged user " + username);
        }
        if (user.isDisabled()) {
            throw new AccountDisabledException(username + " is disabled");
        }
        if (!user.isActive()) {
            throw new LoginNotAllowedException(username + " is not active");
        }

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("username", user.getUsername());
        attributes.put("givenName", user.getGivenName());
        attributes.put("familyName", user.getFamilyName());
        return createHandlerResult(credential, this.principalFactory.createPrincipal(user.getUsername(), attributes), null);
    }

    public void setPrincipalNameTransformer(final PrincipalNameTransformer principalNameTransformer) {
        this.principalNameTransformer = principalNameTransformer;
    }

    public void setOpenScienceFrameworkDao(final OpenScienceFrameworkDaoImpl openScienceFrameworkDao) {
        this.openScienceFrameworkDao = openScienceFrameworkDao;
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
