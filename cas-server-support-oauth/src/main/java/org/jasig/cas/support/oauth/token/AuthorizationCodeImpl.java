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
package org.jasig.cas.support.oauth.token;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.Ticket;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Set;

/**
 * Authorization Code token implementation.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
@Entity
@Table(name="AUTHORIZATIONCODE")
public final class AuthorizationCodeImpl extends AbstractToken implements AuthorizationCode {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -7608149809180111599L;

    /** The ServiceTicket this is associated with. */
    @OneToOne(targetEntity=ServiceTicketImpl.class)
    @OnDelete(action=OnDeleteAction.CASCADE)
    private ServiceTicket serviceTicket;

    /**
     * Instantiates a new oauth refresh token impl.
     */
    public AuthorizationCodeImpl() {
        // nothing to do
    }

    /**
     * Constructs a new Code Token.
     * May throw an {@link IllegalArgumentException} if the Authentication object is null.
     *
     * @param id the id of the Ticket
     * @param serviceTicket the service ticket
     * @param scopes the scopes
     */
    public AuthorizationCodeImpl(final String id, final TokenType type, final String clientId, final String principalId,
                                 final ServiceTicket serviceTicket, final Set<String> scopes) {
        super(id, clientId, principalId, type, scopes);
        this.serviceTicket = serviceTicket;
    }

    @Override
    public Ticket getTicket() {
        return this.serviceTicket;
    }

    @Override
    public final ServiceTicket getServiceTicket() {
        return this.serviceTicket;
    }
}
