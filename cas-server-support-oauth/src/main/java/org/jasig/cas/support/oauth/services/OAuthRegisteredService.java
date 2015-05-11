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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.services.RegexRegisteredService;

import java.util.Collections;
import java.util.List;

/**
 * An extension of the {@link RegexRegisteredService} that defines the
 * OAuth client id and secret for a given registered service.
 * @author Misagh Moayyed, Michael Haselton
 * @since 4.0.0
 */
public final class OAuthRegisteredService extends RegexRegisteredService {

    private static final long serialVersionUID = 4446061823364109018L;

    private String clientSecret;

    private String clientId;

    private Boolean bypassApprovalPrompt = Boolean.FALSE;

    private List<String> grantTypes = Collections.emptyList();

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

    /**
     * Sets the grant types.
     *
     * @param grantTypes the grant types
     */
    public void setGrantTypes(final List<String> grantTypes) {
        this.grantTypes = grantTypes;
    }

    /**
     * Gets the grant types.
     *
     * @return the grant types
     */
    public List<String> getGrantTypes() {
        return Collections.unmodifiableList(this.grantTypes);
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.appendSuper(super.toString());
        builder.append("clientId", getClientId());
        builder.append("approvalPrompt", isBypassApprovalPrompt());
        builder.append("grantTypes", getGrantTypes());
        return builder.toString();
    }
}
