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

/**
 * The Open Science Framework Logout Handler.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
public class OpenScienceFrameworkLogoutHandler {

    @NotNull
    private static MongoOperations MongoTemplate;

    @Document(collection="node")
    private static class OpenScienceFrameworkInstitution {
        @Id
        private String nodeId;

        @Field("institution_id")
        private String institutionId;

        @Field("institution_logout_url")
        private String institutionlogoutUrl;

        @Field("is_deleted")
        private Boolean deleted;

        public String getNodeId() {
            return nodeId;
        }

        public void setId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getInstitutionId() {
            return institutionId;
        }

        public void setInstitutionId(String institutionId) {
            this.institutionId = institutionId;
        }

        public String getInstitutionlogoutUrl() {
            return institutionlogoutUrl;
        }

        public void setInstitutionlogoutUrl(String institutionlogoutUrl) {
            this.institutionlogoutUrl = institutionlogoutUrl;
        }

        public Boolean isDeleted() {
            return this.deleted;
        }

        public void setDeleted(Boolean deleted) {
            this.deleted = deleted;
        }
    }

    public void setMongoTemplate(MongoOperations mongoTemplate) {
        MongoTemplate = mongoTemplate;
    }

    protected static OpenScienceFrameworkInstitution FindInstitutionById(String institutionId) {
        if (institutionId == null) {
            return null;
        }
        return MongoTemplate.findOne(
            new Query(Criteria.where("institution_id").is(institutionId).and("isDeleted").is(Boolean.FALSE)),
            OpenScienceFrameworkInstitution.class
        );
    }

    public static String FindInstitutionLogoutUrlById(String institutionId) {
        OpenScienceFrameworkInstitution institution = FindInstitutionById(institutionId);
        return institution != null ? institution.getInstitutionlogoutUrl() : null;
    }
}
