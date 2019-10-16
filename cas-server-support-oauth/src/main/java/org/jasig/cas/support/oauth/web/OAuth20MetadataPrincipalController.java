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
import org.jasig.cas.support.oauth.metadata.PrincipalMetadata;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.InvalidTokenException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The OAuth 2.0 "Metadata Principal" Controller.
 *
 * This controller handles requests that ask for the metadata regarding a principal. The principal is identified and
 * authenticated by the given access token. In addition, the access token must be of type CAS.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuth20MetadataPrincipalController extends AbstractController {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20MetadataPrincipalController.class);

    /** The CAS OAuth authorization service. */
    private final CentralOAuthService centralOAuthService;

    /**
     * Instantiates a new {@link OAuth20MetadataPrincipalController}.
     *
     * @param centralOAuthService the CAS OAuth service
     */
    public OAuth20MetadataPrincipalController(final CentralOAuthService centralOAuthService) {
        this.centralOAuthService = centralOAuthService;
    }

    @Override
    protected ModelAndView handleRequestInternal(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        // Verify that all required OAuth 2.0 parameters are provided.
        final String prefixedBearerToken = request.getHeader(OAuthConstants.AUTHORIZATION_HEADER);
        LOGGER.debug("{} : {}", OAuthConstants.BEARER_TOKEN, prefixedBearerToken);
        String accessTokenId = request.getParameter(OAuthConstants.ACCESS_TOKEN);
        LOGGER.debug("{} : {}", OAuthConstants.ACCESS_TOKEN, accessTokenId);
        try {
            accessTokenId = verifyRequest(accessTokenId, prefixedBearerToken);
        } catch (final InvalidParameterException e) {
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.INVALID_REQUEST,
                    e.getMessage(),
                    HttpStatus.SC_BAD_REQUEST
            );
        }

        // Verify and retrieve the access token.
        final AccessToken accessToken;
        try {
            accessToken = centralOAuthService.getToken(accessTokenId, AccessToken.class);
        } catch (final InvalidTokenException e) {
            LOGGER.error("Could not get Access Token [{}]", accessTokenId);
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.UNAUTHORIZED_REQUEST,
                    OAuthConstants.INVALID_ACCESS_TOKEN_DESCRIPTION,
                    HttpStatus.SC_UNAUTHORIZED
            );
        }

        // Retrieve the metadata about the principal identified and authenticated by the given access token.
        final Collection<PrincipalMetadata> metadata;
        try {
            metadata = centralOAuthService.getPrincipalMetadata(accessToken);
        } catch (final InvalidTokenException e) {
            LOGGER.error("Invalid Access Token [{}] type [{}]", accessToken.getId(), accessToken.getType());
            return OAuthUtils.writeJsonError(
                    response,
                    OAuthConstants.UNAUTHORIZED_REQUEST,
                    OAuthConstants.INVALID_ACCESS_TOKEN_TYPE_DESCRIPTION,
                    HttpStatus.SC_UNAUTHORIZED
            );
        }

        // Build and return the response.
        final List<Map<String, Object>> metadataList = new ArrayList<>();
        for (final PrincipalMetadata item : metadata) {
            final Map<String, Object> detailMap = new HashMap<>();
            detailMap.put(OAuthConstants.CLIENT_ID, item.getClientId());
            detailMap.put(OAuthConstants.SERVICE_NAME, item.getName());
            detailMap.put(OAuthConstants.SERVICE_DESCRIPTION, item.getDescription());
            detailMap.put(OAuthConstants.SCOPE, item.getScopes());
            metadataList.add(detailMap);
        }
        final Map<String, Object> map = new HashMap<>();
        map.put("data", metadataList);
        final String result = new ObjectMapper().writeValueAsString(map);
        LOGGER.debug("result : {}", result);
        response.setContentType("application/json");
        return OAuthUtils.writeText(response, result, HttpStatus.SC_OK);
    }

    /**
     * Verify that all required OAuth 2.0 parameters are provided.
     *
     * @param accessTokenId the access token id
     * @param prefixedBearerToken the prefixed bearer token provided by the HTTP Authorization header
     * @return the access token id
     * @throws InvalidParameterException with the name of the invalid parameter
     */
    private String verifyRequest(
            final String accessTokenId,
            final String prefixedBearerToken
    ) throws InvalidParameterException {

        // An access token must be provided via either the request body or the HTTP "Authorization" header.
        if (StringUtils.isBlank(accessTokenId)) {
            if (StringUtils.isNotBlank(prefixedBearerToken)
                    && prefixedBearerToken.startsWith(OAuthConstants.BEARER_TOKEN + " ")
            ) {
                return prefixedBearerToken.substring(OAuthConstants.BEARER_TOKEN.length() + 1);
            } else {
                LOGGER.debug(OAuthConstants.MISSING_ACCESS_TOKEN_DESCRIPTION);
                throw new InvalidParameterException(OAuthConstants.ACCESS_TOKEN);
            }
        } else {
            return accessTokenId;
        }
    }
}
