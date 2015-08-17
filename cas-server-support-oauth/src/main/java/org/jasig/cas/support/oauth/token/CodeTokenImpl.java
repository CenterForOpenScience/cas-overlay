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
import javax.persistence.Entity;
import javax.persistence.Table;
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
@Table(name="CODETOKEN")
public final class CodeTokenImpl implements CodeToken {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -7608149809180111599L;

    /** Logger instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeTokenImpl.class);

    /** The unique identifier for this ticket. */
    @Id
    @Column(name="ID", nullable=false)
    private String id;

    /** The ServiceTicket this is associated with. */
    @OneToOne(targetEntity=ServiceTicketImpl.class)
    @OnDelete(action=OnDeleteAction.CASCADE)
    private ServiceTicket serviceTicket;

    /** The client id associated with the token. */
    @Column(name="CLIENT_ID", nullable=false)
    private String clientId;

    /** The callback url. */
    @Column(name="CALLBACK_URL", nullable=false)
    private String callbackUrl;

    @Lob
    @Column(name="SCOPE", nullable=false, length = 1000000)
    private ArrayList<String> scope;

    /**
     * Instantiates a new oauth refresh token impl.
     */
    public CodeTokenImpl() {
        // nothing to do
    }

    /**
     * Constructs a new Code Token.
     * May throw an {@link IllegalArgumentException} if the Authentication object is null.
     *
     * @param id the id of the Ticket
     * @param serviceTicket the service ticket
     * @param scope the scope
     */
    public CodeTokenImpl(final String id,
                         final ServiceTicket serviceTicket,
                         final String clientId,
                         final String callbackUrl,
                         final Set<String> scope) {
        this.id = id;
        this.serviceTicket = serviceTicket;
        this.clientId = clientId;
        this.callbackUrl = callbackUrl;
        this.scope = new ArrayList<>(scope);
    }

    public final String getId() {
        return this.id;
    }

    public final ServiceTicket getServiceTicket() {
        return this.serviceTicket;
    }

    public final String getClientId() {
        return this.clientId;
    }

    public final String getCallbackUrl() {
        return this.callbackUrl;
    }

    public final Set<String> getScope() {
        return new HashSet<>(this.scope);
    }
}
