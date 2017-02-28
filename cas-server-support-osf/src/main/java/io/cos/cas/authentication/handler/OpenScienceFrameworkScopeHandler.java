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

import io.cos.cas.adaptors.api.OpenScienceFrameworkApiCasEndpoint;
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
 * @since 4.1.0
 */
public class OpenScienceFrameworkScopeHandler extends AbstractScopeHandler implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkScopeHandler.class);

    /** The Open Science Framework API CAS Endpoint instance. */
    @NotNull
    private OpenScienceFrameworkApiCasEndpoint osfApiCasEndpoint;

    /** Default Constructor. */
    public OpenScienceFrameworkScopeHandler() {}

    @Override
    public void afterPropertiesSet() throws Exception {}

    @Override
    public Scope getScope(final String scopeName) {

        final JSONObject data = new JSONObject();
        data.put("scopeName", scopeName);

        // encrypt the payload using JWE/JWT
        final String encryptedPayload = osfApiCasEndpoint.encryptPayload("data", data.toString());

        // talk to API `/cas/service/oauthScopes/` endpoint
        final JSONObject response = osfApiCasEndpoint.apiCasService("oauthScopes", encryptedPayload);
        if (response == null || !response.has("scopeDescription")) {
            LOGGER.error("Invalid Response");
            return null;
        }

        final String scopeDescription = (String) response.get("scopeDescription");
        return new Scope(scopeName, scopeDescription, Boolean.FALSE);
    }

    public void setOsfApiCasEndpoint(final OpenScienceFrameworkApiCasEndpoint osfApiCasEndpoint) {
        this.osfApiCasEndpoint = osfApiCasEndpoint;
    }
}
