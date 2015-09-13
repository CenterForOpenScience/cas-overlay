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

import org.jasig.cas.support.oauth.scope.handler.SimpleScopeHandler;
import org.jasig.cas.support.oauth.scope.handler.support.ScopeHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Scope Manager.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public class ScopeManager {

    /** The default scope handler. */
    private final ScopeHandler scopeHandler;

    /** The CAS scope handler. */
    private final ScopeHandler casScopeHandler;

    /**
     * Creates a new scope manager with only a default scope handler.
     *
     * @param scopeHandler The default scope handler.
     */
    public ScopeManager(final ScopeHandler scopeHandler) {
        this(scopeHandler, new SimpleScopeHandler());
    }

    /**
     * Creates a new scope manager with the addition of the cas scope handler.
     *
     * @param scopeHandler The default scope handler.
     * @param casScopeHandler The cas scope handler.
     */
    public ScopeManager(final ScopeHandler scopeHandler, final ScopeHandler casScopeHandler) {
        this.scopeHandler = scopeHandler;
        this.casScopeHandler = casScopeHandler;
    }

    /**
     * Retrieve a scope by name.
     *
     * @param name the name of the scope.
     * @return the retireved scope
     */
    public Scope getScope(final String name) {
        return scopeHandler.getScope(name);
    }

    /**
     * Retrieve a set of default scopes.
     *
     * @return the set of scopes.
     */
    public Set<Scope> getDefaults() {
        return scopeHandler.getDefaults();
    }

    /**
     * Retrieve a set of scopes specific to the CAS handler.
     *
     * @return the set of scopes.
     */
    public Set<String> getCASScopes() {
        final Set<String> scopeSet = new HashSet<>();
        for (final Scope defaultScope : this.casScopeHandler.getDefaults()) {
            scopeSet.add(defaultScope.getName());
        }
        return scopeSet;
    }
}
