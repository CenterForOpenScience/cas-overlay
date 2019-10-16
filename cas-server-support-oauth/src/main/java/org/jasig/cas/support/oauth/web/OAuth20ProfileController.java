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
import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.InvalidParameterException;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.personal.PersonalAccessToken;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.InvalidTokenException;
import org.jasig.cas.support.oauth.token.TokenType;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.validation.Assertion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The OAuth 2.0 "Profile" controller.
 *
 * This controller handles requests that ask for the profile (identifier, attributes and scopes) of the principal
 * associated with a given access token.
 *
 * @author Jerome Leleu
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuth20ProfileController extends AbstractController {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20ProfileController.class);

    /** The CAS OAuth authorization service. */
    private final CentralOAuthService centralOAuthService;

    /** The primary CAS authentication service. */
    private final CentralAuthenticationService centralAuthenticationService;

    /**
     * Instantiates a new {@link OAuth20ProfileController}.
     *
     * @param centralOAuthService the CAS OAuth service
     * @param centralAuthenticationService the primary CAS authentication service
     */
    public OAuth20ProfileController(
            final CentralOAuthService centralOAuthService,
            final CentralAuthenticationService centralAuthenticationService
    ) {
        this.centralOAuthService = centralOAuthService;
        this.centralAuthenticationService = centralAuthenticationService;
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

        // Verify and retrieve the access token. The token can be OAuth-granted by authorization or CAS-granted by
        // authentication. It can also be an OSF-generated PERSONAL access token, which does not exist in the token
        // registry (i.e. CAS database) the first time it is used.
        AccessToken accessToken;
        try {
            accessToken = centralOAuthService.getToken(accessTokenId, AccessToken.class);
        } catch (final InvalidTokenException e) {
            // If not found, check if it is an PERSONAL access token.
            final PersonalAccessToken personalAccessToken = centralOAuthService.getPersonalAccessToken(accessTokenId);
            if (personalAccessToken != null) {
                accessToken = centralOAuthService.grantPersonalAccessToken(personalAccessToken);
            } else {
                LOGGER.error("Could not get Access Token [{}]", accessTokenId);
                return OAuthUtils.writeJsonError(
                        response,
                        OAuthConstants.UNAUTHORIZED_REQUEST,
                        OAuthConstants.INVALID_ACCESS_TOKEN_DESCRIPTION,
                        HttpStatus.SC_UNAUTHORIZED
                );
            }
        }

        // Retrieve the principal id and released attributes associated with the access token.
        final Principal principal;
        final Map<String, Object> attributeMap = new HashMap<>();
        if (accessToken.getType() == TokenType.PERSONAL) {
            // PERSONAL access tokens do not have an OAuth registered service. Thus no attributes can be released.
            // TODO: Need to grant a service ticket here if we would like keep stats on ticket usage.
            principal = accessToken.getTicketGrantingTicket().getAuthentication().getPrincipal();
        } else {
            // Retrieve an existing or grant a new service ticket.
            final ServiceTicket serviceTicket;
            if (accessToken.getType() == TokenType.OFFLINE) {
                // OFFLINE access tokens are granted with a service ticket. Thus just use it here.
                serviceTicket = accessToken.getServiceTicket();
            } else {
                // Both ONLINE and CAS access tokens are granted with a ticket granting ticket and an associated
                // registered service. Thus, must grant a new service ticket here using the TGT and the service.
                serviceTicket = centralAuthenticationService
                        .grantServiceTicket(accessToken.getTicketGrantingTicket().getId(), accessToken.getService()
                );
            }
            // Validate the service ticket, and apply service specific attribute release policy
            final Assertion assertion;
            try {
                assertion = centralAuthenticationService
                        .validateServiceTicket(serviceTicket.getId(), serviceTicket.getService());
            } catch (final InvalidTicketException e) {
                LOGGER.error(
                        "Could not validate Service Ticket [{}] of Access Token [{}] ",
                        serviceTicket.getId(),
                        accessToken.getId()
                );
                return OAuthUtils.writeJsonError(
                        response,
                        OAuthConstants.UNAUTHORIZED_REQUEST,
                        OAuthConstants.INVALID_ACCESS_TOKEN_DESCRIPTION,
                        HttpStatus.SC_UNAUTHORIZED
                );
            }
            // Retrieve principal and attributes from the service ticket validation assertion.
            principal = assertion.getPrimaryAuthentication().getPrincipal();
            for (final Map.Entry<String, Object> attribute : principal.getAttributes().entrySet()) {
                attributeMap.put(attribute.getKey(), attribute.getValue());
            }
        }

        // Build and return the response.
        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, Object> map = new HashMap<>();
        map.put(OAuthConstants.PRINCIPAL_ID, principal.getId());
        if (attributeMap.size() > 0) {
            map.put(OAuthConstants.PRINCIPAL_ATTRIBUTES, attributeMap);
        }
        final Set<String> scopes = accessToken.getScopes();
        if (scopes.size() > 0) {
            map.put(OAuthConstants.SCOPE, accessToken.getScopes());
        }
        final String result = mapper.writeValueAsString(map);
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
