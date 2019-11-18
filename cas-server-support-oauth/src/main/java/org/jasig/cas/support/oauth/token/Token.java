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
package org.jasig.cas.support.oauth.token;

import org.jasig.cas.ticket.Ticket;

import java.io.Serializable;
import java.util.Set;

/**
 * The interface for the generic concept of a token.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public interface Token extends Serializable {

    /**
     * Method to retrieve the id of the token.
     *
     * @return the id
     */
    String getId();

    /**
     * Method to retrieve the client id.
     *
     * @return the client id
     */
    String getClientId();

    /**
     * Method to retrieve the principal id.
     *
     * @return the principal id
     */
    String getPrincipalId();

    /**
     * Method to retrieve the ticket.
     *
     * @return the ticket
     */
    Ticket getTicket();

    /**
     * Method to retrieve the token type.
     *
     * @return the token type
     */
    TokenType getType();

    /**
     * Method to retrieve the scopes.
     *
     * @return the scopes
     */
    Set<String> getScopes();

    /**
     * Method to retrieve the computed hash of the assigned scopes.
     *
     * @return the hash
     */
    Integer getScopesHash();
}
