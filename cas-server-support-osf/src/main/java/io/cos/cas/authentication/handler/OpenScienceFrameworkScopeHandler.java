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

package io.cos.cas.authentication.handler;

import io.cos.cas.api.handler.ApiEndpointHandler;
import io.cos.cas.api.type.ApiEndpoint;
import org.jasig.cas.support.oauth.scope.Scope;
import org.jasig.cas.support.oauth.scope.handler.support.AbstractScopeHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.validation.constraints.NotNull;

/**
 * The Open Science Framework Scope handler.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public class OpenScienceFrameworkScopeHandler extends AbstractScopeHandler implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkScopeHandler.class);

    @NotNull
    private ApiEndpointHandler apiEndpointHandler;

    /** Default Constructor. */
    public OpenScienceFrameworkScopeHandler() {}

    @Override
    public void afterPropertiesSet() throws Exception {}

    @Override
    public Scope getScope(final String scopeName) {

        final JSONObject data = new JSONObject();
        data.put("scopeName", scopeName);

        // encrypt the payload using JWE/JWT
        final String encryptedPayload = apiEndpointHandler.encryptPayload("data", data.toString());

        // talk to API `/cas/service/oauthScopes/` endpoint
        final JSONObject response = apiEndpointHandler.apiCasService(ApiEndpoint.SERVICE_OAUTH_SCOPES, encryptedPayload);
        if (response == null || !response.has("scopeDescription")) {
            LOGGER.debug("Invalid Response");
            return null;
        }

        final String scopeDescription = (String) response.get("scopeDescription");
        return new Scope(scopeName, scopeDescription, Boolean.FALSE);
    }

    public void setApiEndpointHandler(final ApiEndpointHandler apiEndpointHandler) {
        this.apiEndpointHandler = apiEndpointHandler;
    }
}
