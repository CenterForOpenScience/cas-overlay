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

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of <code>ServiceRegistryDao</code> that reads multiple service registries
 * and merges them at the Spring Application Context initialization time.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public class MergingServiceRegistryDao implements ServiceRegistryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergingServiceRegistryDao.class);

    /** An array of ServiceRegistryDao. */
    @NotNull
    private List<ServiceRegistryDao> serviceRegistries =
            new ArrayList<>();

    /**
     * Map of service ID to registered service.
     */
    private Map<Long, RegisteredService> serviceMap = new ConcurrentHashMap<>();

    /**
     * Instantiates a new Merging Service Registry dao.
     */
    public MergingServiceRegistryDao() {
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
        final Map<Long, RegisteredService> temp = new ConcurrentHashMap<>();
        for (final ServiceRegistryDao serviceRegistry : serviceRegistries) {
            List<RegisteredService> registeredServices = serviceRegistry.load();
            for (final RegisteredService registeredService : registeredServices) {
                if (!temp.containsKey(registeredService.getId())) {
                    temp.put(registeredService.getId(), registeredService);
                } else {
                    LOGGER.warn("Registered service [{}] already exists while loading [{}]", registeredService.getId(), serviceRegistry.getClass().getSimpleName());
                }
            }
        }
        this.serviceMap = temp;
        return new ArrayList<>(this.serviceMap.values());
    }

    @Override
    public final RegisteredService findServiceById(final long id) {
        return serviceMap.get(id);
    }

    /**
     * Sets the service registries.
     *
     * @param serviceRegistries Non-null list of service registries.
     */
    public final void setServiceRegistries(final List<ServiceRegistryDao> serviceRegistries) {
        this.serviceRegistries = serviceRegistries;
    }
}
