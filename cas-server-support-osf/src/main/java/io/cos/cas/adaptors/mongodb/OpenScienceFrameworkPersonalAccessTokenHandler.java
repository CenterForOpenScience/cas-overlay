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

import org.jasig.cas.support.oauth.personal.PersonalAccessToken;
import org.jasig.cas.support.oauth.personal.handler.support.AbstractPersonalAccessTokenHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashSet;

public class OpenScienceFrameworkPersonalAccessTokenHandler extends AbstractPersonalAccessTokenHandler
        implements InitializingBean {

    @NotNull
    private MongoOperations mongoTemplate;

    @Document(collection="apioauth2personaltoken")
    private class OpenScienceFrameworkPersonalToken {
        @Id
        private String id;
        @Field("token_id")
        private String tokenId;
        @Field("user_id")
        private String userId;
        private String scopes;
        private Boolean active;

        public String getUserId() {
            return this.userId;
        }

        public String getScopes() {
            return this.scopes;
        }

        public Boolean getActive() {
            return this.active;
        }

        @Override
        public String toString() {
            return "PersonalAccessToken [id=" + this.id + ", userId=" + this.userId + "]";
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public PersonalAccessToken getToken(String tokenId) {
        final OpenScienceFrameworkPersonalToken token = this.mongoTemplate.findOne(new Query(
                new Criteria().andOperator(
                        Criteria.where("token_id").is(tokenId),
                        Criteria.where("active").is(true)
                )
        ), OpenScienceFrameworkPersonalToken.class);

        if (token == null) {
            return null;
        }

        String scopes = token.scopes == null ? "" : token.scopes;
        return new PersonalAccessToken(token.tokenId, token.userId, new HashSet<>(Arrays.asList(scopes.split(" "))));
    }

    public void setMongoTemplate(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
}
