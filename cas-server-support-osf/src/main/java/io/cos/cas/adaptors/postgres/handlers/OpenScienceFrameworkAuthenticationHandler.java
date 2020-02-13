/*
 * Copyright (c) 2016. Center for Open Science
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cos.cas.adaptors.postgres.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.cos.cas.adaptors.postgres.daos.OpenScienceFrameworkDaoImpl;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkGuid;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkTimeBasedOneTimePassword;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkUser;
import io.cos.cas.authentication.exceptions.AccountNotConfirmedIdPLoginException;
import io.cos.cas.authentication.exceptions.AccountNotConfirmedOsfLoginException;
import io.cos.cas.authentication.exceptions.InvalidVerificationKeyException;
import io.cos.cas.authentication.exceptions.OneTimePasswordFailedLoginException;
import io.cos.cas.authentication.exceptions.OneTimePasswordRequiredException;
import io.cos.cas.authentication.OpenScienceFrameworkCredential;
import io.cos.cas.authentication.exceptions.ShouldNotHappenException;
import io.cos.cas.authentication.oath.TotpUtils;

import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.handler.NoOpPrincipalNameTransformer;
import org.jasig.cas.authentication.handler.PrincipalNameTransformer;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;


/**
 * The Open Science Framework Authentication handler.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 19.0.0
 */
public class OpenScienceFrameworkAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler
        implements InitializingBean {

    // time-based one time password parameters
    private static final int TOTP_INTERVAL = 30;
    private static final int TOTP_WINDOW = 1;

    // user status
    private static final String USER_ACTIVE = "ACTIVE";
    private static final String USER_NOT_CONFIRMED_OSF = "NOT_CONFIRMED_OSF";
    private static final String USER_NOT_CONFIRMED_IDP = "NOT_CONFIRMED_IDP";
    private static final String USER_NOT_CLAIMED = "NOT_CLAIMED";
    private static final String USER_MERGED = "MERGED";
    private static final String USER_DISABLED = "DISABLED";
    private static final String USER_STATUS_UNKNOWN = "UNKNOWN";

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
        final String userStatus = verifyUserStatus(user);

        // Verify the user's credential
        // 1. Verification passes right away if the credential is created via authentication delegation using protocols
        //    such as OAuth, CAS, SAML, etc, in which case the condition `credential.remotePrincipal == true` holds.
        // 2. Otherwise, check either password-based or verification-key-based login, which are mutually exclusive.
        if (!credential.isRemotePrincipal()) {
            if (plainTextPassword != null) {
                // Verify password if password exists
                if (!verifyPassword(plainTextPassword, user.getPassword())) {
                    throw new FailedLoginException(username + ": invalid password");
                }
            } else if (verificationKey != null) {
                // Verify verification key if verification key exists
                if (!verificationKey.equals(user.getVerificationKey())) {
                    throw new InvalidVerificationKeyException(username +": invalid verification key");
                }
            } else {
                // This should never happen to the users during normal CAS login flow unless there is an undiscovered
                // internal bug or the login request is programmatically crafted.
                throw new FailedLoginException(username + ": absent remote principal");
            }
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

        // Check user's status, and only ACTIVE user can sign in
        if (USER_NOT_CONFIRMED_OSF.equals(userStatus)) {
            throw new AccountNotConfirmedOsfLoginException(username + " is registered but not confirmed");
        } else if (USER_NOT_CONFIRMED_IDP.equals(userStatus)) {
            throw new AccountNotConfirmedIdPLoginException(username + " is registered via external IdP but not confirmed ");
        }  else if (USER_DISABLED.equals(userStatus)) {
            throw new AccountDisabledException(username + " is disabled");
        } else if (USER_NOT_CLAIMED.equals(userStatus)) {
            throw new ShouldNotHappenException(username + " is not claimed");
        } else if (USER_MERGED.equals(userStatus)) {
            throw new ShouldNotHappenException("Cannot log in to a merged user " + username);
        } else if (USER_STATUS_UNKNOWN.equals(userStatus)) {
            throw new ShouldNotHappenException(username + " is not active: unknown status");
        }
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("username", user.getUsername());
        attributes.put("givenName", user.getGivenName());
        attributes.put("familyName", user.getFamilyName());

        // CAS returns the user's GUID to OSF
        // Note: GUID is recommended. Do not use user's pimary key or username.
        final OpenScienceFrameworkGuid guid = openScienceFrameworkDao.findGuidByUser(user);
        return createHandlerResult(credential, this.principalFactory.createPrincipal(guid.getGuid(), attributes), null);
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
     * Check and verify user status.
     *
     * USER_ACTIVE:             The user is active.
     *
     * USER_NOT_CONFIRMED_OSF:  The user is created via default username / password sign-up but not confirmed.
     *
     * USER_NOT_CONFIRMED_IDP:  The user is created via via external IdP (e.g. ORCiD) login but not confirmed.
     *
     * USER_NOT_CLAIMED:        The user is created as an unclaimed contributor but not claimed.
     *
     * USER_DISABLED:           The user has been deactivated.
     *
     * USER_MERGED:             The user has been merged into another user.
     *
     * USER_STATUS_UNKNOWN:     Unknown or invalid status. This usually indicates that there is something wrong with
     *                          the OSF-CAS auth logic and / or the OSF user model.
     *
     * @param user an {@link OpenScienceFrameworkUser} instance
     * @return a {@link String} that represents the user status
     */
    private String verifyUserStatus(final OpenScienceFrameworkUser user) {

        // An active user must be registered, not disabled, not merged and has a not null password.
        // Only active users can pass the verification.
        if (user.isActive()) {
            logger.info("User Status Check: {}", USER_ACTIVE);
            return USER_ACTIVE;
        } else {
            // If the user instance is neither registered nor not confirmed, it can be either an unclaimed contributor
            // or a newly created user pending confirmation.
            if (!user.isRegistered() && !user.isConfirmed()) {
                if (isUnusablePassword(user.getPassword())) {
                    // If the user instance has an unusable password but also has a pending external identity "CREATE"
                    // confirmation, it must be an unconfirmed user created via external IdP login.
                    try {
                        if (isCreatedByExternalIdp(user.getExternalIdentity())) {
                            logger.info("User Status Check: {}", USER_NOT_CONFIRMED_IDP);
                            return USER_NOT_CONFIRMED_IDP;
                        }
                    } catch (final ShouldNotHappenException e) {
                        logger.error("User Status Check: {}", USER_STATUS_UNKNOWN);
                        return USER_STATUS_UNKNOWN;
                    }
                    // If the user instance has an unusable password without any pending external identity "CREATE"
                    // confirmation, it must be an unclaimed contributor.
                    logger.info("User Status Check: {}", USER_NOT_CLAIMED);
                    return USER_NOT_CLAIMED;
                } else if (checkPasswordPrefix(user.getPassword())) {
                    // If the user instance has a password with a valid prefix, it must be a unconfirmed user who
                    // has registered for a new account.
                    logger.info("User Status Check: {}", USER_NOT_CONFIRMED_OSF);
                    return USER_NOT_CONFIRMED_OSF;
                }
            }
            // If the user instance has been merged by another user, it stays registered and confirmed. The username is
            // GUID and the password is unusable. `.merged_by` being not null is a sufficient condition.
            if (user.isMerged()) {
                logger.info("User Status Check: {}", USER_MERGED);
                return USER_MERGED;
            }
            // If the user instance is disabled, it is also not registered. `.date_disabled` being not null is a
            // sufficient condition. It still has the original username and password. When the user tries to login with
            // correct username and password, an "Account Disabled" message will be displayed.
            if (user.isDisabled()) {
                logger.info("User Status Check: {}", USER_DISABLED);
                return USER_DISABLED;
            }

            // Other status combinations are considered UNKNOWN. This should not happen unless 1) there is bug in the
            // user model and/or 2) the user model has been changed but CAS fails to catch up.
            logger.info("User Status Check: {}", USER_STATUS_UNKNOWN);
            return USER_STATUS_UNKNOWN;
        }
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
     * Check if the user instance is created by an external identity provider and is pending confirmation.
     *
     * @param externalIdentity a {@link JsonObject} that stores all external identities of a user instance
     * @return {@code true} if so and {@code false} otherwise
     * @throws ShouldNotHappenException if {@code externalIdentity} fails JSON parsing.
     */
    private boolean isCreatedByExternalIdp(final JsonObject externalIdentity) throws ShouldNotHappenException {

        for (final Map.Entry<String, JsonElement> provider : externalIdentity.entrySet()) {
            try {
                for (final Map.Entry<String, JsonElement> identity : provider.getValue().getAsJsonObject().entrySet()) {
                    if (!identity.getValue().isJsonPrimitive()) {
                        throw new ShouldNotHappenException();
                    }
                    if ("CREATE".equals(identity.getValue().getAsString())) {
                        logger.info("New and unconfirmed OSF user: {} : {}", identity.getKey(), identity.getValue().toString());
                        return true;
                    }
                }
            } catch (final IllegalStateException e) {
                throw new ShouldNotHappenException();
            }
        }
        return false;
    }

    /**
     * Check if the password hash is "django-unusable".
     *
     * @param passwordHash the password hash
     * @return true if unusable, false otherwise
     */
    private boolean isUnusablePassword(final String passwordHash) {
        return passwordHash == null || passwordHash.startsWith("!");
    }

    /**
     * Check if the password hash bears a valid prefix.
     *
     * @param passwordHash the password hash
     * @return true if usable, false otherwise
     */
    private boolean checkPasswordPrefix(final String passwordHash) {
        return passwordHash != null && (passwordHash.startsWith("bcrypt$") || passwordHash.startsWith("bcrypt_sha256$"));
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
