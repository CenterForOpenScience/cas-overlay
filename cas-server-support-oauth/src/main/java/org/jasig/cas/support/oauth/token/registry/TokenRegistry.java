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

import org.jasig.cas.support.oauth.token.RefreshToken;
import org.jasig.cas.support.oauth.token.Token;

/**
 * todo...
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
    <T extends Token> T getToken(String tokenId, Class<? extends Token> clazz) throws ClassCastException;

    RefreshToken getRefreshToken(String clientId, String principalId);


//    /**
//     * Retrieve a ticket from the registry.
//     *
//     * @param ticketId the id of the ticket we wish to retrieve
//     * @return the requested ticket.
//     */
//    Ticket getTicket(String ticketId);

//    /**
//     * Remove a specific ticket from the registry.
//     *
//     * @param tokenId The id of the token to delete.
//     * @return true if the ticket was removed and false if the ticket did not
//     * exist.
//     */
//    boolean deleteToken(String tokenId);

//    /**
//     * Retrieve all tickets from the registry.
//     *
//     * @return collection of tickets currently stored in the registry. Tickets
//     * might or might not be valid i.e. expired.
//     */
//    Collection<Ticket> getTickets();
}
