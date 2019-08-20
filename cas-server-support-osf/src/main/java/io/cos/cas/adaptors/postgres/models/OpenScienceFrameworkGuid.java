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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * The Open Science Framework GUID.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
@Entity
@Table(name = "osf_guid")
public class OpenScienceFrameworkGuid {

    /** The Primary Key. */
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    /** The GUID of the Object. */
    @Column(name = "_id", nullable = false)
    private String guid;

    /** The Primary Key of the Object. */
    @Column(name = "object_id", nullable = false)
    private Integer objectId;

    /** The Content Type of the Object. */
    @OneToOne
    @JoinColumn(name = "content_type_id")
    private OpenScienceFrameworkDjangoContentTypeId djangoContentType;

    /** The Date Created. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created", nullable = false)
    private Date created;

    /** The Default Constructor. */
    public OpenScienceFrameworkGuid() {}

    public Integer getId() {
        return id;
    }

    public String getGuid() {
        return guid;
    }

    public Integer getObjectId() {
        return objectId;
    }

    public OpenScienceFrameworkDjangoContentTypeId getDjangoContentType() {
        return djangoContentType;
    }

    @Override
    public String toString() {
        return String.format(
            "OpenScienceFrameworkGuid [guid=%s, objectId=%d, djangoContentTypeId=%s]",
            guid,
            objectId,
            djangoContentType.getId()
        );
    }
}
