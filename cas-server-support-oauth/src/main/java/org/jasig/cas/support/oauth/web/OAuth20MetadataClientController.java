/*
 * Copyright (c) 2015. Center for Open Science
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The OAuth 2.0 "Metadata Client" Controller.
 *
 * This controller handles requests that ask for the metadata regarding an OAuth registered service. The service is
 * identified and retrieved by the given client id while the access check (authentication) is verified using the given
 * client secret.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuth20MetadataClientController extends AbstractController {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20MetadataClientController.class);

    /** The CAS OAuth authorization service. */
    private final CentralOAuthService centralOAuthService;

    /**
     * Instantiates a new {@link OAuth20MetadataClientController}.
     *
     * @param centralOAuthService the CAS OAuth service
     */
    public OAuth20MetadataClientController(final CentralOAuthService centralOAuthService) {
        this.centralOAuthService = centralOAuthService;
    }

    @Override
    protected ModelAndView handleRequestInternal(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        // Verify that all required OAuth 2.0 parameters are provided.
        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_ID, clientId);
        final String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_SECRET, "************");
        try {
            verifyRequest(clientId, clientSecret);
        } catch (final InvalidParameterException e) {
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_REQUEST,
                    e.getMessage(),
                    HttpStatus.SC_BAD_REQUEST
            );
        }

        // Retrieve metadata about the OAuth registered service using the given client id and secret.
        final ClientMetadata metadata = centralOAuthService.getClientMetadata(clientId, clientSecret);
        if (metadata == null) {
            LOGGER.debug("Metadata could not be retrieved for the Client ID and Client Secret specified");
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_REQUEST,
                    OAuthConstants.INVALID_CLIENT_ID_OR_SECRET_DESCRIPTION,
                    HttpStatus.SC_BAD_REQUEST
            );
        }

        // Build and return the response.
        final Map<String, Object> map = new HashMap<>();
        map.put(OAuthConstants.CLIENT_ID, metadata.getClientId());
        map.put(OAuthConstants.SERVICE_NAME, metadata.getName());
        map.put(OAuthConstants.SERVICE_DESCRIPTION, metadata.getDescription());
        map.put(OAuthConstants.SERVICE_USERS, metadata.getUsers());
        final String result = new ObjectMapper().writeValueAsString(map);
        LOGGER.debug("result : {}", result);
        response.setContentType("application/json");
        return OAuthUtils.writeText(response, result, HttpStatus.SC_OK);
    }

    /**
     * Verify that all required OAuth 2.0 parameters are provided.
     *
     * @param clientId the client id
     * @param clientSecret the client secret
     * @throws InvalidParameterException with the name of the invalid parameter
     */
    private void verifyRequest(final String clientId, final String clientSecret) throws InvalidParameterException {

        if (StringUtils.isBlank(clientId)) {
            LOGGER.error(OAuthConstants.MISSING_CLIENT_ID_DESCRIPTION);
            throw new InvalidParameterException(OAuthConstants.CLIENT_ID);
        }

        if (StringUtils.isBlank(clientSecret)) {
            LOGGER.error(OAuthConstants.MISSING_CLIENT_SECRET_DESCRIPTION);
            throw new InvalidParameterException(OAuthConstants.CLIENT_SECRET);
        }
    }
}
