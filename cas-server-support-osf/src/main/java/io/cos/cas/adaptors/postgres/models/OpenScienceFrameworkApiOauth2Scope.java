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

import javax.persistence.*;

/**
 * The Open Science Framework API Oauth2 Scope.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.0
 */
@Entity
@Table(name = "osf_models_apioauth2scope")
public class OpenScienceFrameworkApiOauth2Scope {

    private static final Logger logger = LoggerFactory.getLogger(OpenScienceFrameworkApiOauth2Scope.class);

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /** Default Constructor */
    public OpenScienceFrameworkApiOauth2Scope() {}


    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Boolean isActive() {
        return isActive;
    }

    @Override
    public String toString() {
        return String.format("OpenScienceFrameworkScope [id=%s, name=%s]", this.id, this.name);
    }
}
