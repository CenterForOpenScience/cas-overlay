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
package io.cos.cas.adaptors.mongodb;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.cos.cas.authentication.LoginNotAllowedException;
import io.cos.cas.authentication.OneTimePasswordFailedLoginException;
import io.cos.cas.authentication.OneTimePasswordRequiredException;
import io.cos.cas.authentication.OpenScienceFrameworkCredential;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.bson.types.ObjectId;
import org.apache.commons.codec.binary.Base32;

import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.NoOpPrincipalNameTransformer;
import org.jasig.cas.authentication.handler.PrincipalNameTransformer;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;
import javax.xml.bind.DatatypeConverter;

import io.cos.cas.authentication.oath.TotpUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * The Open Science Framework Authentication handler.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public class OpenScienceFrameworkAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler
        implements InitializingBean {

    private static final int TOTP_INTERVAL = 30;
    private static final int TOTP_WINDOW = 1;

    @NotNull
    private PrincipalNameTransformer principalNameTransformer = new NoOpPrincipalNameTransformer();

    @NotNull
    private MongoOperations mongoTemplate;

    @NotNull
    private String casLoginUrl;

    @NotNull
    private String casLoginJweSecret;

    @NotNull
    private String casLoginJwtSecret;

    @Document(collection="user")
    private static class OpenScienceFrameworkUser {
        @Id
        private String id;
        private String username;
        private String password;
        @Field("verification_key")
        private String verificationKey;
        @Field("given_name")
        private String givenName;
        @Field("family_name")
        private String familyName;
        @Field("is_registered")
        private Boolean isRegistered;
        @Field("merged_by")
        private Boolean mergedBy;
        @Field("date_disabled")
        private Date dateDisabled;
        @Field("date_confirmed")
        private Date dateConfirmed;
        @Field("is_claimed")
        private Boolean isClaimed;

        public String getUsername() {
            return this.username;
        }

        public void setUsername(final String username) {
            this.username = username;
        }

        public String getPassword() {
            return this.password;
        }

        public void setPassword(final String password) {
            this.password = password;
        }

        public String getVerificationKey() {
            return this.verificationKey;
        }

        public void setVerificationKey(final String verificationKey) {
            this.verificationKey = verificationKey;
        }

        public String getGivenName() {
            return this.givenName;
        }

        public void setGivenName(final String givenName) {
            this.givenName = givenName;
        }

        public String getFamilyName() {
            return this.familyName;
        }

        public void setFamilyName(final String familyName) {
            this.familyName = familyName;
        }

        public Boolean getIsClaimed() {
            return this.isClaimed;
        }

        public void setIsClaimed(final Boolean isClaimed) {
            this.isClaimed = isClaimed;
        }

        public Boolean isMerged() {
            return this.mergedBy != null;
        }

        public Boolean isDisabled() {
            return this.dateDisabled != null;
        }

        public Boolean isConfirmed() {
            return this.dateConfirmed != null;
        }

        public Boolean isActive() {
            return this.isRegistered
                    && this.password != null
                    && !this.isMerged()
                    && !this.isDisabled()
                    && this.isConfirmed();
        }

        @Override
        public String toString() {
            return String.format("OpenScienceFrameworkUser [id=%s, username=%s]", this.id, this.username);
        }
    }

    @Document(collection="twofactorusersettings")
    private static class TimeBasedOneTimePassword {
        @Id
        private ObjectId id;
        @Field("totp_secret")
        private String totpSecret;
        @Field("is_confirmed")
        private Boolean isConfirmed;
        private Boolean deleted;

        public String getTotpSecret() {
            return this.totpSecret;
        }

        public void setTotpSecret(final String totpSecret) {
            this.totpSecret = totpSecret;
        }

        public Boolean getIsConfirmed() {
            return this.isConfirmed;
        }

        public void setIsConfirmed(final Boolean isConfirmed) {
            this.isConfirmed = isConfirmed;
        }

        public Boolean getDeleted() {
            return this.deleted;
        }

        public void setDeleted(final Boolean deleted) {
            this.deleted = deleted;
        }

        /**
         * Returns the TOTP secret in encoded as Base32.
         *
         * @return the encoded secret
         */
        public String getTotpSecretBase32() {
            final byte[] bytes = DatatypeConverter.parseHexBinary(this.totpSecret);
            return new Base32().encodeAsString(bytes);
        }

        @Override
        public String toString() {
            return String.format("TimeBasedOneTimePassword [id=%s]", this.id);
        }
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
     * Authenticates a Open Science Framework credential.
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

        if (credential.isRemotePrincipal()) {
            this.remotePrincipalAuthenticateInternal(credential);
        }

        final OpenScienceFrameworkUser user = this.mongoTemplate.findOne(new Query(
            new Criteria().orOperator(
                    Criteria.where("emails").is(username),
                    Criteria.where("username").is(username)
            )
        ), OpenScienceFrameworkUser.class);

        if (user == null) {
            throw new AccountNotFoundException(username + " not found with query");
        }

        Boolean validPassphrase = Boolean.FALSE;
        if (credential.isRemotePrincipal()) {
            // remote principal's are already verified by a third party (in our case a third party SAML authentication).
            validPassphrase = Boolean.TRUE;
        } else if (verificationKey != null && verificationKey.equals(user.verificationKey)) {
            // verification key can substitute as a temporary password.
            validPassphrase = Boolean.TRUE;
        } else if (BCrypt.checkpw(plainTextPassword, user.password)) {
            validPassphrase = Boolean.TRUE;
        }
        if (!validPassphrase) {
            throw new FailedLoginException(username + " invalid verification key or password");
        }

        final TimeBasedOneTimePassword timeBasedOneTimePassword = this.mongoTemplate.findOne(new Query(Criteria
                .where("owner").is(user.id)
                .and("isConfirmed").is(Boolean.TRUE)
                .and("deleted").is(Boolean.FALSE)
        ), TimeBasedOneTimePassword.class);

        if (timeBasedOneTimePassword != null && timeBasedOneTimePassword.totpSecret != null) {
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

        // Validate basic information such as username/password and a potential One-Time Password before
        // providing any indication of account status.
        if (!user.isRegistered) {
            throw new LoginNotAllowedException(username + " is not registered");
        }
        if (!user.isClaimed) {
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
        attributes.put("username", user.username);
        attributes.put("givenName", user.givenName);
        attributes.put("familyName", user.familyName);
        return createHandlerResult(credential, this.principalFactory.createPrincipal(user.id, attributes), null);
    }

    /**
     * Remote Principal Authentication of an Open Science Framework credential, securely notifies the OSF
     * the operation has occurred. Allowing the OSF the opportunity to create a verified user account and/or assign
     * institutional affiliation to the user's account.
     *
     * @param credential the credential object bearing the username, password, etc...
     */
    private void remotePrincipalAuthenticateInternal(final OpenScienceFrameworkCredential credential) {
        try {
            // Build a JWT and wrap it with JWE for secure transport to the OSF API.
            final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(credential.getUsername())
                    .claim("data", new ObjectMapper().writeValueAsString(credential.getAuthenticationHeaders()))
                    .expirationTime(new Date(new Date().getTime() + 60 * 1000))
                    .build();

            final SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

            final JWSSigner signer = new MACSigner(this.casLoginJwtSecret.getBytes());
            signedJWT.sign(signer);

            final JWEObject jweObject = new JWEObject(
                    new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
                            .contentType("JWT")
                            .build(),
                    new Payload(signedJWT));
            jweObject.encrypt(new DirectEncrypter(this.casLoginJweSecret.getBytes()));
            final String jweString = jweObject.serialize();

            // A call is made to the OSF CAS Institution Login Endpoint to create a registered user (if
            // one does not already exist) and apply institutional affiliation.
            final HttpResponse httpResponse = Request.Post(this.casLoginUrl)
                    .bodyString(jweString, ContentType.APPLICATION_JSON)
                    .execute()
                    .returnResponse();
            logger.info(
                    "Remote Principal Authenticate (OSF API) Response: <{}> Status Code {}",
                    credential.getUsername(),
                    httpResponse.getStatusLine().getStatusCode()
            );
        } catch (JOSEException | IOException e) {
            // log the error and return the user to the login flow
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public void setPrincipalNameTransformer(final PrincipalNameTransformer principalNameTransformer) {
        this.principalNameTransformer = principalNameTransformer;
    }

    public void setMongoTemplate(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void setCasLoginUrl(final String casLoginUrl) {
        this.casLoginUrl = casLoginUrl;
    }

    public void setCasLoginJweSecret(final String casLoginJweSecret) {
        this.casLoginJweSecret = casLoginJweSecret;
    }

    public void setCasLoginJwtSecret(final String casLoginJwtSecret) {
        this.casLoginJwtSecret = casLoginJwtSecret;
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
