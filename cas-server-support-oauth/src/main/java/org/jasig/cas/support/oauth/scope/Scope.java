/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.jasig.cas.support.oauth.scope;

/**
 * An OAuth Scope.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public final class Scope {

    /** The scope name. */
    private final String name;

    /** The scope description. */
    private final String description;

    /** The scope default status. */
    private final Boolean isDefault;

    /**
     * Creates a new scope.
     *
     * @param name the scope name..
     * @param description the scope description.
     */
    public Scope(final String name, final String description) {
        this(name, description, Boolean.FALSE);
    }

    /**
     * Creates a new scope.
     *
     * @param name the scope name..
     * @param description the scope description.
     * @param isDefault the scope default status.
     */
    public Scope(final String name, final String description, final Boolean isDefault) {
        this.name = name;
        this.description = description;
        this.isDefault = isDefault;
    }

    /**
     * Get the name of the scope.
     *
     * @return the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the description of the scope.
     *
     * @return the description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Get the default status of the scope.
     *
     * @return the default status.
     */
    public Boolean getIsDefault() {
        return this.isDefault;
    }
}
