/*
 * Copyright (c) 2016. Center for Open Science
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
package io.cos.cas.adaptors.postgres.handlers;

import io.cos.cas.adaptors.postgres.daos.OpenScienceFrameworkDaoImpl;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkInstitution;
import io.cos.cas.adaptors.postgres.types.DelegationProtocol;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The Open Science Framework Institution Handler.
 *
 * @author Longze Chen
 * @since 4.1.5
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
     * Check if an institution of a given ID exists and supports SSO.
     *
     * @param id the OSF institution ID that identifies the institution
     * @return <code>true</code> if exists and <code>false </code> otherwise
     */
    public boolean validateInstitutionForLogin(final String id) {

        final OpenScienceFrameworkInstitution institution
                = openScienceFrameworkDao.findOneInstitutionById(id);
        return institution != null && institution.getDelegationProtocol() != null;
    }

    /**
     * Find the logout url for given institution identified by institution _id.
     *
     * @param id the OSF institution ID that identifies the institution
     * @return String or null
     */
    public String findInstitutionLogoutUrlById(final String id) {
        final OpenScienceFrameworkInstitution institution = openScienceFrameworkDao.findOneInstitutionById(id);
        return institution != null ? institution.getLogoutUrl() : null;
    }

    /**
     * <p>Return a map of institution login url or institution ID as key and institution name as value.</p>
     *
     * <p>For saml-shib institutions, the login url is the key and the institution display name is the value.</p>
     *
     * <p>For cas-pac4j institutions, the institution ID is the key and the institution display name is the value. </p>
     *
     * @param target the osf service target after successful institution login, only used for "saml-shib" institutions
     * @param id the OSF institution ID if auto-selection is enabled
     * @return a single-entry {@link Map} if <code>id</code> presents and if the institution this <code>id</code>
     *         identifies exists and supports institution SSO; otherwise return a multi-entry {@link Map} of all.
     */
    public Map<String, String> getInstitutionLoginUrlMap(final String target, final String id) {

        List<OpenScienceFrameworkInstitution> institutionList = new LinkedList<>();
        if (id == null || id.isEmpty()) {
            institutionList = openScienceFrameworkDao.findAllInstitutions();
        } else {
            final OpenScienceFrameworkInstitution institution
                    = openScienceFrameworkDao.findOneInstitutionById(id);
            if (institution != null) {
                institutionList.add(institution);
            } else {
                institutionList = openScienceFrameworkDao.findAllInstitutions();
            }
        }

        final Map<String, String> institutionLoginUrlMap = new HashMap<>();
        for (final OpenScienceFrameworkInstitution institution: institutionList) {
            final DelegationProtocol delegationProtocol = institution.getDelegationProtocol();
            if (DelegationProtocol.SAML_SHIB.equals(delegationProtocol)) {
                institutionLoginUrlMap.put(institution.getLoginUrl() + "&target=" + target, institution.getName());
            } else if (DelegationProtocol.CAS_PAC4J.equals(delegationProtocol)) {
                institutionLoginUrlMap.put(institution.getId(), institution.getName());
            }
        }
        return institutionLoginUrlMap;
    }
}
