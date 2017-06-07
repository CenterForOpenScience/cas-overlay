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

import io.cos.cas.types.DelegationProtocol;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jasig.cas.authentication.RememberMeUsernamePasswordCredential;

import java.util.HashMap;
import java.util.Map;

/**
 * Open Science Framework Credential.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public class OpenScienceFrameworkCredential extends RememberMeUsernamePasswordCredential {

    /** Authentication attribute name for Institution ID. */
    public static final String INSTITUTION_ID = "institutionId";

    /** Authentication attribute name for Remote Principal. */
    public static final String REMOTE_PRINCIPAL = "remotePrincipal";

    /** Unique ID for serialization. */
    private static final long serialVersionUID = -3006234230814410939L;

    /** Remote Principal appended to username in string representation. */
    private static final String REMOTE_PRINCIPAL_SUFFIX = "+rp";

    /** Verification Key appended to username in string representation. */
    private static final String VERIFICATION_KEY_SUFFIX = "+vk";

    /** Time-based One Time Password suffix appended to username in string representation. */
    private static final String ONE_TIME_PASSWORD_SUFFIX = "+otp";

    private String verificationKey;

    private String oneTimePassword;

    private Boolean remotePrincipal = Boolean.FALSE;

    private String institutionId;

    private String nonInstitutionExternalIdProvider;

    private String nonInstitutionExternalId;

    private DelegationProtocol delegationProtocol;

    private Map<String, String> delegationAttributes = new HashMap<>();

    /** Default Constructor. */
    public OpenScienceFrameworkCredential() {}

    public String getVerificationKey() {
        return this.verificationKey;
    }

    public void setVerificationKey(final String verificationKey) {
        this.verificationKey = verificationKey;
    }

    public String getOneTimePassword() {
        return this.oneTimePassword;
    }

    public void setOneTimePassword(final String oneTimePassword) {
        this.oneTimePassword = oneTimePassword;
    }

    public final Boolean isRemotePrincipal() {
        return this.remotePrincipal;
    }

    public final void setRemotePrincipal(final Boolean remotePrincipal) {
        this.remotePrincipal = remotePrincipal;
    }

    public final String getInstitutionId() {
        return this.institutionId;
    }

    public final void setInstitutionId(final String institutionId) {
        this.institutionId = institutionId;
    }

    public String getNonInstitutionExternalIdProvider() {
        return nonInstitutionExternalIdProvider;
    }

    public void setNonInstitutionExternalIdProvider(final String nonInstitutionIdExternalProvider) {
        this.nonInstitutionExternalIdProvider = nonInstitutionIdExternalProvider;
    }

    public String getNonInstitutionExternalId() {
        return nonInstitutionExternalId;
    }

    public void setNonInstitutionExternalId(final String nonInstitutionExternalId) {
        this.nonInstitutionExternalId = nonInstitutionExternalId;
    }

    public final DelegationProtocol getDelegationProtocol() {
        return delegationProtocol;
    }

    public void setDelegationProtocol(final DelegationProtocol delegationProtocol) {
        this.delegationProtocol = delegationProtocol;
    }

    public final Map<String, String> getDelegationAttributes() {
        return delegationAttributes;
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

        if (this.remotePrincipal) {
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
        if (!this.delegationProtocol.equals(other.delegationProtocol)) {
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
                .append(delegationProtocol)
                .toHashCode();
    }
}
