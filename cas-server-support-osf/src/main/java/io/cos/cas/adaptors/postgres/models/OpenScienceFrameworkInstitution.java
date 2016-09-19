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
import javax.persistence.Table;

/**
 * The Open Science Framework Institution
 *
 * @author Longze Chen
 * @since 4.1.o
 */
@Entity
@Table(name = "osf_models_institution")
public class OpenScienceFrameworkInstitution {

    private static final Logger logger = LoggerFactory.getLogger(OpenScienceFrameworkInstitution.class);

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "login_url")
    private String loginUrl;

    @Column(name = "logout_url")
    private String logoutUrl;

    /** Default Constructor */
    public OpenScienceFrameworkInstitution() {}

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    @Override
    public String toString() {
        return String.format("OpenScienceFrameworkInstitution [providerId=%s, name=%s]", providerId, name);
    }

}
