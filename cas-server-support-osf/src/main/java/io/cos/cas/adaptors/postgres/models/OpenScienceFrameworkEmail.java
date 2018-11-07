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
 * The Open Science Framework Email.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
@Entity
@Table(name = "osf_email")
public class OpenScienceFrameworkEmail {

    /** The Primary Key. */
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    /** The Email Address. */
    @Column(name = "address", nullable = false)
    private String address;

    /** The Owner of The Email Address. */
    @OneToOne
    @JoinColumn(name = "user_id")
    private OpenScienceFrameworkUser user;

    /** Default Constructor. */
    public OpenScienceFrameworkEmail() {}

    public Integer getId() {
        return id;
    }

    public String getName() {
        return address;
    }

    public OpenScienceFrameworkUser getUser() {
        return user;
    }

    @Override
    public String toString() {
        return String.format("%s [id=%d, email=%s, user=%s]", this.getClass().getSimpleName(), id, address, user.getUsername());
    }
}
