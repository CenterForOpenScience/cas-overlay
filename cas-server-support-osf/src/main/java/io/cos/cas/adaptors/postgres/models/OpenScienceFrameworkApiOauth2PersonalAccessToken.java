/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

package io.cos.cas.adaptors.postgres.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * The Open Science Framework API OAuth2 Personal Access Token.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
@Entity
@Table(name = "osf_apioauth2personaltoken")
public class OpenScienceFrameworkApiOauth2PersonalAccessToken {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "token_id", nullable = false)
    private String tokenId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @OneToOne
    @JoinColumn(name = "owner_id")
    private OpenScienceFrameworkUser owner;

    /** Default Constructor.*/
    public OpenScienceFrameworkApiOauth2PersonalAccessToken() {}

    public Integer getId() {
        return id;
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getName() {
        return name;
    }

    public Boolean isActive() {
        return isActive;
    }

    public OpenScienceFrameworkUser getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return String.format("PersonalAccessToken [id=%s, owner=%s]", id, owner.getUsername());
    }
}
