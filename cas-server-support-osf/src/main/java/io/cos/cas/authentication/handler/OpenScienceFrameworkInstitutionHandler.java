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

package io.cos.cas.authentication.handler;

import io.cos.cas.services.OpenScienceFrameworkInstitutionRegisteredService;
import io.cos.cas.types.DelegationProtocol;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The Open Science Framework Institution Handler.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class OpenScienceFrameworkInstitutionHandler {

    @NotNull
    private ServicesManager servicesManager;

    /**
     * Construct an instance of OSF Institution Handler with the Service Manager.
     *
     * @param servicesManager the Service Manager that manages all services including Institutions
     */
    public OpenScienceFrameworkInstitutionHandler(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /**
     * Find the Logout URL for a given Institution by Institution ID.
     * Note: Institution ID is not Service ID
     *
     * @param institutionId the Institution ID
     * @return the Institution Logout URL or null
     */
    public String findInstitutionLogoutUrlById(final String institutionId) {

        final long id = (new BigInteger(institutionId.getBytes())).longValue();
        final Collection<RegisteredService> registeredServices = servicesManager.getAllServices();

        for (final RegisteredService service: registeredServices) {
            if (service instanceof OpenScienceFrameworkInstitutionRegisteredService) {
                final OpenScienceFrameworkInstitutionRegisteredService institution
                        = (OpenScienceFrameworkInstitutionRegisteredService) service;
                if (institutionId.equals(institution.getInstitutionId())) {
                    return institution.getInstitutionLogoutUrl();
                }
            }
        }
        return null;
    }

    /**
     * Return a map of institution login url and institution name.
     *  1.  The "name" is the value instead of the key.
     *  2.  For institutions authenticated through "cas-pac4j", the institution id replaces the login url,
     *      whose actual login url is generated during flow "client action".
     *
     * @param target The osf service target after successful institution login (only for "saml-shib" institutions)
     * @return Map.
     *      For "saml-shib", full login url as key and full institution display name as value;
     *      For "cas-pac4j", institution id as key and full institution display name as value;
     */
    public Map<String, String> getInstitutionLoginUrls(final String target) {

        final Map<String, String> institutionLogin = new HashMap<>();
        final Collection<RegisteredService> registeredServices = servicesManager.getAllServices();

        for (final RegisteredService service: registeredServices) {
            if (service instanceof OpenScienceFrameworkInstitutionRegisteredService) {
                final OpenScienceFrameworkInstitutionRegisteredService institution
                        = (OpenScienceFrameworkInstitutionRegisteredService) service;
                final DelegationProtocol delegationProtocol = institution.getDelegationProtocol();
                if (DelegationProtocol.SAML_SHIB.equals(delegationProtocol)) {
                    institutionLogin.put(institution.getInstitutionLoginUrl() + "&target=" + target, institution.getName());
                } else if (DelegationProtocol.CAS_PAC4J.equals(delegationProtocol)) {
                    institutionLogin.put(institution.getInstitutionId(), institution.getName());
                }
            }
        }

        return institutionLogin;
    }
}
