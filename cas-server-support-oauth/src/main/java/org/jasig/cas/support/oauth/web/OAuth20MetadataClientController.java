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
package org.jasig.cas.support.oauth.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.InvalidParameterException;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.metadata.ClientMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * This controller handles requests for metadata regarding a client.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public final class OAuth20MetadataClientController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20MetadataClientController.class);

    private static final String CLIENT_ID = "client_id";

    private static final String NAME = "name";

    private static final String DESCRIPTION = "description";

    private static final String USERS = "users";

    private final CentralOAuthService centralOAuthService;

    /**
     * Instantiates a new o auth20 client metadata controller.
     *
     * @param centralOAuthService the central oauth service
     */
    public OAuth20MetadataClientController(final CentralOAuthService centralOAuthService) {
        this.centralOAuthService = centralOAuthService;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_ID, clientId);

        final String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_SECRET, "************");

        try {
            verifyRequest(clientId, clientSecret);
        } catch (final InvalidParameterException e) {
            return OAuthUtils.writeJsonError(response, OAuthConstants.INVALID_REQUEST, e.getMessage(), HttpStatus.SC_BAD_REQUEST);
        }

        final ClientMetadata metadata = centralOAuthService.getClientMetadata(clientId, clientSecret);

        final Map<String, Object> map = new HashMap<>();
        map.put(CLIENT_ID, metadata.getClientId());
        map.put(NAME, metadata.getName());
        map.put(DESCRIPTION, metadata.getDescription());
        map.put(USERS, metadata.getUsers());

        final String result = new ObjectMapper().writeValueAsString(map);
        LOGGER.debug("result : {}", result);

        response.setContentType("application/json");
        return OAuthUtils.writeText(response, result, HttpStatus.SC_OK);
    }

    /**
     * Verify the request by reviewing the values of client id, etc...
     *
     * @param clientId the client id
     * @param clientSecret the client secret
     * @throws InvalidParameterException with the name of the invalid parameter
     */
    private void verifyRequest(final String clientId, final String clientSecret) throws InvalidParameterException {
        // clientId is required
        if (StringUtils.isBlank(clientId)) {
            LOGGER.error("Missing {}", OAuthConstants.CLIENT_ID);
            throw new InvalidParameterException(OAuthConstants.CLIENT_ID);
        }
        // clientSecret is required
        if (StringUtils.isBlank(clientSecret)) {
            LOGGER.error("Missing {}", OAuthConstants.CLIENT_SECRET);
            throw new InvalidParameterException(OAuthConstants.CLIENT_SECRET);
        }
    }
}
