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

import io.cos.cas.api.handler.APIEndpointHandler;
import io.cos.cas.api.type.APIErrors;
import io.cos.cas.api.type.APIEndpoint;
import org.apache.http.HttpStatus;
import org.jasig.cas.support.oauth.personal.PersonalAccessToken;
import org.jasig.cas.support.oauth.personal.handler.support.AbstractPersonalAccessTokenHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashSet;

/**
 * The Open Science FrameWork API OAuth2 Personal Access Token Handler.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public class OpenScienceFrameworkPersonalAccessTokenHandler extends AbstractPersonalAccessTokenHandler
        implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkPersonalAccessTokenHandler.class);

    @NotNull
    private APIEndpointHandler apiEndpointHandler;

    /** Default Constructor. */
    public OpenScienceFrameworkPersonalAccessTokenHandler() {}

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public PersonalAccessToken getToken(final String tokenId) {

        final JSONObject data = new JSONObject();
        data.put("tokenId", tokenId);
        final JSONObject response = apiEndpointHandler.handle(
                APIEndpoint.SERVICE_OAUTH_TOKEN,
                apiEndpointHandler.encryptPayload("data", data.toString())
        );

        if (response != null) {
            final int status = response.getInt("status");
            if (status == HttpStatus.SC_OK) {
                final JSONObject responseBody = response.getJSONObject("body");
                if (responseBody == null || !responseBody.has("ownerId") || !responseBody.has("tokenScopes")) {
                    LOGGER.debug("Invalid Response");
                    return null;
                }
                final String ownerGuid = (String) responseBody.get("ownerId");
                final String tokenScopes = (String) responseBody.get("tokenScopes");

                return new PersonalAccessToken(
                        tokenId,
                        ownerGuid,
                        new HashSet<>(Arrays.asList(tokenScopes.split(" ")))
                );
            } else if (status == HttpStatus.SC_BAD_REQUEST) {
                final APIErrors error = apiEndpointHandler.getAPIErrorFromResponse(response.getJSONObject("body"));
                LOGGER.error("API Request Failed: code={}, detail='{}'", error.getCode(), error.getDetail());
            }
        }

        return null;
    }
    public void setApiEndpointHandler(final APIEndpointHandler apiEndpointHandler) {
        this.apiEndpointHandler = apiEndpointHandler;
    }
}
