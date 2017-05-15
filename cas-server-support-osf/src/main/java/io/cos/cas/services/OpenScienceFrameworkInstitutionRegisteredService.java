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

package io.cos.cas.services;

import io.cos.cas.types.DelegationProtocol;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;

/**
 * Open Science Framework Institution Registered Service.
 *
 * Institution is considered as a special type of Registered Service, which contains/caches Institution
 * information that is used by CAS for Institution Login and Logout purposes. Its `serviceId` does not
 * match any String and has the the lowest evaluation priority.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class OpenScienceFrameworkInstitutionRegisteredService extends RegexRegisteredService {

    private static final long serialVersionUID = 2941289781073114252L;

    private static final String SERVICE_ID = "(?!.*)";

    private static final int EVALUATION_ORDER = 10000;

    private String institutionId;

    private String institutionLoginUrl;

    private String institutionLogoutUrl;

    private String delegationProtocol;

    /**
     * Default Constructor.
     *
     * Construct an instance of Open Science Framework Institution as a Registered Service.
     * Set `serviceId` to "(?!.*)" which matches nothing.
     * Set `evaluationOrder` to 10000 which is the lowest priority.
     */
    public OpenScienceFrameworkInstitutionRegisteredService() {
        this.setServiceId(SERVICE_ID);
        this.setEvaluationOrder(EVALUATION_ORDER);
    }

    public String getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(final String institutionId) {
        this.institutionId = institutionId;
    }

    public String getInstitutionLoginUrl() {
        return institutionLoginUrl;
    }

    public void setInstitutionLoginUrl(final String institutionLoginUrl) {
        this.institutionLoginUrl = institutionLoginUrl;
    }

    public String getInstitutionLogoutUrl() {
        return institutionLogoutUrl;
    }

    public void setInstitutionLogoutUrl(final String institutionLogoutUrl) {
        this.institutionLogoutUrl = institutionLogoutUrl;
    }

    /**
     * @return the delegation protocol of an institution.
     */
    public DelegationProtocol getDelegationProtocol() {
        try {
            return DelegationProtocol.getType(delegationProtocol);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    public void setDelegationProtocol(final String delegationProtocol) {
        this.delegationProtocol = delegationProtocol;
    }

    @Override
    public int compareTo(final RegisteredService other) {
        if (other instanceof OpenScienceFrameworkInstitutionRegisteredService) {
            final OpenScienceFrameworkInstitutionRegisteredService institutionRegisteredService
                    = (OpenScienceFrameworkInstitutionRegisteredService) other;
            return new CompareToBuilder()
                    .appendSuper(super.compareTo(other))
                    .append(this.getInstitutionId(), institutionRegisteredService.getInstitutionId())
                    .toComparison();
        }
        return super.compareTo(other);
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.appendSuper(super.toString());
        builder.append("institutionId", getInstitutionId());
        builder.append("institutionName", getName());
        builder.append("delegationProtocol", getDelegationProtocol());
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
        final OpenScienceFrameworkInstitutionRegisteredService other = (OpenScienceFrameworkInstitutionRegisteredService) obj;
        return this.institutionId.equals(other.institutionId);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(institutionId)
                .toHashCode();
    }
}
