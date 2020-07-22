/*
 * Copyright (c) 2015. Center for Open Science
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
package io.cos.cas.services;

import io.cos.cas.adaptors.postgres.daos.OpenScienceFrameworkDaoImpl;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkApiOauth2Application;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.jasig.cas.services.ServiceRegistryDao;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link ServiceRegistryDao} that reads services definition from the OSF at the initialization time
 * of Spring Application Context.
 *
 * Note: Similar to the OSF and its services (e.g. preprints, registries, etc.), OSF developer apps loaded from the OSF
 *       database are treated as {@link RegisteredService}. CAS determines which service it is by "regex" checking the
 *       "service" query parameter in the login URL against the {@link RegisteredService#getServiceId()}. If a service
 *       matches two or more {@code serviceId}, the one with the highest {@link RegisteredService#getEvaluationOrder()}
 *       will be matched. The order is a non-negative integer, {@code 0} is the highest and the order decreases as the
 *       value grows larger.
 *
 *       {@link OpenScienceFrameworkApiOauth2Application#getCallbackUrl()} is entered by the owner of the developer app
 *       via OSF and it is used here in CAS to set the {@code serviceId}. Thus we must set the evaluation order to the
 *       lowest so that it does not take over other services. Here are a list of evaluation orders:
 *
 *       1. 1000: branded preprints, CAS oauth callback authorization service
 *       2. 1500: OSF preprints, OSF registries
 *       3. 2000: OSF, CAS
 *       4. 3000: OSF developer apps
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 19.3.0
 */
public class OpenScienceFrameworkServiceRegistryDao implements ServiceRegistryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkServiceRegistryDao.class);

    private static final int HEX_RADIX = 16;

    private static final int EVALUATION_ORDER = 3000;

    /** Map of service ID to registered service. */
    private Map<Long, RegisteredService> serviceMap = new ConcurrentHashMap<>();

    /** The Open Science Framework Data Access Model. */
    private OpenScienceFrameworkDaoImpl openScienceFrameworkDao;

    /**
     * Instantiates a new Open Science Framework service registry dao.
     *
     * @param openScienceFrameworkDao the open science framework data access object
     */
    public OpenScienceFrameworkServiceRegistryDao(final OpenScienceFrameworkDaoImpl openScienceFrameworkDao) {
        this.openScienceFrameworkDao = openScienceFrameworkDao;
    }

    @Override
    public final RegisteredService save(final RegisteredService service) {
        return null;
    }

    @Override
    public final synchronized boolean delete(final RegisteredService service) {
        return false;
    }

    @Override
    public final synchronized List<RegisteredService> load() {

        // Load all OSF developer apps from the OSF database via OSF DAO
        final List<OpenScienceFrameworkApiOauth2Application> oAuthServices
                = openScienceFrameworkDao.findOauthApplications();

        // Init attribute release policy
        final ReturnAllowedAttributeReleasePolicy attributeReleasePolicy = new ReturnAllowedAttributeReleasePolicy();
        final ArrayList<String> allowedAttributes = new ArrayList<>();
        attributeReleasePolicy.setAllowedAttributes(allowedAttributes);

        // Create the registered service for each developer app and put them into  a service map
        final Map<Long, RegisteredService> serviceMap = new ConcurrentHashMap<>();
        for (final OpenScienceFrameworkApiOauth2Application oAuthService : oAuthServices) {
            final OAuthRegisteredService service = new OAuthRegisteredService();
            service.setId(new BigInteger(oAuthService.getId(), HEX_RADIX).longValue());
            service.setName(oAuthService.getName());
            service.setDescription(oAuthService.getDescription());
            service.setServiceId(oAuthService.getCallbackUrl());
            service.setEvaluationOrder(EVALUATION_ORDER);
            service.setBypassApprovalPrompt(Boolean.FALSE);
            service.setClientId(oAuthService.getClientId());
            service.setClientSecret(oAuthService.getClientSecret());
            service.setAttributeReleasePolicy(attributeReleasePolicy);
            serviceMap.put(service.getId(), service);
        }

        // Set the service map and return a list of services
        this.serviceMap = serviceMap;
        return new ArrayList<>(this.serviceMap.values());
    }

    @Override
    public final RegisteredService findServiceById(final long id) {
        return serviceMap.get(id);
    }
}
