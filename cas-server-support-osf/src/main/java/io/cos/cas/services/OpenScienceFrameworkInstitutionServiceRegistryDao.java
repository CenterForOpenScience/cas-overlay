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

import io.cos.cas.api.handler.ApiEndpointHandler;
import io.cos.cas.api.type.ApiEndpoint;
import org.apache.http.HttpStatus;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistryDao;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Open Science Framework Institution Service Registry DAO.
 *
 * Implementation of <code>ServiceRegistryDao</code> that reads OSF Institutions definition via the
 * OSF API CAS Service Endpoint during the Spring Application Context initialization time.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
public class OpenScienceFrameworkInstitutionServiceRegistryDao implements ServiceRegistryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkInstitutionServiceRegistryDao.class);

    private static final String SERVICE_TYPE = "LOAD_INSTITUTIONS";

    private Map<Long, RegisteredService> serviceMap = new ConcurrentHashMap<>();

    @NotNull
    private ApiEndpointHandler apiEndpointHandler;

    /**
     * Instantiates a new Open Science Framework Institution Service Registry Dao.
     *
     * @param apiEndpointHandler the API Endpoint Handler
     */
    public OpenScienceFrameworkInstitutionServiceRegistryDao(final ApiEndpointHandler apiEndpointHandler) {
        this.apiEndpointHandler = apiEndpointHandler;
    }

    @Override
    public final RegisteredService save(final RegisteredService service) {
        return null;
    }

    @Override
    public final synchronized boolean delete(final RegisteredService service) {
        return false;
    }


    @SuppressWarnings("unchecked")
    @Override
    public final synchronized List<RegisteredService> load() {

        final Map<Long, RegisteredService> serviceMap = new ConcurrentHashMap<>();

        final JSONObject data = new JSONObject().put("serviceType", SERVICE_TYPE);
        final JSONObject response = apiEndpointHandler.handle(
                ApiEndpoint.SERVICE_LOAD_INSTITUTIONS,
                apiEndpointHandler.encryptPayload("data", data.toString())
        );

        if (response != null && response.getInt("status") == HttpStatus.SC_OK) {
            final JSONObject responseBody = response.getJSONObject("body");
            if (responseBody != null) {
                final Iterator<String> iterator = responseBody.keys();
                while (iterator.hasNext()) {
                    final OpenScienceFrameworkInstitutionRegisteredService service = new OpenScienceFrameworkInstitutionRegisteredService();
                    final String institutionId = iterator.next();
                    final JSONObject institutionData = verifyService(responseBody, institutionId);
                    if (institutionData == null) {
                        continue;
                    }
                    service.setId(new BigInteger(institutionId.getBytes()).longValue());
                    service.setInstitutionId(institutionId);
                    service.setName((institutionData.getString("institutionName")));
                    service.setInstitutionLoginUrl((institutionData.getString("institutionLoginUrl")));
                    service.setInstitutionLogoutUrl((institutionData.getString("institutionLogoutUrl")));
                    service.setDelegationProtocol(institutionData.getString("delegationProtocol"));
                    serviceMap.put(service.getId(), service);
                }
            }
        }

        this.serviceMap = serviceMap;
        return new ArrayList<>(this.serviceMap.values());
    }

    @Override
    public final RegisteredService findServiceById(final long id) {
        return serviceMap.get(id);
    }

    /**
     * Verify the service information in response.
     *
     * @param response the response as a JSON Object
     * @param institutionId the institution's GUID as a String
     * @return the institution service as a JSON Object if verified; null otherwise
     */
    private JSONObject verifyService(final JSONObject response, final String institutionId) {

        JSONObject institutionData;

        try {
            institutionData = response.getJSONObject(institutionId);
        } catch (final JSONException e) {
            LOGGER.error("JSON Exception: Cannot Parse JSON Object for Institution: {}", institutionId);
            return null;
        }

        if (institutionData.has("institutionName")
                && institutionData.has("institutionLoginUrl")
                && institutionData.has("institutionLogoutUrl")
                && institutionData.has("delegationProtocol")) {
            return institutionData;
        } else {
            LOGGER.error("Definition Exception: Missing Definition for Institution: {}", institutionId);
            return null;
        }
    }
}
