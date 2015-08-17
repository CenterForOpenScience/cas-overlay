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
package org.jasig.cas.support.oauth.authentication.principal;

import org.jasig.cas.authentication.principal.AbstractWebApplicationService;
import org.jasig.cas.authentication.principal.Response;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class OAuthService extends AbstractWebApplicationService {

    private static final String CONST_PARAM_TICKET = "ticket";

    private final Response.ResponseType responseType;

    /**
     * Instantiates a new web application service impl.
     *
     * @param id the id
     */
    public OAuthService(final String id) {
        this(id, id, null, null);
    }

    private OAuthService(final String id,
                         final String originalUrl,
                         final String artifactId,
                         final Response.ResponseType responseType) {
        super(id, originalUrl, artifactId);
        this.responseType = responseType;
    }

    @Override
    public Response getResponse(final String ticketId) {
        final Map<String, String> parameters = new HashMap<>();

        if (StringUtils.hasText(ticketId)) {
            parameters.put(CONST_PARAM_TICKET, ticketId);
        }

        if (Response.ResponseType.POST == this.responseType) {
            return Response.getPostResponse(getOriginalUrl(), parameters);
        }
        return Response.getRedirectResponse(getOriginalUrl(), parameters);
    }
}
