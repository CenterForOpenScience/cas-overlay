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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * The Open Science Framework User.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.0
 */
@Entity
@Table(name = "osf_models_osfuser")
public final class OpenScienceFrameworkUser {

    /** The logger instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkUser.class);

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "verification_key")
    private String verificationKey;

    @OneToOne
    @JoinColumn(name = "merged_by_id")
    private OpenScienceFrameworkUser mergedBy;

    @Column(name = "given_name", nullable = false)
    private String givenName;

    @Column(name = "family_name", nullable = false)
    private String familyName;

    @Column(name = "is_registered", nullable = false)
    private Boolean registered;

    @Column(name = "is_claimed", nullable = false)
    private Boolean claimed;

    // TODO: handle postgres `timestamp with time zone`
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_confirmed")
    private Date dateConfirmed;

    // TODO: handle postgres `timestamp with time zone`
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_disabled")
    private Date dateDisabled;

    /** Default Constructor. */
    public OpenScienceFrameworkUser() {}

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getVerificationKey() {
        return verificationKey;
    }

    public OpenScienceFrameworkUser getMergedBy() {
        return mergedBy;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public Boolean isRegistered() {
        return registered;
    }

    public Boolean isClaimed() {
        return claimed;
    }

    public Boolean isMerged() {
        return mergedBy != null;
    }

    public Boolean isConfirmed() {
        return dateConfirmed != null;
    }

    public Boolean isDisabled() {
        return dateDisabled != null;
    }

    public Boolean isActive() {
        return isRegistered() && !isMerged() && !isDisabled() && isConfirmed();
    }

    @Override
    public String toString() {
        return String.format("OpenScienceFrameworkUser [id=%s, username=%s]", id, username);
    }
}
