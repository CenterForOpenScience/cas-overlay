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

package io.cos.cas.adaptors.postgres.handlers;

import io.cos.cas.adaptors.postgres.daos.OpenScienceFrameworkDaoImpl;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkInstitution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

/**
 * The Open Science Framework Institution Handler.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
public class OpenScienceFrameworkInstitutionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkInstitution.class);

    @NotNull
    private OpenScienceFrameworkDaoImpl openScienceFrameworkDao;

    /** Default Constructor. */
    public OpenScienceFrameworkInstitutionHandler() {}

    /**
     * @param openScienceFrameworkDao the open science framework data access object.
     */
    public void setOpenScienceFrameworkDao(final OpenScienceFrameworkDaoImpl openScienceFrameworkDao) {
        this.openScienceFrameworkDao = openScienceFrameworkDao;
    }

    /**
     * Find the logout url for given institution identified by provider id.
     *
     * @param providerId the provider id
     * @return String or null
     */
    public String findInstitutionLogoutUrlByProviderId(final String providerId) {
        final OpenScienceFrameworkInstitution institution = openScienceFrameworkDao.findOneInstitutionByProviderId(providerId);
        return institution != null ? institution.getLogoutUrl() : null;
    }
}
