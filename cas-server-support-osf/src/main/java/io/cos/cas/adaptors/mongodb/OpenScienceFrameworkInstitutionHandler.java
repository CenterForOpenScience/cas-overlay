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

package io.cos.cas.adaptors.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

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
    private MongoOperations mongoTemplate;

    @Document(collection="node")
    private static class OpenScienceFrameworkInstitution {
        @Id
        private String nodeId;

        @Field("institution_id")
        private String institutionId;

        @Field("title")
        private String name;

        @Field("institution_logo_name")
        private String logoName;

        @Field("institution_auth_url")
        private String loginUrl;

        @Field("institution_logout_url")
        private String logoutUrl;

        @Field("is_deleted")
        private Boolean deleted;

        public String getNodeId() {
            return nodeId;
        }

        public String getInstitutionId() {
            return institutionId;
        }

        public Boolean isDeleted() {
            return this.deleted;
        }

        public String getName() {
            return name;
        }

        public String getLoginUrl() {
            return loginUrl;
        }

        public String getLogoutUrl() {
            return logoutUrl;
        }

        @Override
        public String toString() {
            return String.format("OpenScienceFrameworkInstitution [nodeId=%s, institutionId=%s, institutionName=%s",
                    this.nodeId, this.institutionId, this.name);
        }
    }

    public void setMongoTemplate(final MongoOperations mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Find the institution by id. Return the institution instance or null.
     * @param institutionId The institution id
     * @return OpenScienceFrameworkInstitution or null
     */
    private OpenScienceFrameworkInstitution findInstitutionById(final String institutionId) {
        if (institutionId == null) {
            return null;
        }
        final OpenScienceFrameworkInstitution institution = this.mongoTemplate.findOne(
            new Query(Criteria.where("institution_id").is(institutionId).and("is_deleted").is(Boolean.FALSE)),
            OpenScienceFrameworkInstitution.class
        );
        return institution;
    }

    /**
     * Find the institution logout url by id. Return the logout url or null.
     * @param institutionId The institution id
     * @return String
     */
    public String findInstitutionLogoutUrlById(final String institutionId) {
        final OpenScienceFrameworkInstitution institution = this.findInstitutionById(institutionId);
        return institution != null ? institution.getLogoutUrl() : null;
    }

    /**
     * Return a map of institution name and login url.
     * @return Map&lt;String, String&gt;
     */
    public Map<String, String> getInstitutionLogin() {
        final List<OpenScienceFrameworkInstitution> institutionList = this.mongoTemplate.find(
            new Query(Criteria.where("institution_id").ne(null).and("institution_auth_url").ne(null).and("is_deleted").is(Boolean.FALSE)),
            OpenScienceFrameworkInstitution.class
        );
        final Map<String, String> institutionLogin = new HashMap<>();
        for (final OpenScienceFrameworkInstitution institution: institutionList) {
            institutionLogin.put(institution.getLoginUrl(), institution.getName());
        }
        return institutionLogin;
    }
}
