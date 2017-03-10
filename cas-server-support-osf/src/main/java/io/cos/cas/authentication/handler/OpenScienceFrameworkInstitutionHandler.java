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
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The Open Science Framework Institution Handler.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
public class OpenScienceFrameworkInstitutionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkInstitutionHandler.class);

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
        final OpenScienceFrameworkInstitutionRegisteredService institution
            = (OpenScienceFrameworkInstitutionRegisteredService) servicesManager
                .findServiceBy(new BigInteger(institutionId.getBytes()).longValue());
        return institution != null ? institution.getInstitutionLogoutUrl() : null;
    }

    /**
     * Return a map of Institution Login Url and Institution Name.
     * 1. For protocol "saml-shib", the Login URL is the Key, and the Institution Name is the value.
     * 2. For protocol "cas-pac4j", the Institution ID is the Key, and the Institution Name is the value.
     *    The "actual" Login URL is generated during the "ClientAction" flow.
     *
     * @param target the OSF Service Target after successful Institution Login,
     *               which is only applicable for protocol "saml-shib"
     * @return  a map of Institution Login Url or ID, and Institution Name
     */
    public Map<String, String> getInstitutionLoginUrls(final String target) {

        final Map<String, String> institutionLogin = new HashMap<>();
        final Collection<RegisteredService> registeredServices = servicesManager.getAllServices();

        for (final RegisteredService service: registeredServices) {
            if (service instanceof OpenScienceFrameworkInstitutionRegisteredService) {
                final OpenScienceFrameworkInstitutionRegisteredService institution
                        = (OpenScienceFrameworkInstitutionRegisteredService) service;

                if (institution.verifyDelegationProtocol()) {
                    if ("saml-shib".equals(institution.getDelegationProtocol())) {
                        institutionLogin.put(institution.getInstitutionLoginUrl() + "&target=" + target, institution.getName());
                    } else if ("cas-pac4j".equals(institution.getDelegationProtocol())) {
                        institutionLogin.put(institution.getInstitutionId(), institution.getName());
                    }
                }
            }
        }

        return institutionLogin;
    }
}
