/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package io.cos.cas.authentication;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jasig.cas.authentication.RememberMeUsernamePasswordCredential;

import java.util.HashMap;
import java.util.Map;

/**
 * Credential for authenticating with a username and password.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public class OpenScienceFrameworkCredential extends RememberMeUsernamePasswordCredential {

    /** Unique ID for serialization. */
    private static final long serialVersionUID = -3006234230814410939L;

    /** Authentication attribute name for Institution ID. */
    public static final String INSTITUTION_ID = "institutionId";

    /** Authentication attribute name for Remote Principal. */
    public static final String REMOTE_PRINCIPAL = "remotePrincipal";

    /** Remote Principal appended to username in string representation. */
    private static final String REMOTE_PRINCIPAL_SUFFIX = "+rp";

    /** Verification Key appended to username in string representation. */
    private static final String VERIFICATION_KEY_SUFFIX = "+vk";

    /** Time-based One Time Password suffix appended to username in string representation. */
    private static final String ONE_TIME_PASSWORD_SUFFIX = "+otp";

    /** The Verification Key. */
    private String verificationKey;

    /** The One Time Password. */
    private String oneTimePassword;

    /** Indicates a Remote Principal. */
    private Boolean remotePrincipal = Boolean.FALSE;

    /** The Institution Id. */
    private String institutionId = "osf";

    /** The Authentication Headers. */
    private Map<String, String> authenticationHeaders = new HashMap<>();

    /** Default constructor. */
    public OpenScienceFrameworkCredential() {}

    /**
     * Creates a new instance with the given username and password.
     *
     * @param username Non-null user name.
     * @param password Non-null password.
     * @param rememberMe remember me.
     * @param verificationKey verification key.
     */
    public OpenScienceFrameworkCredential(final String username, final String password, final Boolean rememberMe,
                                          final String verificationKey) {
        this(username, password, rememberMe, verificationKey, null);
    }

    /**
     * Creates a new instance with the given username and password.
     *
     * @param username Non-null user name.
     * @param password Non-null password.
     * @param rememberMe remember me.
     * @param verificationKey verification key.
     * @param oneTimePassword one time password.
     */
    public OpenScienceFrameworkCredential(final String username, final String password, final Boolean rememberMe,
                                          final String verificationKey, final String oneTimePassword) {
        this.setUsername(username);
        this.setPassword(password);
        this.setRememberMe(rememberMe);
        this.setVerificationKey(verificationKey);
        this.setOneTimePassword(oneTimePassword);
    }

    /**
     * @return Returns the Verification Key.
     */
    public String getVerificationKey() {
        return this.verificationKey;
    }

    /**
     * @param verificationKey The Verification Key to set.
     */
    public void setVerificationKey(final String verificationKey) {
        this.verificationKey = verificationKey;
    }

    /**
     * @return Returns the One Time Password.
     */
    public String getOneTimePassword() {
        return this.oneTimePassword;
    }

    /**
     * @param oneTimePassword The One Time Password to set.
     */
    public void setOneTimePassword(final String oneTimePassword) {
        this.oneTimePassword = oneTimePassword;
    }

    /**
     * @return Returns the Remote Principal.
     */
    public final Boolean isRemotePrincipal() {
        return this.remotePrincipal;
    }

    /**
     * @param remotePrincipal The Remote Principal.
     */
    public final void setRemotePrincipal(final Boolean remotePrincipal) {
        this.remotePrincipal = remotePrincipal;
    }

    /**
     * @return Returns Institution Id
     */
    public String getInstitutionId() { return this.institutionId; }

    /**
     * @param institutionId The Institution Id
     */
    public final void setInstitutionId(final String institutionId) {
        this.institutionId = institutionId;
    }

    /**
     * @return Returns the Authentication Headers.
     */
    public final Map<String, String> getAuthenticationHeaders() {
        return authenticationHeaders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return this.getUsername();
    }

    @Override
    public String toString() {
        String representation = super.toString();

        if (this.isRemotePrincipal()) {
            representation += REMOTE_PRINCIPAL_SUFFIX;
        }
        if (this.verificationKey != null) {
            representation += VERIFICATION_KEY_SUFFIX;
        }
        if (this.oneTimePassword != null) {
            representation += ONE_TIME_PASSWORD_SUFFIX;
        }
        return representation;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OpenScienceFrameworkCredential other = (OpenScienceFrameworkCredential) obj;
        if (!this.verificationKey.equals(other.verificationKey)) {
            return false;
        }
        if (!this.oneTimePassword.equals(other.oneTimePassword)) {
            return false;
        }
        if (!this.remotePrincipal.equals(other.remotePrincipal)) {
            return false;
        }
        if (!this.institutionId.equals(other.institutionId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(verificationKey)
                .append(oneTimePassword)
                .append(remotePrincipal)
                .append(institutionId)
                .toHashCode();
    }
}
