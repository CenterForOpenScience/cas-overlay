/*
 * Copyright (c) 2015. Center for Open Science
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jasig.cas.support.oauth.token.registry;

import org.jasig.cas.support.oauth.token.Token;
import org.jasig.cas.support.oauth.token.TokenType;

import java.util.Collection;
import java.util.Set;

/**
 * The interface defines what basic functionality a token registry should provide.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public interface TokenRegistry {

    /**
     * Add a token to the registry.
     *
     * Token storage is based on the token id.
     *
     * @param token the token we wish to add to the cache
     */
    void addToken(Token token);

    /**
     * Update a token in the registry.
     *
     * @param token the token we wish to update in the cache
     */
    void updateToken(Token token);

    /**
     * Retrieve a token from the registry.
     *
     * @param tokenId the id of the token we wish to retrieve
     * @param clazz the expected class of the token we wish to retrieve
     * @param <T> the generic token type to return that extends {@link Token}
     * @return the requested token
     * @throws ClassCastException the class cast exception
     */
    <T extends Token> T getToken(String tokenId, Class<T> clazz) throws ClassCastException;

    /**
     * Retrieve a collection of tokens associated with the client id specified.
     *
     * @param clientId the client id of the tokens we wish to retrieve
     * @param clazz the expected class of the token we wish to retrieve
     * @param <T> the generic token type to return that extends {@link Token}
     * @return a collection of requested tokens
     * @throws ClassCastException the class cast exception
     */
    <T extends Token> Collection<T> getClientTokens(String clientId, Class<T> clazz) throws ClassCastException;

    /**
     * Retrieve a collection of tokens associated with the client id and the principal id specified.
     *
     * @param clientId the client id of the tokens we wish to retrieve
     * @param principalId the principal id of the tokens we wish to retrieve
     * @param clazz the expected class of the token we wish to retrieve
     * @param <T> the generic token type to return that extends {@link Token}
     * @return a collection of requested tokens
     * @throws ClassCastException the class cast exception
     */
    <T extends Token> Collection<T> getClientPrincipalTokens(
            String clientId,
            String principalId,
            Class<T> clazz
    ) throws ClassCastException;

    /**
     * Retrieve a collection of tokens associated with the client id, principal id and token type specified.
     *
     * @param clientId the client id of the tokens we wish to retrieve
     * @param principalId the principal id of the tokens we wish to retrieve
     * @param type the token type of the tokens we wish to retrieve
     * @param clazz the expected class of the token we wish to retrieve
     * @param <T> the generic token type to return that extends {@link Token}
     * @return a collection of requested tokens
     * @throws ClassCastException the class cast exception
     */
    <T extends Token> Collection<T> getClientPrincipalTokens(
            String clientId,
            String principalId,
            TokenType type,
            Class<T> clazz
    ) throws ClassCastException;

    /**
     * Retrieve a collection of tokens associated with the principal id specified.
     *
     * @param principalId the principal id of the tokens we wish to retrieve
     * @param clazz the expected class of the token we wish to retrieve
     * @param <T> the generic token type to return that extends {@link Token}
     * @return a collection of requested tokens
     * @throws ClassCastException the class cast exception
     */
    <T extends Token> Collection<T> getPrincipalTokens(String principalId, Class<T> clazz) throws ClassCastException;

    /**
     * Check if a token exists by client id, principal id and assigned scopes.
     *
     * @param clientId the client id of the token we wish to find
     * @param principalId the principal id of the token we wish to find
     * @param scopes the scopes associated with the token we wish to find
     * @param clazz the expected class of the token we wish to find
     * @param <T> the generic token type to return that extends {@link Token}
     * @return indicates if the token could be found
     * @throws ClassCastException the class cast exception
     */
    <T extends Token> Boolean isToken(
            String clientId,
            String principalId,
            Set<String> scopes,
            Class<T> clazz
    ) throws ClassCastException;

    /**
     * Check if a token exists by token type, client id, principal id and assigned scopes.
     *
     * @param type the token type of the token we wish to find
     * @param clientId the client id of the token we wish to find
     * @param principalId the principal id of the token we wish to find
     * @param scopes the scopes associated with the token we wish to find
     * @param clazz the expected class of the token we wish to find
     * @param <T> the generic token type to return that extends {@link Token}
     * @return indicates if the token could be found
     * @throws ClassCastException the class cast exception
     */
    <T extends Token> Boolean isToken(
            TokenType type,
            String clientId,
            String principalId,
            Set<String> scopes,
            Class<T> clazz
    ) throws ClassCastException;

    /**
     * Count the number unique principal's assigned to a client token id.
     *
     * @param clientId the client id of the tokens we wish to find
     * @return a count of the number of unique principals
     */
    Integer getPrincipalCount(String clientId);
}
