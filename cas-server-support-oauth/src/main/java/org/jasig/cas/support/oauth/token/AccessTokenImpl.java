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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

/**
 * Todo...
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
@Entity
@Table(name="ACCESSTOKEN")
public final class AccessTokenImpl implements AccessToken {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -2608145809180961597L;

    /** Logger instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenImpl.class);

    /** The unique identifier for this ticket. */
    @Id
    @Column(name="ID", nullable=false)
    private String id;

    /** The RefreshToken this is associated with. */
    @OneToOne(targetEntity=RefreshTokenImpl.class)
    private RefreshToken refreshToken;

    /** The ServiceTicket this is associated with. */
    @OneToOne(targetEntity=ServiceTicketImpl.class)
    @OnDelete(action=OnDeleteAction.CASCADE)
    private ServiceTicket serviceTicket;

    /**
     * Instantiates a new oauth refresh token impl.
     */
    public AccessTokenImpl() {
        // nothing to do
    }

    /**
     * Constructs a new AccessToken.
     *
     * @param id the id of the Ticket
     * @param serviceTicket the service ticket
     */
    public AccessTokenImpl(final String id,
                           final ServiceTicket serviceTicket) {
        this(id, null, serviceTicket);
    }

    /**
     * Constructs a new AccessToken.
     *
     * @param id the id of the Ticket
     * @param refreshToken the refresh token
     * @param serviceTicket the service ticket
     */
    public AccessTokenImpl(final String id,
                           final RefreshToken refreshToken,
                           final ServiceTicket serviceTicket) {
        this.id = id;
        this.refreshToken = refreshToken;
        this.serviceTicket = serviceTicket;
    }

    public final String getId() {
        return this.id;
    }

    public final ServiceTicket getServiceTicket() {
        return this.serviceTicket;
    }

    public final RefreshToken getRefreshToken() {
        return this.refreshToken;
    }
}
