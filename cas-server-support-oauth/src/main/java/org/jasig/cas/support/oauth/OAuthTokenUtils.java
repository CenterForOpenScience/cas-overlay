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

package org.jasig.cas.support.oauth;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.support.oauth.authentication.principal.OAuthCredential;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.util.CipherExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;


/**
 * OAuth Access Token Utilities
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public class OAuthTokenUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthTokenUtils.class);

    public static OAuthToken getAccessToken(final HttpServletRequest request, final CipherExecutor cipherExecutor)
            throws RuntimeException {
        String accessToken = request.getParameter(OAuthConstants.ACCESS_TOKEN);
        if (StringUtils.isBlank(accessToken)) {
            final String authHeader = request.getHeader("Authorization");
            if (StringUtils.isNotBlank(authHeader)
                    && authHeader.toLowerCase().startsWith(OAuthConstants.BEARER_TOKEN.toLowerCase() + " ")) {
                accessToken = authHeader.substring(OAuthConstants.BEARER_TOKEN.length() + 1);
            } else {
                LOGGER.debug("Missing access token");
                throw new TokenInvalidException();
            }
        }

        final OAuthToken token = readToken(cipherExecutor, accessToken);
        if (token == null) {
            LOGGER.debug("Could not read access token");
            throw new TokenInvalidException();
        }
        return token;
    }

    public static OAuthToken getToken(final HttpServletRequest request, final CipherExecutor cipherExecutor, final String name)
            throws RuntimeException {
        final String jwtToken = request.getParameter(name);
        final OAuthToken token = readToken(cipherExecutor, jwtToken);
        if (token == null) {
            LOGGER.debug("Could not read token [{}]", name);
            throw new TokenInvalidException();
        }
        return token;
    }

    public static Ticket getTicket(final CentralAuthenticationService centralAuthenticationService, final OAuthToken token)
            throws RuntimeException {
        final String ticketId = token.serviceTicketId != null ? token.serviceTicketId : token.ticketGrantingTicketId;
        try {
            return centralAuthenticationService.getTicket(ticketId, Ticket.class);
        } catch (InvalidTicketException e) {
            LOGGER.debug("Invalid or expired ticket [{}]", token);
            throw new TokenInvalidException();
        }
    }

    public static ServiceTicket getServiceTicket(final CentralAuthenticationService centralAuthenticationService,
                                                 final OAuthToken token)
            throws RuntimeException {
        if (StringUtils.isBlank(token.serviceTicketId)) {
            final Service service = new SimpleWebApplicationServiceImpl(token.serviceId);
            try {
                return centralAuthenticationService.grantServiceTicket(token.ticketGrantingTicketId, service);
            } catch (TicketException e) {
                LOGGER.debug("Invalid or expired ticket granting ticket [{}] for service [{}]", token.ticketGrantingTicketId, service);
                throw new TokenUnauthorizedException();
            }
        } else {
            try {
                return centralAuthenticationService.getTicket(token.serviceTicketId, Ticket.class);
            } catch (InvalidTicketException e) {
                LOGGER.debug("Invalid or expired service ticket [{}]", token.serviceTicketId);
                throw new TokenUnauthorizedException();
            }
        }
    }

    public static String getJsonWebToken(final CipherExecutor cipherExecutor, final Ticket ticket) {
        return getJsonWebToken(cipherExecutor, ticket, null);
    }

    public static String getJsonWebToken(final CipherExecutor cipherExecutor, final Ticket ticket, final Service service) {
        if (ticket instanceof TicketGrantingTicket) {
            return cipherExecutor.encode(new OAuthToken(ticket.getId(), service == null ? null : service.getId()).toString());
        }
        return cipherExecutor.encode(new OAuthToken(ticket.getId()).toString());
    }

    public static Boolean hasPermission(final Ticket accessTicket, final Ticket ticket) {
        final Authentication accessAuthentication = getAuthentication(accessTicket);
        final Principal accessPrincipal = accessAuthentication.getPrincipal();

        final Authentication ticketAuthentication = getAuthentication(ticket);
        final Principal ticketPrincipal = ticketAuthentication.getPrincipal();

        return accessPrincipal.getId().equals(ticketPrincipal.getId());
    }

    public static Authentication getAuthentication(final Ticket ticket) {
        return ticket instanceof TicketGrantingTicket ?
                ((TicketGrantingTicket) ticket).getAuthentication() : ticket.getGrantingTicket().getAuthentication();
    }

    /**
     * Fetch a new access ticket.
     *
     * @param centralAuthenticationService the central authentication service
     * @param refreshTicket the refresh ticket
     * @param service the service
     * @return ServiceTicket, if successful
     */
    public static ServiceTicket fetchAccessTicket(final CentralAuthenticationService centralAuthenticationService,
                                                  final TicketGrantingTicket refreshTicket, final Service service)
            throws TokenUnauthorizedException {
        try {
            return centralAuthenticationService.grantServiceTicket(refreshTicket.getId(), service);
        } catch (Exception e) {
            throw new TokenUnauthorizedException();
        }
    }

    /**
     * Fetch a new refresh ticket.
     *
     * @param centralAuthenticationService the central authentication service
     * @param clientId the client id
     * @param principal the principal
     * @return TicketGrantingTicket, if successful
     */
    public static TicketGrantingTicket fetchRefreshTicket(final CentralAuthenticationService centralAuthenticationService,
                                                          final String clientId, final Principal principal)
            throws TokenUnauthorizedException {
        final OAuthCredential credential = new OAuthCredential(clientId, principal.getId(), principal.getAttributes());
        try {
            return centralAuthenticationService.createTicketGrantingTicket(credential);
        } catch (final Exception e) {
            throw new TokenUnauthorizedException();
        }
    }

    /**
     * Attempt to locate and return an existing refresh token.
     *
     * @param centralAuthenticationService the central authentication service
     * @param clientId the client id
     * @param principal the principal
     * @return TicketGrantingTicket refresh token or null
     */
    public static TicketGrantingTicket getRefreshToken(final CentralAuthenticationService centralAuthenticationService, final String clientId, final Principal principal) {
        final Collection<Ticket> tickets = centralAuthenticationService.getTickets(new Predicate() {
            @Override
            public boolean evaluate(final Object currentTicket) {
                if (currentTicket instanceof TicketGrantingTicket) {
                    final TicketGrantingTicket currentTicketGrantingTicket = (TicketGrantingTicket) currentTicket;
                    final Principal currentPrincipal = currentTicketGrantingTicket.getAuthentication().getPrincipal();
                    final Map<String, Object> currentAttributes = currentTicketGrantingTicket.getAuthentication().getAttributes();

                    if ((currentAttributes.containsKey(OAuthCredential.AUTHENTICATION_ATTRIBUTE_OAUTH) && (Boolean) currentAttributes.get(OAuthCredential.AUTHENTICATION_ATTRIBUTE_OAUTH))
                            && (currentAttributes.containsKey(OAuthConstants.CLIENT_ID) && currentAttributes.get(OAuthConstants.CLIENT_ID).equals(clientId))
                            && currentPrincipal.getId().equals(principal.getId())) {
                        return !currentTicketGrantingTicket.isExpired();
                    }
                }
                return false;
            }
        });

        if (tickets.size() == 1) {
            return (TicketGrantingTicket) tickets.iterator().next();
        }
        return null;
    }

    /**
     * Return a list of refresh tokens associated with a specific client application.
     *
     * @param centralAuthenticationService the central authentication service
     * @param clientId the client id
     * @return Collection of refresh tokens for the application specified
     */
    public static Collection<Ticket> getRefreshTokens(final CentralAuthenticationService centralAuthenticationService, final String clientId) {
        return centralAuthenticationService.getTickets(new Predicate() {
            @Override
            public boolean evaluate(final Object currentTicket) {
                if (currentTicket instanceof TicketGrantingTicket) {
                    final TicketGrantingTicket currentTicketGrantingTicket = (TicketGrantingTicket) currentTicket;
                    final Map<String, Object> currentAttributes = currentTicketGrantingTicket.getAuthentication().getAttributes();

                    if ((currentAttributes.containsKey(OAuthCredential.AUTHENTICATION_ATTRIBUTE_OAUTH) && (Boolean) currentAttributes.get(OAuthCredential.AUTHENTICATION_ATTRIBUTE_OAUTH))
                            && (currentAttributes.containsKey(OAuthConstants.CLIENT_ID) && currentAttributes.get(OAuthConstants.CLIENT_ID).equals(clientId))) {
                        return !currentTicketGrantingTicket.isExpired();
                    }
                }
                return false;
            }
        });
    }

    /**
     * Return a list of refresh tokens associated with the a principal.
     *
     * @param centralAuthenticationService the central authentication service
     * @param principal the principal
     * @return Collection of refresh tokens associated with the principal specified
     */
    public static Collection<Ticket> getRefreshTokens(final CentralAuthenticationService centralAuthenticationService, final Principal principal) {
        return centralAuthenticationService.getTickets(new Predicate() {
            @Override
            public boolean evaluate(final Object currentTicket) {
                if (currentTicket instanceof TicketGrantingTicket) {
                    final TicketGrantingTicket currentTicketGrantingTicket = (TicketGrantingTicket) currentTicket;
                    final Principal currentPrincipal = currentTicketGrantingTicket.getAuthentication().getPrincipal();
                    final Map<String, Object> currentAttributes = currentTicketGrantingTicket.getAuthentication().getAttributes();

                    if ((currentAttributes.containsKey(OAuthCredential.AUTHENTICATION_ATTRIBUTE_OAUTH) && (Boolean) currentAttributes.get(OAuthCredential.AUTHENTICATION_ATTRIBUTE_OAUTH))
                            && currentPrincipal.getId().equals(principal.getId())) {
                        return !currentTicketGrantingTicket.isExpired();
                    }
                }
                return false;
            }
        });
    }

    private static OAuthToken readToken(final CipherExecutor cipherExecutor, final String jwtToken) {
        try {
            return OAuthToken.read(cipherExecutor.decode(jwtToken));
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }
}
