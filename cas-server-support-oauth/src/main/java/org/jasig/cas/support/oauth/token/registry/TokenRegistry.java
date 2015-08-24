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
package org.jasig.cas.support.oauth.token.registry;

import java.util.Collection;
import java.util.Set;

import org.jasig.cas.support.oauth.token.Token;
import org.jasig.cas.support.oauth.token.TokenType;

/**
 * Token Registry interface.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public interface TokenRegistry {

    /**
     * Add a ticket to the registry. Ticket storage is based on the ticket id.
     *
     * @param token The ticket we wish to add to the cache.
     */
    void addToken(Token token);

    void updateToken(Token token);

    /**
     * Retrieve a token from the registry. If the token retrieved does not
     * match the expected class, an InvalidTicketException is thrown.
     *
     * @param tokenId the id of the token we wish to retrieve.
     * @param clazz The expected class of the ticket we wish to retrieve.
     * @param <T> the generic token type to return that extends {@link Token}
     * @return the requested token.
     */
    <T extends Token> T getToken(String tokenId, Class<T> clazz) throws ClassCastException;

    <T extends Token> Collection<T> getClientTokens(String clientId, Class<T> clazz) throws ClassCastException;

    <T extends Token> Collection<T> getClientPrincipalTokens(String clientId, String principalId, Class<T> clazz) throws ClassCastException;

    <T extends Token> Collection<T> getClientPrincipalTokens(String clientId, String principalId, TokenType type, Class<T> clazz) throws ClassCastException;

    <T extends Token> Collection<T> getPrincipalTokens(String principalId, Class<T> clazz) throws ClassCastException;

    <T extends Token> Boolean isToken(String clientId, String principalId, Set<String> scopes, Class<T> clazz);

    <T extends Token> Boolean isToken(TokenType type, String clientId, String principalId, Set<String> scopes, Class<T> clazz);

    Integer getPrincipalCount(String clientId);
}
