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

import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import java.util.HashSet;
import java.util.Set;

/**
 * The abstract base class for token classes.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
@MappedSuperclass
public abstract class AbstractToken implements Token {

    /** Unique id for serialization. */
    private static final long serialVersionUID = -5608324980180191592L;

    /** The unique id for this token. */
    @Id
    @Column(name="ID", nullable=false)
    private String id;

    /** The client id associated with the token. */
    @Column(name="CLIENT_ID")
    private String clientId;

    /** The principal id associated with the token. */
    @Column(name="PRINCIPAL_ID", nullable=false)
    private String principalId;

    /** The type associated with the token. */
    @Column(name="TYPE", nullable=false)
    private TokenType type;

    @Lob
    @Column(name="SCOPES", nullable=false, length = 1000000)
    private final HashSet<String> scopes = new HashSet<>();

    @Column(name="SCOPES_HASH", nullable=false)
    private Integer scopesHash;

    /** Default constructor. */
    protected AbstractToken(){}

    /**
     * Instantiate a new {@link AbstractToken}.
     *
     * @param id the unique id of the token
     * @param clientId the client id
     * @param principalId the principal id
     * @param type the token type
     * @param scopes the token scopes
     */
    public AbstractToken(
            final String id,
            final String clientId,
            final String principalId,
            final TokenType type,
            final Set<String> scopes
    ) {
        Assert.notNull(id, "Token ID cannot be null");
        Assert.notNull(principalId, "Principal ID cannot be null");
        Assert.notNull(type, "Token type cannot be null");
        Assert.notNull(scopes, "Token scopes cannot be null");
        this.id = id;
        this.clientId = clientId;
        this.principalId = principalId;
        this.type = type;
        this.scopes.addAll(scopes);
    }

    /** Compute the hash of all scopes upon saving the token. */
    @PreUpdate
    @PrePersist
    private void updateScopesHash() {
        this.scopesHash = this.scopes.hashCode();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getClientId() {
        return this.clientId;
    }

    @Override
    public String getPrincipalId() {
        return this.principalId;
    }

    @Override
    public TokenType getType() {
        return this.type;
    }

    @Override
    public Set<String> getScopes() {
        return this.scopes;
    }

    @Override
    public Integer getScopesHash() {
        return this.scopesHash;
    }
}
