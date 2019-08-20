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
import javax.persistence.Table;

/**
 * The Open Science Framework GUID.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
@Entity
@Table(name = "django_content_type")
public class OpenScienceFrameworkDjangoContentTypeId {

    /** The Primary Key. */
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    /** The Application Label. */
    @Column(name = "app_label", nullable = false)
    private String appLabel;

    /** The Model Name. */
    @Column(name = "model", nullable = false)
    private String model;

    /** Default Constructor. */
    public OpenScienceFrameworkDjangoContentTypeId() {}

    public Integer getId() {
        return id;
    }

    public String getAppLabel() {
        return appLabel;
    }

    public String getModel() {
        return model;
    }

    @Override
    public String toString() {
        return String.format("%s_%s", appLabel, model);
    }
}

