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
package org.jasig.cas.support.oauth.services;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import org.jasig.cas.services.OSFRegisteredService;
import org.jasig.cas.services.RegisteredService;

/**
 * OAuth registered service.
 *
 * As an extension of the {@link OSFRegisteredService}, this class defines a few extra OAuth properties for a given
 * registered service, including the client id, client secret and a boolean flag which determines whether to bypass
 * the approval prompt during authorization.
 *
 * @author Misagh Moayyed
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuthRegisteredService extends OSFRegisteredService {

    /** Unique id for serialization. */
    private static final long serialVersionUID = 5318897374067731021L;

    private String clientSecret;

    private String clientId;

    private Boolean bypassApprovalPrompt = Boolean.FALSE;

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return this.clientSecret;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public Boolean isBypassApprovalPrompt() {
        return bypassApprovalPrompt;
    }

    public void setBypassApprovalPrompt(final Boolean bypassApprovalPrompt) {
        this.bypassApprovalPrompt = bypassApprovalPrompt;
    }

    @Override
    public int compareTo(final RegisteredService other) {
        if (other instanceof OAuthRegisteredService) {
            final OAuthRegisteredService oAuthRegisteredService = (OAuthRegisteredService) other;
            return new CompareToBuilder()
                    .appendSuper(super.compareTo(other))
                    .append(this.getClientId(), oAuthRegisteredService.getClientId())
                    .toComparison();
        }
        return super.compareTo(other);
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.appendSuper(super.toString());
        builder.append("clientId", getClientId());
        builder.append("approvalPrompt", isBypassApprovalPrompt());
        return builder.toString();
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
        final OAuthRegisteredService other = (OAuthRegisteredService) obj;
        return this.clientId.equals(other.clientId);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(clientId).toHashCode();
    }
}
