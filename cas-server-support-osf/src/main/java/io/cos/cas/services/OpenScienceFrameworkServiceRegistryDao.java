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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of <code>ServiceRegistryDao</code> that reads services definition from the
 * Open Science Framework at the Spring Application Context initialization time.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public class OpenScienceFrameworkServiceRegistryDao implements ServiceRegistryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkServiceRegistryDao.class);

    @NotNull
    private MongoOperations mongoTemplate;

    /**
     * Map of service ID to registered service.
     */
    private Map<Long, RegisteredService> serviceMap = new ConcurrentHashMap<>();

    @Document(collection="apioauth2application")
    private class OAuth {
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
        private Boolean active;

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return this.description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getCallbackUrl() {
            return this.callbackUrl;
        }

        public void setCallbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
        }

        public String getClientId() {
            return this.clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return this.clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public Boolean getActive() {
            return this.active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }

        @Override
        public String toString() {
            return "OAuth [id=" + this.id + ", name=" + this.name + "]";
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
        List<OAuth> oAuthServices = this.mongoTemplate.find(new Query(Criteria
                .where("active").is(true)
        ), OAuth.class);

        ReturnAllowedAttributeReleasePolicy attributeReleasePolicy = new ReturnAllowedAttributeReleasePolicy();
        ArrayList<String> allowedAttributes = new ArrayList<>();
        allowedAttributes.add("username");
        allowedAttributes.add("givenName");
        allowedAttributes.add("familyName");
        attributeReleasePolicy.setAllowedAttributes(allowedAttributes);

        final Map<Long, RegisteredService> temp = new ConcurrentHashMap<>();
        for (final OAuth oAuthService : oAuthServices) {
            OAuthRegisteredService service = new OAuthRegisteredService();
            service.setId(new BigInteger(oAuthService.getId()).longValue());
            service.setName(oAuthService.getName());
            service.setDescription(oAuthService.getDescription());
            service.setServiceId(oAuthService.getCallbackUrl());
            service.setBypassApprovalPrompt(false);
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
