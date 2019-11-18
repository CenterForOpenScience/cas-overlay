/*
 * Copyright (c) 2018. Center for Open Science
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
 * The M2M Relationship between OSF API OAuth2 "Personal Access Token" and "Scope".
 *
 * @author Longze Chen
 * @since 4.1.5
 */
@Entity
@Table(name = "osf_apioauth2personaltoken_scopes")
public class OpenScienceFrameworkApiOauth2TokenScope {

    /** The Primary Key. */
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    /** The Primary Key of the Personal Access Token Object. */
    @Column(name = "apioauth2personaltoken_id", nullable = false)
    private Integer tokenPk;

    /** The Primary Key of the Scope Object. */
    @Column(name = "apioauth2scope_id", nullable = false)
    private Integer scopePk;

    /** The Default Constructor. */
    public OpenScienceFrameworkApiOauth2TokenScope() {}

    public Integer getId() {
        return id;
    }

    public Integer getTokenPk() {
        return tokenPk;
    }

    public Integer getScopePk() {
        return scopePk;
    }

    @Override
    public String toString() {
        return String.format("OpenScienceFrameworkApiOauth2TokenScope [tokenPk=%s, scopePk=%d, ]", tokenPk, scopePk);
    }
}
