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

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkAuthenticationHandler.class);

    private static final int TOTP_INTERVAL = 30;
    private static final int TOTP_WINDOW = 1;

    @NotNull
    private PrincipalNameTransformer principalNameTransformer = new NoOpPrincipalNameTransformer();

    @NotNull
    private OpenScienceFrameworkDaoImpl openScienceFrameworkDao;

    /** Default Constructor. */
    public OpenScienceFrameworkAuthenticationHandler() {}

    /**
     * @param principalNameTransformer the principal name transformer.
     */
    public void setPrincipalNameTransformer(final PrincipalNameTransformer principalNameTransformer) {
        this.principalNameTransformer = principalNameTransformer;
    }

    /**
     * @param openScienceFrameworkDao the open science framework data access object
     */
    public void setOpenScienceFrameworkDao(final OpenScienceFrameworkDaoImpl openScienceFrameworkDao) {
        this.openScienceFrameworkDao = openScienceFrameworkDao;
    }

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

        // TO-DO: handle the case user provide non-username email
        final OpenScienceFrameworkUser user = openScienceFrameworkDao.findOneUserByUsername(username);

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
        } else if (verifyPassword(plainTextPassword, user.getPassword())) {
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
            // if no one time password is provided in credential, redirect to `casOtpLoginView`
            if (oneTimePassword == null) {
                throw new OneTimePasswordRequiredException("Time-based One Time Password required");
            }
            // verify one time password
            try {
                final Long longOneTimePassword = Long.valueOf(oneTimePassword);
                if (!TotpUtils.checkCode(timeBasedOneTimePassword.getTotpSecretBase32(), longOneTimePassword, TOTP_INTERVAL, TOTP_WINDOW)) {
                    throw new OneTimePasswordFailedLoginException(username + " invalid time-based one time password");
                }
            } catch (final Exception e) {
                throw new OneTimePasswordFailedLoginException(username + " invalid time-based one time password");
            }
        }

        // Validate basic information such as username, password or verification key, and a potential one time password
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

        return createHandlerResult(credential, this.principalFactory.createPrincipal(user.getId().toString(), attributes), null);
    }

    /**
     * {@inheritDoc}
     * @return True if credential is a {@link OpenScienceFrameworkCredential}, false otherwise.
     */
    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OpenScienceFrameworkCredential;
    }

    /**
     * Verify Password.
     *
     * @param plainTextPassword the plain text password provided by user
     * @param userPasswordHash the password hash stored in database
     * @return True if verified, False otherwise
     */
    private boolean verifyPassword(final String plainTextPassword, final String userPasswordHash) {

        String password = plainTextPassword;
        String passwordHash = userPasswordHash;

        try {
            if (userPasswordHash.startsWith("bcrypt$")) {
                //  'django.contrib.auth.hashers.BCryptPasswordHasher'
                passwordHash = userPasswordHash.split("bcrypt\\$")[1];
            } else if(userPasswordHash.startsWith("bcrypt_sha256$")) {
                //  'django.contrib.auth.hashers.BCryptSHA256PasswordHasher'
                passwordHash = userPasswordHash.split("bcrypt_sha256\\$")[1];
                password = sha256HashPassword(plainTextPassword);
            }
            passwordHash = updateBCryptHashIdentifier(passwordHash);
            return password != null && passwordHash != null && BCrypt.checkpw(password, passwordHash);
        } catch (final Exception e) {
            // TO-DO: more specific exception handling
            logger.error(String.format("CAS fails to verify password: %s", e.toString()));
            return false;
        }
    }

    /**
     * SHA256 hash the password, the first step for BCryptSHA256.
     *
     * @param password the plain text password provided by user
     * @return the password hash in String or null
     */
    private String sha256HashPassword(final String password) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] sha256HashedPassword = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            final StringBuilder builder = new StringBuilder();
            for (final byte b : sha256HashedPassword) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (final Exception e) {
            // TO-DO: more specific exception handling
            logger.error(String.format("Message Digest Error: %s", e.toString()));
            return null;
        }
    }

    /**
     * Update BCrypt Hash Identifier for Compatibility.
     * Spring's BCrypt is not vulnerable to OpenBSD's `u_int8_t` overflow issue. However, it only recognize `$2a` for
     * password hash. This will replace `$2b` generate with `$2a`. This is secure.
     *
     * @param passwordHash the password hash by BCrypt or BCryptSHA256
     * @return the spring compatible hash in String or null
     */
    private String updateBCryptHashIdentifier(final String passwordHash) {
        try {
            final StringBuilder builder = new StringBuilder(passwordHash);
            builder.setCharAt(2, 'a');
            return builder.toString();
        } catch (final Exception e) {
            // TO-DO: more specific exception handling
            logger.error(String.format("Invalid BCrypt Hash Identifier: %s", e.toString()));
            return null;
        }
    }
}
