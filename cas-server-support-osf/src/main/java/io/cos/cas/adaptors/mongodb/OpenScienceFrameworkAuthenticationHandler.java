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

import java.security.GeneralSecurityException;

import io.cos.cas.authentication.LoginNotAllowedException;
import io.cos.cas.authentication.OneTimePasswordFailedLoginException;
import io.cos.cas.authentication.OneTimePasswordRequiredException;
import io.cos.cas.authentication.OpenScienceFrameworkCredential;
import org.bson.types.ObjectId;
import org.apache.commons.codec.binary.Base32;

import org.jasig.cas.authentication.*;
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

import java.util.*;

import org.jasig.cas.Message;
import org.jasig.cas.authentication.principal.Principal;

import io.cos.cas.authentication.oath.TotpUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class OpenScienceFrameworkAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler
        implements InitializingBean {

    @NotNull
    private PrincipalNameTransformer principalNameTransformer = new NoOpPrincipalNameTransformer();

    @NotNull
    private MongoOperations mongoTemplate;

    @Document(collection="user")
    private class User {
        @Id
        private String id;
        private String username;
        private String password;
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

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return this.password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getGivenName() {
            return this.givenName;
        }

        public void setGivenName(String givenName) {
            this.givenName = givenName;
        }

        public String getFamilyName() {
            return this.familyName;
        }

        public void setFamilyName(String familyName) {
            this.familyName = familyName;
        }

        public Boolean getIsClaimed() {
            return this.isClaimed;
        }

        public void setIsClaimed(Boolean isClaimed) {
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
            return "User [id=" + this.id + ", username=" + this.username + "]";
        }
    }

    @Document(collection="twofactorusersettings")
    private class TimeBasedOneTimePassword {
        @Id
        private ObjectId id;
        @Field("totp_secret")
        private String totpSecret;

        public String getTotpSecret() {
            return this.totpSecret;
        }

        public void setTotpSecret(String totpSecret) {
            this.totpSecret = totpSecret;
        }

        public String getTotpSecretBase32() {
            byte[] bytes = DatatypeConverter.parseHexBinary(this.totpSecret);
            return new Base32().encodeAsString(bytes);
        }

        @Override
        public String toString() {
            return "TimeBasedOneTimePassword [id=" + this.id + "]";
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

    protected final HandlerResult authenticateInternal(final OpenScienceFrameworkCredential credential)
            throws GeneralSecurityException, PreventedException {
        final String username = credential.getUsername();
        final String plainTextPassword = credential.getPassword();
        final String oneTimePassword = credential.getOneTimePassword();

        final User user = this.mongoTemplate.findOne(new Query(Criteria
                .where("username").is(username)
        ), User.class);

        if (user == null) {
            throw new AccountNotFoundException(username + " not found with query");
        }
        if (!BCrypt.checkpw(plainTextPassword, user.password)) {
            throw new FailedLoginException(username + " invalid password");
        }

        TimeBasedOneTimePassword timeBasedOneTimePassword = this.mongoTemplate.findOne(new Query(Criteria
                .where("owner").is(user.id)
                .and("is_confirmed").is(true)
                .and("deleted").is(false)
        ), TimeBasedOneTimePassword.class);

        if (timeBasedOneTimePassword != null && timeBasedOneTimePassword.totpSecret != null) {
            if (oneTimePassword == null) {
                throw new OneTimePasswordRequiredException("Time-based One Time Password required");
            }
            try {
                if (!TotpUtils.checkCode(timeBasedOneTimePassword.getTotpSecretBase32(), Long.valueOf(oneTimePassword), 30, 1)) {
                    throw new OneTimePasswordFailedLoginException(username + " invalid time-based one time password");
                }
            } catch (Exception ex) {
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

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    /**
     * Helper method to construct a handler result
     * on successful authentication events.
     *
     * @param credential the credential on which the authentication was successfully performed.
     * Note that this credential instance may be different from what was originally provided
     * as transformation of the username may have occurred, if one is in fact defined.
     * @param principal the resolved principal
     * @param warnings the warnings
     * @return the constructed handler result
     */
    protected final HandlerResult createHandlerResult(final Credential credential, final Principal principal,
            final List<Message> warnings) {
        return new HandlerResult(this, new BasicCredentialMetaData(credential), principal, warnings);
    }

    public final void setPrincipalNameTransformer(final PrincipalNameTransformer principalNameTransformer) {
        this.principalNameTransformer = principalNameTransformer;
    }

    public final void setMongoTemplate(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * {@inheritDoc}
     * @return True if credential is a {@link UsernamePasswordCredential}, false otherwise.
     */
    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OpenScienceFrameworkCredential;
    }
}
