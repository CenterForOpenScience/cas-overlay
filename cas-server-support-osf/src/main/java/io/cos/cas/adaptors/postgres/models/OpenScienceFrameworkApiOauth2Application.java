/*
 * Copyright (c) 2016. Center for Open Science
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
package io.cos.cas.adaptors.postgres.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The OpenScience Framework API OAuth2 Application.
 *
 * @author Micael Haselton
 * @author Longze Chen
 * @since 4.1.0
 */
@Entity
@Table(name = "osf_apioauth2application")
public class OpenScienceFrameworkApiOauth2Application {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * The `objectId` serves as a unique and fixed identifier for each oauth application.
     * It comes from `apioauth2application._id` (mongo) to `apioauth2application._id` (postgres).
     * Methods such as `getId()` refer to this `objectId`.
     */
    @Column(name ="_id", nullable = false, unique=true)
    private String objectId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "client_id", nullable = false, unique = true)
    private String clientId;

    @Column(name = "client_secret", nullable = false)
    private String clientSecret;

    @Column(name = "callback_url", nullable = false)
    private String callbackUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /**
     * Returns the `objectId` instead of `id` (postgres pk).
     * @return the object id
     */
    public String getId() {
        return objectId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    @Override
    public String toString() {
        return String.format("OpenScienceFrameworkApiOauth2Application [_id=%s, name=%s]", objectId, name);
    }
}
