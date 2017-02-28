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

import io.cos.cas.adaptors.api.OpenScienceFrameworkApiCasEndpoint;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.jasig.cas.services.ServiceRegistryDao;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
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

    /** The Open Science Framework API CAS Endpoint instance. */
    @NotNull
    private OpenScienceFrameworkApiCasEndpoint osfApiCasEndpoint;

    /**
     * Instantiates a new Open Science Framework service registry dao.
     *
     * @param osfApiCasEndpoint the Open Science Framework API CAS Endpoint
     */
    public OpenScienceFrameworkServiceRegistryDao(final OpenScienceFrameworkApiCasEndpoint osfApiCasEndpoint) {
        this.osfApiCasEndpoint = osfApiCasEndpoint;
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

        final ReturnAllowedAttributeReleasePolicy attributeReleasePolicy = new ReturnAllowedAttributeReleasePolicy();
        final ArrayList<String> allowedAttributes = new ArrayList<>();
        attributeReleasePolicy.setAllowedAttributes(allowedAttributes);

        final JSONObject data = new JSONObject();
        data.put("serviceType", "oAuthApplications");
        // encrypt the payload using JWE/JWT
        final String encryptedPayload = osfApiCasEndpoint.encryptPayload("data", data.toString());

        // talk to API `/cas/service/pat/` endpoint
        final JSONObject response = osfApiCasEndpoint.apiCasService("developerApps", encryptedPayload);
        final Iterator<String> iterator = response.keys();
        while (iterator.hasNext()) {
            final OAuthRegisteredService service = new OAuthRegisteredService();
            final String serviceGuid = iterator.next();
            final JSONObject serviceData = verifyService(response, serviceGuid);
            service.setId(new BigInteger(serviceGuid, HEX_RADIX).longValue());
            service.setName((serviceData.getString("name")));
            service.setDescription((serviceData.getString("description")));
            service.setServiceId((serviceData.getString("callbackUrl")));
            service.setBypassApprovalPrompt(Boolean.FALSE);
            service.setClientId((serviceData.getString("clientId")));
            service.setClientSecret((serviceData.getString("clientSecret")));
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

    /**
     * Verify the service information in response.
     *
     * @param response the response as a JSONObject
     * @param serviceGuid the service GUID as a String
     * @return the service as a JSONObject if verified; null otherwise
     */
    private JSONObject verifyService(final JSONObject response, final String serviceGuid) {

        JSONObject serviceData;

        try {
            serviceData = response.getJSONObject(serviceGuid);
        } catch (final JSONException e) {
            LOGGER.error("Fail to Parse OAuth Service. Service ID: {}", serviceGuid);
            LOGGER.error(e.toString());
            return null;
        }

        if (serviceData.has("name") && serviceData.has("description") && serviceData.has("callbackUrl")
                && serviceData.has("clientId") && serviceData.has("clientSecret")) {
            return serviceData;
        } else {
            LOGGER.error("Missing Information for OAuth Service. Service ID: {}", serviceGuid);
            return null;
        }
    }
}
