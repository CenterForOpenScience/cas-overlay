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
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.metadata.PrincipalMetadata;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.ticket.InvalidTicketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This controller handles requests for metadata regarding a principal.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public final class OAuth20MetadataPrincipalController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20MetadataPrincipalController.class);

    private static final String CLIENT_ID = "client_id";

    private static final String NAME = "name";

    private static final String DESCRIPTION = "description";

    private static final String SCOPE = "scope";

    private final CentralOAuthService centralOAuthService;

    /**
     * Instantiates a new o auth20 principal metadata controller.
     *
     * @param centralOAuthService the central oauth service
     */
    public OAuth20MetadataPrincipalController(final CentralOAuthService centralOAuthService) {
        this.centralOAuthService = centralOAuthService;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        String accessTokenId = request.getParameter(OAuthConstants.ACCESS_TOKEN);
        if (StringUtils.isBlank(accessTokenId)) {
            final String authHeader = request.getHeader("Authorization");
            if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith(OAuthConstants.BEARER_TOKEN + " ")) {
                accessTokenId = authHeader.substring(OAuthConstants.BEARER_TOKEN.length() + 1);
            } else {
                LOGGER.debug("Missing Access Token");
                return OAuthUtils.writeJsonError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_BAD_REQUEST);
            }
        }

        final AccessToken accessToken;
        try {
            accessToken = centralOAuthService.getToken(accessTokenId, AccessToken.class);
        } catch (final InvalidTicketException e) {
            LOGGER.error("Could not get Access Token [{}]", accessTokenId);
            return OAuthUtils.writeJsonError(response, OAuthConstants.UNAUTHORIZED_REQUEST, HttpStatus.SC_UNAUTHORIZED);
        }

        final Collection<PrincipalMetadata> metadata = centralOAuthService.getPrincipalMetadata(accessToken);

        final List<Map<String, Object>> metadataList = new ArrayList<>();
        for (final PrincipalMetadata item : metadata) {
            final Map<String, Object> detailMap = new HashMap<>();
            detailMap.put(CLIENT_ID, item.getClientId());
            detailMap.put(NAME, item.getName());
            detailMap.put(DESCRIPTION, item.getDescription());
            detailMap.put(SCOPE, item.getScopes());
            metadataList.add(detailMap);
        }

        final Map<String, Object> map = new HashMap<>();
        map.put("data", metadataList);

        final String result = new ObjectMapper().writeValueAsString(map);
        LOGGER.debug("result : {}", result);

        response.setContentType("application/json");
        return OAuthUtils.writeText(response, result, HttpStatus.SC_OK);
    }
}
