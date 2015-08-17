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

import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.authentication.principal.OAuthCredential;
import org.jasig.cas.support.oauth.authentication.principal.OAuthService;
import org.jasig.cas.support.oauth.scope.OAuthScope;
import org.jasig.cas.support.oauth.scope.OAuthScopeManager;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.token.*;
import org.jasig.cas.support.oauth.token.registry.TokenRegistry;
import org.jasig.cas.ticket.*;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * todo...
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public class CentralOAuthServiceImpl implements CentralOAuthService {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** CentralAuthenticationService for requesting tickets as needed. */
    @NotNull
    private final CentralAuthenticationService centralAuthenticationService;

    /** ServicesManager for verifying service endpoints. */
    @NotNull
    private final ServicesManager servicesManager;

    /** TokenRegistry for storing and retrieving tokens as needed. */
    @NotNull
    private final TicketRegistry ticketRegistry;

    /** TokenRegistry for storing and retrieving tokens as needed. */
    @NotNull
    private final TokenRegistry tokenRegistry;

    /** OAuthScopeManager for storing and retrieving scopes as needed. */
    @NotNull
    private final OAuthScopeManager scopeManager;

    /**
     * UniqueTicketIdGenerator to generate ids for CodeTokens
     * created.
     */
    @NotNull
    private final UniqueTicketIdGenerator codeTokenUniqueIdGenerator;

    /**
     * UniqueTicketIdGenerator to generate ids for RefreshTokens
     * created.
     */
    @NotNull
    private final UniqueTicketIdGenerator refreshTokenUniqueIdGenerator;

    /**
     * UniqueTicketIdGenerator to generate ids for AccessTokens
     * created.
     */
    @NotNull
    private final UniqueTicketIdGenerator accessTokenUniqueIdGenerator;

    /**
     * Build the central oauth service implementation.
     *
     * @param tokenRegistry the tokens registry.
     * @param codeTokenUniqueIdGenerator the Code Token id generator.
     * @param refreshTokenUniqueIdGenerator the Refresh Token id generator.
     * @param accessTokenUniqueIdGenerator the Access Token id generator.
     */
    public CentralOAuthServiceImpl(final CentralAuthenticationService centralAuthenticationService,
                                   final ServicesManager servicesManager,
                                   final TicketRegistry ticketRegistry,
                                   final TokenRegistry tokenRegistry,
                                   final UniqueTicketIdGenerator codeTokenUniqueIdGenerator,
                                   final UniqueTicketIdGenerator refreshTokenUniqueIdGenerator,
                                   final UniqueTicketIdGenerator accessTokenUniqueIdGenerator,
                                   final OAuthScopeManager scopeManager) {
        this.centralAuthenticationService = centralAuthenticationService;
        this.servicesManager = servicesManager;
        this.ticketRegistry = ticketRegistry;
        this.tokenRegistry = tokenRegistry;
        this.codeTokenUniqueIdGenerator = codeTokenUniqueIdGenerator;
        this.refreshTokenUniqueIdGenerator = refreshTokenUniqueIdGenerator;
        this.accessTokenUniqueIdGenerator = accessTokenUniqueIdGenerator;
        this.scopeManager = scopeManager;
    }

    @Override
    @Transactional(readOnly = false)
    public CodeToken grantCodeToken(final String ticketGrantingTicketId,
                                    final String clientId,
                                    final String callbackUrl,
                                    final Set<String> scope) throws TicketException {
        final Service service = new SimpleWebApplicationServiceImpl(callbackUrl);
        final ServiceTicket serviceTicket = centralAuthenticationService.grantServiceTicket(ticketGrantingTicketId, service);
        final CodeTokenImpl codeToken = new CodeTokenImpl(
                this.codeTokenUniqueIdGenerator.getNewTicketId(CodeToken.PREFIX),
                serviceTicket, clientId, callbackUrl, scope);
        this.tokenRegistry.addToken(codeToken);
        return codeToken;
    }

    @Override
    @Transactional(readOnly = false)
    public RefreshToken grantRefreshToken(final String code,
                                          final String clientId,
                                          final String clientSecret,
                                          final String redirectUri) throws TicketException {
        final CodeToken codeToken = getToken(code, CodeToken.class);
        logger.debug("Code Token : {} : {}", codeToken.getId(), codeToken.getServiceTicket().getId());
        if (codeToken.getServiceTicket().isExpired()) {
            logger.error("{} (Service Ticket) expired : {}", OAuthConstants.CODE, codeToken.getServiceTicket().getId());
            throw new TokenUnauthorizedException();
        }

        final OAuthRegisteredService service = OAuthUtils.getRegisteredOAuthService(servicesManager, clientId);
        if (service == null) {
            logger.error("Unknown {} : {}", OAuthConstants.CLIENT_ID, clientId);
            throw new TokenUnauthorizedException();
        }
        if (!StringUtils.equals(service.getClientSecret(), clientSecret)) {
            logger.error("Wrong client secret for service {}", service);
            throw new TokenUnauthorizedException();
        }
        if (!redirectUri.matches(service.getServiceId())) {
            logger.error("Unsupported {} : {} for serviceId : {}", OAuthConstants.REDIRECT_URI, redirectUri, service.getServiceId());
            throw new TokenUnauthorizedException();
        }

        final TicketGrantingTicket ticketGrantingTicket = codeToken.getServiceTicket().getGrantingTicket();
        // remove the service ticket, doing so will also remove the code token.
        ticketRegistry.deleteTicket(codeToken.getServiceTicket().getId());

        // check for an existing refresh token
        RefreshToken refreshToken = tokenRegistry.getRefreshToken(clientId, ticketGrantingTicket.getAuthentication().getPrincipal().getId());
        if (refreshToken != null) {
            Set<String> refreshTokenScope = refreshToken.getScope();
            for (final String scope : codeToken.getScope()) {
                if (!refreshTokenScope.contains(scope)) {
                    refreshTokenScope.add(scope);
                }
            }
            tokenRegistry.updateToken(refreshToken);
        } else {
            final Principal loginPrincipal = ticketGrantingTicket.getAuthentication().getPrincipal();
            final OAuthCredential credential = new OAuthCredential(loginPrincipal.getId(), loginPrincipal.getAttributes());

            final TicketGrantingTicket refreshTicket;
            try {
                refreshTicket = centralAuthenticationService.createTicketGrantingTicket(credential);
            } catch (final Exception e) {
                throw new TokenUnauthorizedException();
            }

            refreshToken = new RefreshTokenImpl(
                    refreshTokenUniqueIdGenerator.getNewTicketId(RefreshToken.PREFIX),
                    refreshTicket, clientId, redirectUri, codeToken.getScope());
            tokenRegistry.addToken(refreshToken);
        }

        logger.debug("{} : {}", OAuthConstants.REFRESH_TOKEN, refreshToken);
        return refreshToken;
    }

    @Override
    @Transactional(readOnly = false)
    public AccessToken grantAccessToken(RefreshToken refreshToken) throws TicketException {
        final Service service = new OAuthService(refreshToken.getRedirectUri());

        final ServiceTicket accessTicket;
        try {
            accessTicket = centralAuthenticationService.grantServiceTicket(refreshToken.getTicketGrantingTicket().getId(), service);
        } catch (Exception e) {
            throw new TokenUnauthorizedException();
        }

        final AccessToken accessToken = new AccessTokenImpl(
                accessTokenUniqueIdGenerator.getNewTicketId(AccessToken.PREFIX),
                refreshToken,
                accessTicket);
        tokenRegistry.addToken(accessToken);

        logger.debug("{} : {}", OAuthConstants.ACCESS_TOKEN, accessToken);
        return accessToken;
    }

    @Override
    @Transactional(readOnly = false)
    public AccessToken grantAccessToken(String ticketGrantingTicketId, String serviceId) throws TicketException {
        final Service service = new OAuthService(serviceId);
        final TicketGrantingTicket ticketGrantingTicket = centralAuthenticationService.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);

        final ServiceTicket accessTicket;
        try {
            accessTicket = centralAuthenticationService.grantServiceTicket(ticketGrantingTicket.getId(), service);
        } catch (TicketException ex) {
            throw new TokenUnauthorizedException();
        }

        final AccessToken accessToken = new AccessTokenImpl(
                accessTokenUniqueIdGenerator.getNewTicketId(AccessToken.PREFIX),
                accessTicket);
        tokenRegistry.addToken(accessToken);

        accessTicket.isExpired();

        logger.debug("{} : {}", OAuthConstants.ACCESS_TOKEN, accessToken);
        return accessToken;
    }

    @Transactional(readOnly = true)
    public RefreshToken getRefreshToken(String clientId, String principalId)
    {
        return tokenRegistry.getRefreshToken(clientId, principalId);
    }

    @Transactional(readOnly = true)
    @Timed(name = "GET_TOKEN_TIMER")
    @Metered(name = "GET_TOKEN_METER")
    @Counted(name="GET_TOKEN_COUNTER", monotonic=true)
    @SuppressWarnings("unchecked")
    public <T extends Token> T getToken(final String tokenId, final Class<? extends Token> clazz)
            throws InvalidTicketException {
        Assert.notNull(tokenId, "tokenId cannot be null");
        final Token token = this.tokenRegistry.getToken(tokenId, clazz);

        if (token == null) {
            logger.debug("Token [{}] by type [{}] cannot be found in the token registry.", tokenId, clazz.getSimpleName());
            // TODO: create token exception
            throw new InvalidTicketException(tokenId);
        }

        return (T) token;
    }

    public Map<String, OAuthScope> getScopeMap(String scope) {
        final Map<String, OAuthScope> scopeMap = new HashMap<>();
        if (scope != null) {
            for (String name : scope.split(",")) {
                OAuthScope oAuthScope = scopeManager.getScope(name);
                if (oAuthScope != null) {
                    scopeMap.put(oAuthScope.getName(), oAuthScope);
                }
            }
        }

        final OAuthScope defaultScope = getDefaultScope();
        if (defaultScope != null) {
            scopeMap.put(defaultScope.getName(), defaultScope);
        }

        return scopeMap;
    }

    public OAuthScope getDefaultScope() {
        return scopeManager.getDefault();
    }
}


