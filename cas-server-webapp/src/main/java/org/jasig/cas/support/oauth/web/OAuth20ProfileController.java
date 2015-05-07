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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.validation.Assertion;
import org.jose4j.json.internal.json_simple.JSONArray;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * This controller returns a profile for the authenticated user
 * (identifier + attributes), found with the access token (CAS granting
 * ticket).
 *
 * @author Jerome Leleu, Michael Haselton
 * @since 3.5.0
 */
public final class OAuth20ProfileController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20ProfileController.class);

    private static final String ID = "id";

    private static final String ATTRIBUTES = "attributes";

    private final TicketRegistry ticketRegistry;

    private final CentralAuthenticationService centralAuthenticationService;

    /**
     * Instantiates a new o auth20 profile controller.
     *
     * @param ticketRegistry the ticket registry
     */
    public OAuth20ProfileController(final TicketRegistry ticketRegistry, final CentralAuthenticationService centralAuthenticationService) {
        this.ticketRegistry = ticketRegistry;
        this.centralAuthenticationService = centralAuthenticationService;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        String accessTokenId = request.getParameter(OAuthConstants.ACCESS_TOKEN);
        if (StringUtils.isBlank(accessTokenId)) {
            final String authHeader = request.getHeader("Authorization");
            if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith(OAuthConstants.BEARER_TOKEN + " ")) {
                accessTokenId = authHeader.substring(OAuthConstants.BEARER_TOKEN.length() + 1);
            }
        }
        LOGGER.debug("{} : {}", OAuthConstants.ACCESS_TOKEN, accessTokenId);
        // accessToken is required
        if (StringUtils.isBlank(accessTokenId)) {
            LOGGER.error("Missing {}", OAuthConstants.ACCESS_TOKEN);
            return OAuthUtils.writeTextError(response, OAuthConstants.MISSING_ACCESS_TOKEN, HttpStatus.SC_BAD_REQUEST);
        }

        // get service ticket, needed to lookup service for validation
        final ServiceTicket serviceTicket = (ServiceTicket) this.ticketRegistry.getTicket(accessTokenId);
        if (serviceTicket == null) {
            LOGGER.error("Unknown Service Ticket : {}", accessTokenId);
            return OAuthUtils.writeTextError(response, OAuthConstants.MISSING_ACCESS_TOKEN, HttpStatus.SC_NOT_FOUND);
        }

        // validate the service ticket, also applies attribute release policy
        final Assertion assertion;
        try {
            assertion = this.centralAuthenticationService.validateServiceTicket(accessTokenId, serviceTicket.getService());
        } catch (InvalidTicketException e) {
            LOGGER.error("Expired {} : {}", OAuthConstants.ACCESS_TOKEN, accessTokenId);
            return OAuthUtils.writeTextError(response, OAuthConstants.MISSING_ACCESS_TOKEN, HttpStatus.SC_BAD_REQUEST);
        }

        // generate profile : identifier + attributes
        final Principal principal = assertion.getPrimaryAuthentication().getPrincipal();

        final JSONObject result = new JSONObject();
        result.put(ID, principal.getId());

        final JSONArray resultAttributes = new JSONArray();
        result.put(ATTRIBUTES, resultAttributes);
        for (final Map.Entry<String, Object> attribute : principal.getAttributes().entrySet()) {
            JSONObject jsonAttribute = new JSONObject();
            jsonAttribute.put(attribute.getKey(), attribute.getValue());
            resultAttributes.add(jsonAttribute);
        }
        LOGGER.debug("result : {}", result);

        response.setContentType("application/json");
        return OAuthUtils.writeText(response, result.toString(), HttpStatus.SC_OK);
    }
}
