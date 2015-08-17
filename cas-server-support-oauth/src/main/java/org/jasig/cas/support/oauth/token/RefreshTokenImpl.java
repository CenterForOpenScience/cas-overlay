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
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Todo...
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
@Entity
@Table(name="REFRESHTOKEN")
public final class RefreshTokenImpl implements RefreshToken {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -4808149803180911589L;

    /** Logger instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshTokenImpl.class);

    /** The unique identifier for this ticket. */
    @Id
    @Column(name="ID", nullable=false)
    private String id;

    /** The client id associated with the token. */
    @Column(name="CLIENT_ID", nullable=false)
    private String clientId;

    /** The principal id associated with the token. */
    @Column(name="PRINCIPAL_ID", nullable=false)
    private String principalId;

    /** The client id associated with the token. */
    @Column(name="REDIRECT_URI", nullable=false)
    private String redirectUri;

    /** The TicketGrantingTicket this is associated with. */
    @OneToOne(targetEntity=TicketGrantingTicketImpl.class)
    @OnDelete(action=OnDeleteAction.CASCADE)
    private TicketGrantingTicket ticketGrantingTicket;

    @Lob
    @Column(name="SCOPE", nullable=false, length = 1000000)
    private ArrayList<String> scope;

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
     * @param ticketGrantingTicket the ticket granting ticket
     * @param clientId the client id
     * @param redirectUri the redirect uri
     * @param scope the scope
     */
    public RefreshTokenImpl(final String id,
                            final TicketGrantingTicket ticketGrantingTicket,
                            final String clientId,
                            final String redirectUri,
                            final Set<String> scope) {
        this.id = id;
        this.ticketGrantingTicket = ticketGrantingTicket;
        this.principalId = ticketGrantingTicket.getAuthentication().getPrincipal().getId();
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.scope = new ArrayList<>(scope);
    }

    public final String getId() {
        return this.id;
    }

    public final TicketGrantingTicket getTicketGrantingTicket() {
        return this.ticketGrantingTicket;
    }

    public final String getClientId() {
        return this.clientId;
    }

    public final String getPrincipalId() {
        return this.principalId;
    }

    public final String getRedirectUri() {
        return this.redirectUri;
    }

    public final Set<String> getScope() {
        return new HashSet<>(this.scope);
    }
}
