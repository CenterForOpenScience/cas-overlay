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
//import org.apache.http.HttpStatus;
//import org.jasig.cas.CentralAuthenticationService;
//import org.jasig.cas.authentication.principal.Principal;
//import org.jasig.cas.services.ServicesManager;
//import org.jasig.cas.support.oauth.*;
//import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
//import org.jasig.cas.ticket.Ticket;
//import org.jasig.cas.util.CipherExecutor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.web.servlet.ModelAndView;
//import org.springframework.web.servlet.mvc.AbstractController;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.*;
//
///**
// * This controller handles requests to enumerate client application tokens.
// *
// * @author Michael Haselton
// * @since 4.1.0
// */
//public final class OAuth20TokenAuthorizationController extends AbstractController {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20TokenAuthorizationController.class);
//
//    private static final String NAME = "name";
//
//    private static final String DESCRIPTION = "description";
//
//    private final ServicesManager serviceManager;
//
//    private final CentralAuthenticationService centralAuthenticationService;
//
//    private final CipherExecutor cipherExecutor;
//
//    /**
//     * Instantiates a new o auth20 authorized token controller.
//     *
//     * @param centralAuthenticationService the central authentication service
//     * @param cipherExecutor the cipher executor
//     */
//    public OAuth20TokenAuthorizationController(final ServicesManager serviceManager,
//                                               final CentralAuthenticationService centralAuthenticationService,
//                                               final CipherExecutor cipherExecutor) {
//        this.serviceManager = serviceManager;
//        this.centralAuthenticationService = centralAuthenticationService;
//        this.cipherExecutor = cipherExecutor;
//    }
//
//    @Override
//    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
//            throws Exception {
//        final OAuthToken accessToken = OAuthTokenUtils.getAccessToken(request, cipherExecutor);
//        final Ticket accessTicket = OAuthTokenUtils.getTicket(centralAuthenticationService, accessToken);
//        final Principal accessPrincipal = OAuthTokenUtils.getAuthentication(accessTicket).getPrincipal();
//
//        final Collection<Ticket> tickets = OAuthTokenUtils.getRefreshTokens(centralAuthenticationService, accessPrincipal);
//
//        final ObjectMapper mapper = new ObjectMapper();
//        final Map<String, Object> map = new HashMap<>();
//        final List<Map<String, Object>> ticketList = new ArrayList<>();
//        for (final Ticket ticket : tickets) {
//            String clientId = (String) OAuthTokenUtils.getAuthentication(ticket).getAttributes().get(OAuthConstants.CLIENT_ID);
//            OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(serviceManager, clientId);
//
//            final Map<String, Object> ticketMap = new HashMap<>();
//            ticketMap.put(OAuthConstants.CLIENT_ID, registeredService.getClientId());
//            ticketMap.put(NAME, registeredService.getName());
//            ticketMap.put(DESCRIPTION, registeredService.getDescription());
//            ticketList.add(ticketMap);
//        }
//        map.put("data", ticketList);
//        final String result = mapper.writeValueAsString(map);
//        LOGGER.debug("result : {}", result);
//
//        response.setContentType("application/json");
//        return OAuthUtils.writeText(response, result, HttpStatus.SC_OK);
//    }
//}
