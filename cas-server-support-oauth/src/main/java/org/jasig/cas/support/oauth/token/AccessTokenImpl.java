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
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Set;

/**
 * Access Token Implementation class.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
@Entity
@Table(name="ACCESSTOKEN")
public final class AccessTokenImpl extends AbstractToken implements AccessToken {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -2608145809180961597L;

    /** The TicketGrantingTicket this is associated with. */
    @OneToOne(targetEntity=TicketGrantingTicketImpl.class, orphanRemoval=true)
    @OnDelete(action= OnDeleteAction.CASCADE)
    private TicketGrantingTicket ticketGrantingTicket;

    /** The service associated with the tgt. */
    @Lob
    @Column(name="SERVICE")
    private Service service;

    /** The ServiceTicket this is associated with. */
    @OneToOne(targetEntity=ServiceTicketImpl.class, orphanRemoval=true)
    @OnDelete(action= OnDeleteAction.CASCADE)
    private ServiceTicket serviceTicket;

    /**
     * Instantiates a new access token impl.
     */
    public AccessTokenImpl() {
        // nothing to do
    }

    /**
     * Constructs a new AccessToken.
     *
     * @param id the id of the Ticket
     * @param type the token type
     * @param clientId the client id
     * @param principalId the principal id
     * @param ticketGrantingTicket the ticket granting ticket
     * @param service the service
     * @param serviceTicket the service ticket
     * @param scopes the granted scopes
     */
    public AccessTokenImpl(final String id, final TokenType type, final String clientId, final String principalId,
                           final TicketGrantingTicket ticketGrantingTicket, final Service service,
                           final ServiceTicket serviceTicket, final Set<String> scopes) {
        super(id, clientId, principalId, type, scopes);

        this.ticketGrantingTicket = ticketGrantingTicket;
        this.service = service;
        this.serviceTicket = serviceTicket;
    }

    @Override
    @Transient
    public Ticket getTicket() {
        if (getType() == TokenType.OFFLINE) {
            return this.serviceTicket;
        }

        return this.ticketGrantingTicket;
    }

    @Override
    public TicketGrantingTicket getTicketGrantingTicket() {
        if (getType() == TokenType.OFFLINE) {
            return this.serviceTicket.getGrantingTicket();
        }

        return this.ticketGrantingTicket;
    }

    @Override
    public Service getService() {
        return this.service;
    }

    @Override
    public ServiceTicket getServiceTicket() {
        return this.serviceTicket;
    }
}
