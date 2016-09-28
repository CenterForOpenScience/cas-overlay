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

        final OpenScienceFrameworkUser user = openScienceFrameworkDao.findOneUserByEmail(username);
        if (user == null) {
            throw new AccountNotFoundException(username + " not found with query");
        }

        Boolean validPassphrase = Boolean.FALSE;

        if (credential.isRemotePrincipal()) {
            // verified through remote principals
            validPassphrase = Boolean.TRUE;
        } else if (verificationKey != null && verificationKey.equals(user.getVerificationKey())) {
            // verified by verification key
            validPassphrase = Boolean.TRUE;
        } else if (verifyPassword(plainTextPassword, user.getPassword())) {
            // verified by password
            validPassphrase = Boolean.TRUE;
        }
        if (!validPassphrase) {
            throw new FailedLoginException(username + ": invalid remote authentication, verification key or password");
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
                throw new OneTimePasswordFailedLoginException(username + ": invalid time-based one time password");
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

        // CAS returns the user's postgres primary key string to OSF
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
     * Verify Password. `bcrypt$` (backward compatibility) and `bcrypt_sha256$` are the only two valid prefix.
     *
     * @param plainTextPassword the plain text password provided by the user
     * @param userPasswordHash the password hash stored in database
     * @return True if verified, False otherwise
     */
    private boolean verifyPassword(final String plainTextPassword, final String userPasswordHash) {

        String password, passwordHash;

        try {
            if (userPasswordHash.startsWith("bcrypt$")) {
                // django.contrib.auth.hashers.BCryptPasswordHasher
                passwordHash = userPasswordHash.split("bcrypt\\$")[1];
                password = plainTextPassword;
            } else if(userPasswordHash.startsWith("bcrypt_sha256$")) {
                // django.contrib.auth.hashers.BCryptSHA256PasswordHasher
                passwordHash = userPasswordHash.split("bcrypt_sha256\\$")[1];
                password = sha256HashPassword(plainTextPassword);
            } else {
                // invalid password hash prefix
                return false;
            }
            passwordHash = updateBCryptHashIdentifier(passwordHash);
            return password != null && passwordHash != null && BCrypt.checkpw(password, passwordHash);
        } catch (final Exception e) {
            // Do not log stack trace which may contain user's plaintext password
            logger.error(String.format("CAS has encountered a problem when verifying the password: %s.", e.toString()));
            return false;
        }
    }

    /**
     * Hash the password using SHA256, the first step for BCryptSHA256.
     * This is dependent on django.contrib.auth.hashers.BCryptSHA256PasswordHasher.
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
            // Do not log stack trace which may contain user's plaintext password
            logger.error(String.format("CAS has encountered a problem when sha256-hashing the password: %s.", e.toString()));
            return null;
        }
    }

    /**
     * Update BCrypt Hash Identifier for Compatibility.
     *
     * Spring's BCrypt implements the specification and is not vulnerable to OpenBSD's `u_int8_t` overflow issue. How-
     * ever, it only recognizes `$2$` or `$2a$` identifier for a password BCrypt hash. The solution is to replace `$2b$`
     * or `$2y$` with `$2a` in the hash before calling `BCrypt.checkpw()`. This is correct and secure.
     *
     * @param passwordHash the password hash by BCrypt or BCryptSHA256
     * @return the spring compatible hash string or null
     */
    private String updateBCryptHashIdentifier(final String passwordHash) {
        try {
            if (passwordHash.charAt(2) != '$') {
                final StringBuilder builder = new StringBuilder(passwordHash);
                builder.setCharAt(2, 'a');
                return builder.toString();
            }
            return passwordHash;
        } catch (final Exception e) {
            // Do not log stack trace which may contain user's plaintext password
            logger.error(String.format("CAS has encountered a problem when updating password hash identifier: %s.", e.toString()));
            return null;
        }
    }
}
