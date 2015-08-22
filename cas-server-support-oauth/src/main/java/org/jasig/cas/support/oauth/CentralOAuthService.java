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

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.oauth.metadata.ClientMetadata;
import org.jasig.cas.support.oauth.metadata.PrincipalMetadata;
import org.jasig.cas.support.oauth.personal.PersonalAccessToken;
import org.jasig.cas.support.oauth.scope.Scope;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.token.*;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Central OAuth Service
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public interface CentralOAuthService {

    OAuthRegisteredService getRegisteredService(String clientId);

    AuthorizationCode grantAuthorizationCode(TokenType type, String clientId, String ticketGrantingTicketId, String callbackUrl, Set<String> scopes) throws TicketException;

    RefreshToken grantOfflineRefreshToken(AuthorizationCode authorizationCode, String redirectUri) throws TicketException;

    AccessToken grantCASAccessToken(TicketGrantingTicket ticketGrantingTicket, Service service) throws TicketException;

    AccessToken grantPersonalAccessToken(PersonalAccessToken personalAccessToken) throws TicketException;

    AccessToken grantOfflineAccessToken(RefreshToken refreshToken) throws TicketException;

    AccessToken grantOnlineAccessToken(AuthorizationCode authorizationCode) throws TicketException;

    Boolean revokeToken(Token token);

    Boolean revokeClientTokens(String clientId, String clientSecret);

    Boolean revokeClientPrincipalTokens(AccessToken accessToken);

    ClientMetadata getClientMetadata(String clientId, String clientSecret);

    Collection<PrincipalMetadata> getPrincipalMetadata(AccessToken accessToken);

    Boolean isRefreshToken(String clientId, String principalId, Set<String> scopes);

    Boolean isAccessToken(TokenType type, String clientId, String principalId, Set<String> scopes);

    Token getToken(String tokenId) throws InvalidTicketException;

    <T extends Token> T getToken(String tokenId, Class<T> clazz) throws InvalidTicketException;

    PersonalAccessToken getPersonalAccessToken(String tokenId);

    Map<String, Scope> getScopes(Set<String> scopeSet);
}
