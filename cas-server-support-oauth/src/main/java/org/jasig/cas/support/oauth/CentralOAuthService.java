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
import org.jasig.cas.support.oauth.scope.InvalidScopeException;
import org.jasig.cas.support.oauth.scope.Scope;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.AuthorizationCode;
import org.jasig.cas.support.oauth.token.InvalidTokenException;
import org.jasig.cas.support.oauth.token.RefreshToken;
import org.jasig.cas.support.oauth.token.Token;
import org.jasig.cas.support.oauth.token.TokenType;
import org.jasig.cas.support.oauth.token.registry.TokenRegistry;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Central OAuth Service.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public interface CentralOAuthService {

    /**
     * Get a registered oauth service by client id.
     *
     * @param clientId the client id
     * @return a registered service
     */
    OAuthRegisteredService getRegisteredService(String clientId);

    /**
     * Grant an authorization code for followup by the server.
     *
     * @param type the token type
     * @param clientId the client id
     * @param ticketGrantingTicketId the associated ticket granting ticket id
     * @param callbackUrl the callback url
     * @param scopes the set of scopes
     * @return an authorization code to be passed back to the client
     * @throws TicketException the ticket exception
     */
    AuthorizationCode grantAuthorizationCode(TokenType type, String clientId, String ticketGrantingTicketId, String callbackUrl,
                                             Set<String> scopes) throws TicketException;

    /**
     * Grant an offline refresh token.
     *
     * @param authorizationCode the authorization code token
     * @param redirectUri the redirect uri
     * @return a refresh token to be passed back to the client request.
     * @throws InvalidTokenException the invalid token exception
     */
    RefreshToken grantOfflineRefreshToken(AuthorizationCode authorizationCode, String redirectUri) throws InvalidTokenException;

    /**
     * Grant CAS Access Token. Generates an access token associated with a Ticket Granting Ticket given to the CAS client.
     *
     * @param ticketGrantingTicket the ticket granting ticket
     * @param service the service
     * @return an access token to be integrated in the CAS service validation attributes.
     * @throws TicketException the ticket exception
     */
    AccessToken grantCASAccessToken(TicketGrantingTicket ticketGrantingTicket, Service service) throws TicketException;

    /**
     * Grant a Personal Access Token. Generates an access token associated with the personal access token.
     *
     * @param personalAccessToken the personal access token
     * @return an access token tied to the personal access token
     * @throws InvalidTokenException the invalid token exception
     */
    AccessToken grantPersonalAccessToken(PersonalAccessToken personalAccessToken) throws InvalidTokenException;

    /**
     * Grant an Offline Access Token.
     *
     * @param refreshToken a refresh token
     * @return a new access token based on the refresh token provided
     * @throws InvalidTokenException the invalid token exception
     */
    AccessToken grantOfflineAccessToken(RefreshToken refreshToken) throws InvalidTokenException;

    /**
     * Grant an Online Access Token.
     *
     * @param authorizationCode the authorization code
     * @return a new access token based on the authorization code provided
     * @throws InvalidTokenException the invalid token exception
     */
    AccessToken grantOnlineAccessToken(AuthorizationCode authorizationCode) throws InvalidTokenException;

    /**
     * Revoke a Token.
     *
     * @param token the token
     * @return a Boolean status if the token was successfully revoked.
     */
    Boolean revokeToken(Token token);

//    /**
//     * Revoke all Tokens associated with the specified client id, authorized by the client secret.
//     *
//     * Note: This method is deprecated. Please avoid implementing it or using its implementations.
//     *       The functionality is now performed by the `OAuth20RevokeClientTokensController`.
//     * TODO: Remove this method and its implementations
//     *
//     * @param clientId the client id
//     * @param clientSecret the client secret
//     * @return a Boolean status if tokens were successfully revoked.
//     */
//    Boolean revokeClientTokens(String clientId, String clientSecret);

    /**
     * Revoke all Tokens associated with the access token principal id and the client id specified.
     *
     * @param accessToken the access token
     * @param clientId the client id
     * @return a Boolean status if the tokens were successfully revoked.
     */
    Boolean revokeClientPrincipalTokens(AccessToken accessToken, String clientId);

    /**
     * Get metadata about the client id requested, authorized by the client secret.
     *
     * @param clientId the client id
     * @param clientSecret the client secret
     * @return metadata regarding the client id
     */
    ClientMetadata getClientMetadata(String clientId, String clientSecret);

    /**
     * Get metadata about the principal requested, authorized by the access token.
     *
     * @param accessToken the access token
     * @return metadata regarding the access token and its principal id.
     * @throws InvalidTokenException the invalid token exception
     */
    Collection<PrincipalMetadata> getPrincipalMetadata(AccessToken accessToken) throws InvalidTokenException;

    /**
     * Check if a refresh token exists for the client, principal and scopes specified.
     *
     * @param clientId the client id
     * @param principalId the principal id
     * @param scopes the set of scopes
     * @return a Boolean status if an active token was found.
     */
    Boolean isRefreshToken(String clientId, String principalId, Set<String> scopes);

    /**
     * Check if an access token exists for the token type, client, principal and scopes specified.
     *
     * @param type the token type
     * @param clientId the client id
     * @param principalId the principal id
     * @param scopes the set of scopes
     * @return a Boolean status if an active token was found.
     */
    Boolean isAccessToken(TokenType type, String clientId, String principalId, Set<String> scopes);

    /**
     * Get the token by the id specified.
     *
     * @param tokenId the token id
     * @return a token
     * @throws InvalidTokenException the invalid token exception
     */
    Token getToken(String tokenId) throws InvalidTokenException;

    /**
     * Get the token by the id and clazz specified.
     *
     * @param tokenId the token id
     * @param clazz The expected class of the token we wish to retrieve.
     * @param <T> the generic token type to return that extends {@link Token}
     * @return a token
     * @throws InvalidTokenException the invalid token exception
     */
    <T extends Token> T getToken(String tokenId, Class<T> clazz) throws InvalidTokenException;

    /**
     * Get the personal access token of the id specified.
     *
     * @param tokenId the token id
     * @return a personal access token
     */
    PersonalAccessToken getPersonalAccessToken(String tokenId);

    /**
     * Validates a list of scopes given and returns any additional default scopes required by the manager.
     *
     * @param scopeSet the set of scopes
     * @return a validated set of scopes
     * @throws InvalidScopeException the invalid scope exception
     */
    Map<String, Scope> getScopes(Set<String> scopeSet) throws InvalidScopeException;

    /**
     * @return the token registry.
     */
    TokenRegistry getTokenRegistry();
}
