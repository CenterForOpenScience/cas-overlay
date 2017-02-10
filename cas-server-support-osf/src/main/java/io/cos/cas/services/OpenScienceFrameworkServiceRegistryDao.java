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
 * Implementation of <code>ServiceRegistryDao</code> that reads services definition from the
 * Open Science Framework at the Spring Application Context initialization time.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.0
 */
public class OpenScienceFrameworkServiceRegistryDao implements ServiceRegistryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkServiceRegistryDao.class);

    private static final int HEX_RADIX = 16;

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
        final List<OpenScienceFrameworkApiOauth2Application> oAuthServices = openScienceFrameworkDao.findOauthApplications();

        final ReturnAllowedAttributeReleasePolicy attributeReleasePolicy = new ReturnAllowedAttributeReleasePolicy();
        final Map<Long, RegisteredService> serviceMap = new ConcurrentHashMap<>();
        final ArrayList<String> allowedAttributes = new ArrayList<>();
        /**
         * e.g. global attribute release
         * allowedAttributes.add("username");
         * allowedAttributes.add("givenName");
         * allowedAttributes.add("familyName");
         */
        attributeReleasePolicy.setAllowedAttributes(allowedAttributes);
        for (final OpenScienceFrameworkApiOauth2Application oAuthService : oAuthServices) {
            final OAuthRegisteredService service = new OAuthRegisteredService();
            service.setId(new BigInteger(oAuthService.getId(), HEX_RADIX).longValue());
            service.setName(oAuthService.getName());
            service.setDescription(oAuthService.getDescription());
            service.setServiceId(oAuthService.getCallbackUrl());
            service.setBypassApprovalPrompt(Boolean.FALSE);
            service.setClientId(oAuthService.getClientId());
            service.setClientSecret(oAuthService.getClientSecret());
            service.setAttributeReleasePolicy(attributeReleasePolicy);
            serviceMap.put(service.getId(), service);
        }
        this.serviceMap = serviceMap;
        return new ArrayList<>(this.serviceMap.values());
    }

    @Override
    public final RegisteredService findServiceById(final long id) {
        return serviceMap.get(id);
    }
}
