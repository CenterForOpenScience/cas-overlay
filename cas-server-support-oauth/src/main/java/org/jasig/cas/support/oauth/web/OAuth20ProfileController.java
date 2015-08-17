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
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.oauth.*;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.RefreshToken;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * This controller returns a profile for the authenticated user
 * (identifier + attributes), found with the access token (CAS granting
 * ticket).
 *
 * @author Jerome Leleu
 * @author Michael Haselton
 * @since 3.5.0
 */
public final class OAuth20ProfileController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20ProfileController.class);

    private static final String ID = "id";

    private static final String ATTRIBUTES = "attributes";

    private static final String SCOPE = "scope";

    private final CentralAuthenticationService centralAuthenticationService;

    private final CentralOAuthService centralOAuthService;

    /**
     * Instantiates a new o auth20 profile controller.
     *
     * @param centralOAuthService the central oauth service
     */
    public OAuth20ProfileController(final CentralAuthenticationService centralAuthenticationService,
                                    final CentralOAuthService centralOAuthService) {
        this.centralAuthenticationService = centralAuthenticationService;
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
                return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_BAD_REQUEST);
                // TODO: verify the exception shows up properly to the user or remove it
//                throw new TokenInvalidException();
            }
        }

        final AccessToken accessToken;
        try {
            accessToken = centralOAuthService.getToken(accessTokenId, AccessToken.class);
        } catch (InvalidTicketException e) {
            LOGGER.error("Could not get Access Token [{}]", accessTokenId);
            return OAuthUtils.writeTextError(response, OAuthConstants.UNAUTHORIZED_REQUEST, HttpStatus.SC_UNAUTHORIZED);
            // TODO: verify the exception shows up properly to the user or remove it
//                throw new TokenUnauthorizedException();
        }

        // validate the service ticket, and apply service specific attribute release policy
        final Assertion assertion;
        try {
            assertion = centralAuthenticationService.validateServiceTicket(
                    accessToken.getServiceTicket().getId(), accessToken.getServiceTicket().getService());
        } catch (InvalidTicketException e) {
            LOGGER.error("Could not validate Service Ticket [{}]", accessToken.getServiceTicket().getId());
            return OAuthUtils.writeTextError(response, OAuthConstants.UNAUTHORIZED_REQUEST, HttpStatus.SC_UNAUTHORIZED);
        }

        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, Object> map = new HashMap<>();

        final Principal principal = assertion.getPrimaryAuthentication().getPrincipal();
        map.put(ID, principal.getId());

        final Map<String, Object> attributeMap = new HashMap<>();
        for (final Map.Entry<String, Object> attribute : principal.getAttributes().entrySet()) {
            attributeMap.put(attribute.getKey(), attribute.getValue());
        }
        map.put(ATTRIBUTES, attributeMap);

        // add scope if a refresh token exists, otherwise we consider this a login access token generated during login
        if (accessToken.getRefreshToken() != null) {
            map.put(SCOPE, accessToken.getRefreshToken().getScope());
        }

        final String result = mapper.writeValueAsString(map);
        LOGGER.debug("result : {}", result);

        response.setContentType("application/json");
        return OAuthUtils.writeText(response, result, HttpStatus.SC_OK);
    }
}
