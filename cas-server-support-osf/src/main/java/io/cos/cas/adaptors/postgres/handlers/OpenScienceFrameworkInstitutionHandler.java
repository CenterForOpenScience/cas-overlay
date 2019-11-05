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
        final List<OpenScienceFrameworkInstitution> institutionList = openScienceFrameworkDao.findAllInstitutions();
        final Map<String, String> institutionLogin = new HashMap<>();
        for (final OpenScienceFrameworkInstitution institution: institutionList) {
            final DelegationProtocol delegationProtocol = institution.getDelegationProtocol();
            if (DelegationProtocol.SAML_SHIB.equals(delegationProtocol)) {
                institutionLogin.put(institution.getLoginUrl() + "&target=" + target, institution.getName());
            } else if (DelegationProtocol.CAS_PAC4J.equals(delegationProtocol)) {
                institutionLogin.put(institution.getId(), institution.getName());
            }
        }
        return institutionLogin;
    }
}
