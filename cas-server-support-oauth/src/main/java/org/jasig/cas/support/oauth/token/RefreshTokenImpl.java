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
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Set;

/**
 * Refresh Token Implementation.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
@Entity
@Table(name="REFRESHTOKEN")
@Access(AccessType.FIELD)
public final class RefreshTokenImpl extends AbstractToken implements RefreshToken {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -4808149803180911589L;

    /** The TicketGrantingTicket this is associated with. */
    @OneToOne(targetEntity=TicketGrantingTicketImpl.class, orphanRemoval=true)
    @OnDelete(action= OnDeleteAction.CASCADE)
    private TicketGrantingTicket ticketGrantingTicket;

    /** The service associated with the tgt. */
    @Column(name="SERVICE", nullable=false)
    private Service service;

    /**
     * Instantiates a new oauth refresh token impl.
     */
    public RefreshTokenImpl() {
        // nothing to do
    }

    /**
     * Constructs a new RefreshToken.
     *
     * @param id the id of the Ticket
     * @param clientId the client id
     * @param principalId the principal id
     * @param ticketGrantingTicket the ticket granting ticket
     * @param service the service
     * @param scopes the granted scopes
     */
    public RefreshTokenImpl(final String id, final String clientId, final String principalId,
                            final TicketGrantingTicket ticketGrantingTicket, final Service service,
                            final Set<String> scopes) {
        super(id, clientId, principalId, TokenType.OFFLINE, scopes);
        this.ticketGrantingTicket = ticketGrantingTicket;
        this.service = service;
    }

    @Override
    public Ticket getTicket() {
        return this.ticketGrantingTicket;
    }

    @Override
    public TicketGrantingTicket getTicketGrantingTicket() {
        return this.ticketGrantingTicket;
    }

    @Override
    public Service getService() {
        return this.service;
    }
}
