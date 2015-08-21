///*
// * Licensed to Apereo under one or more contributor license
// * agreements. See the NOTICE file distributed with this work
// * for additional information regarding copyright ownership.
// * Apereo licenses this file to you under the Apache License,
// * Version 2.0 (the "License"); you may not use this file
// * except in compliance with the License.  You may obtain a
// * copy of the License at the following location:
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package org.jasig.cas.support.oauth.web;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.http.HttpStatus;
//import org.jasig.cas.CentralAuthenticationService;
//import org.jasig.cas.services.ServicesManager;
//import org.jasig.cas.support.oauth.OAuthConstants;
//import org.jasig.cas.support.oauth.OAuthTokenUtils;
//import org.jasig.cas.support.oauth.OAuthUtils;
//import org.jasig.cas.support.oauth.TokenInvalidException;
//import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
//import org.jasig.cas.ticket.Ticket;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.web.servlet.ModelAndView;
//import org.springframework.web.servlet.mvc.AbstractController;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * This controller handles requests to revoke access and refresh tokens.
// *
// * @author Michael Haselton
// * @since 4.1.0
// */
//public final class OAuth20MetadataApplicationController extends AbstractController {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20MetadataApplicationController.class);
//
//    private static final String NAME = "name";
//
//    private static final String DESCRIPTION = "description";
//
//    private static final String USERS = "users";
//
//    private final ServicesManager servicesManager;
//
//    private final CentralAuthenticationService centralAuthenticationService;
//
//    /**
//     * Instantiates a new o auth20 revoke application tokens controller.
//     *
//     * @param servicesManager the services manager
//     * @param centralAuthenticationService the central authentication service
//     */
//    public OAuth20MetadataApplicationController(final ServicesManager servicesManager,
//                                                final CentralAuthenticationService centralAuthenticationService) {
//        this.servicesManager = servicesManager;
//        this.centralAuthenticationService = centralAuthenticationService;
//    }
//
//    @Override
//    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
//            throws Exception {
//        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
//        if (StringUtils.isBlank(clientId)) {
//            throw new TokenInvalidException();
//        }
//
//        String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);
//        if (StringUtils.isBlank(clientSecret)) {
//            throw new TokenInvalidException();
//        }
//
//        final OAuthRegisteredService service = OAuthUtils.getRegisteredOAuthService(servicesManager, clientId);
//        if (service == null) {
//            LOGGER.debug("Service not found for Client ID [{}]", clientId);
//            throw new TokenInvalidException();
//        }
//
//        if (!StringUtils.equals(service.getClientSecret(), clientSecret)) {
//            LOGGER.error("Invalid Client Secret specified for Client ID [{}]]", clientId);
//            throw new TokenInvalidException();
//        }
//
//        final Collection<Ticket> tickets = OAuthTokenUtils.getRefreshTokens(centralAuthenticationService, clientId);
//
//        final ObjectMapper mapper = new ObjectMapper();
//        final Map<String, Object> map = new HashMap<>();
//        map.put(OAuthConstants.CLIENT_ID, service.getClientId());
//        map.put(NAME, service.getName());
//        map.put(DESCRIPTION, service.getDescription());
//        map.put(USERS, tickets.size());
//
//        final String result = mapper.writeValueAsString(map);
//        LOGGER.debug("result : {}", result);
//
//        response.setContentType("application/json");
//        return OAuthUtils.writeText(response, result, HttpStatus.SC_OK);
//    }
//}
