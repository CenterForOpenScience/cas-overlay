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

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Open Science Framework Institution Handler.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
public class OpenScienceFrameworkInstitutionHandler {

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
     * Find the logout url for given institution identified by institution _id.
     *
     * @param id the institution _id
     * @return String or null
     */
    public String findInstitutionLogoutUrlById(final String id) {
        final OpenScienceFrameworkInstitution institution = openScienceFrameworkDao.findOneInstitutionById(id);
        return institution != null ? institution.getLogoutUrl() : null;
    }

    /**
     * Return a map of institution name and login url.
     *
     * @param target The osf service target after successful institution login
     * @return Map&lt;String, String&gt;
     */
    public Map<String, String> getInstitutionLoginUrls(final String target) {
        final List<OpenScienceFrameworkInstitution> institutionList = openScienceFrameworkDao.findAllInstitutions();
        final Map<String, String> institutionLogin = new HashMap<>();
        for (final OpenScienceFrameworkInstitution institution: institutionList) {
            institutionLogin.put(institution.getLoginUrl() + "&target=" + target, institution.getName());
        }
        return institutionLogin;
    }
}
