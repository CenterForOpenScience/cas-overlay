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
import org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.jasig.cas.services.ServiceRegistryDao;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.validation.constraints.NotNull;
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
 * @since 4.1.0
 */
public class OpenScienceFrameworkServiceRegistryDao implements ServiceRegistryDao {

    private static final int HEX_RADIX = 16;

    @NotNull
    private MongoOperations mongoTemplate;

    /**
     * Map of service ID to registered service.
     */
    private Map<Long, RegisteredService> serviceMap = new ConcurrentHashMap<>();

    @Document(collection="apioauth2application")
    private static class OAuth {
        @Id
        private String id;
        private String name;
        private String description;
        @Field("callback_url")
        private String callbackUrl;
        @Field("client_id")
        private String clientId;
        @Field("client_secret")
        private String clientSecret;
        @Field("is_active")
        private Boolean isActive;

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getDescription() {
            return this.description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public String getCallbackUrl() {
            return this.callbackUrl;
        }

        public void setCallbackUrl(final String callbackUrl) {
            this.callbackUrl = callbackUrl;
        }

        public String getClientId() {
            return this.clientId;
        }

        public void setClientId(final String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return this.clientSecret;
        }

        public void setClientSecret(final String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public Boolean getIsActive() {
            return this.isActive;
        }

        public void setIsActive(final Boolean isActive) {
            this.isActive = isActive;
        }

        @Override
        public String toString() {
            return String.format("OAuth [id=%s, name=%s]", this.id, this.name);
        }
    }

    /**
     * Instantiates a new Open Science Framework service registry dao.
     *
     * @param mongoTemplate the mongo template
     */
    public OpenScienceFrameworkServiceRegistryDao(final MongoOperations mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
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
        final List<OAuth> oAuthServices = this.mongoTemplate.find(new Query(Criteria
                .where("isActive").is(Boolean.TRUE)
        ), OAuth.class);

        final ReturnAllowedAttributeReleasePolicy attributeReleasePolicy = new ReturnAllowedAttributeReleasePolicy();
        final ArrayList<String> allowedAttributes = new ArrayList<>();
        // e.g. global attribute release
        // allowedAttributes.add("username");
        // allowedAttributes.add("givenName");
        // allowedAttributes.add("familyName");
        attributeReleasePolicy.setAllowedAttributes(allowedAttributes);

        final Map<Long, RegisteredService> temp = new ConcurrentHashMap<>();
        for (final OAuth oAuthService : oAuthServices) {
            final OAuthRegisteredService service = new OAuthRegisteredService();
            service.setId(new BigInteger(oAuthService.getId(), HEX_RADIX).longValue());
            service.setName(oAuthService.getName());
            service.setDescription(oAuthService.getDescription());
            service.setServiceId(oAuthService.getCallbackUrl());
            service.setBypassApprovalPrompt(Boolean.FALSE);
            service.setClientId(oAuthService.getClientId());
            service.setClientSecret(oAuthService.getClientSecret());
            service.setAttributeReleasePolicy(attributeReleasePolicy);
            temp.put(service.getId(), service);
        }
        this.serviceMap = temp;
        return new ArrayList<>(this.serviceMap.values());
    }

    @Override
    public final RegisteredService findServiceById(final long id) {
        return serviceMap.get(id);
    }
}
