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

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.Ticket;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import java.util.Set;

/**
 * The implementation class for {@link AuthorizationCode}.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
@Entity
@Table(name="AUTHORIZATIONCODE")
public final class AuthorizationCodeImpl extends AbstractToken implements AuthorizationCode {

    /** Unique id for serialization. */
    private static final long serialVersionUID = -7608149809180111599L;

    /** The service ticket this authorization code is associated with. */
    @OneToOne(targetEntity=ServiceTicketImpl.class, orphanRemoval=true)
    @OnDelete(action= OnDeleteAction.CASCADE)
    private ServiceTicket serviceTicket;

    /** Default constructor. */
    public AuthorizationCodeImpl(){}

    /**
     * Instantiate a new {@link AuthorizationCodeImpl}.
     *
     * @param id the id of the authorization code
     * @param type the token type
     * @param clientId the client id
     * @param principalId the principal id
     * @param serviceTicket the service ticket
     * @param scopes the scopes
     */
    public AuthorizationCodeImpl(
            final String id,
            final TokenType type,
            final String clientId,
            final String principalId,
            final ServiceTicket serviceTicket,
            final Set<String> scopes
    ) {
        super(id, clientId, principalId, type, scopes);
        this.serviceTicket = serviceTicket;
    }

    @Override
    public Ticket getTicket() {
        return this.serviceTicket;
    }

    @Override
    public ServiceTicket getServiceTicket() {
        return this.serviceTicket;
    }
}
