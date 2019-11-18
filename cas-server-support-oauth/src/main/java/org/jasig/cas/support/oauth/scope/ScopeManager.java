/*
 * Copyright (c) 2015. Center for Open Science
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
package org.jasig.cas.support.oauth.scope;

import org.jasig.cas.support.oauth.scope.handler.SimpleScopeHandler;
import org.jasig.cas.support.oauth.scope.handler.support.ScopeHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * OAuth scope manager.
 *
 * This manager is a property of the CAS OAuth Service {@link org.jasig.cas.support.oauth.CentralOAuthServiceImpl} and
 * is used to retrieve scope information via its two scope handlers.
 *
 * The OSF scope handler {@literal io.cos.cas.adaptors.postgres.handlers.OpenScienceFrameworkPersonalAccessTokenHandler}
 * is used as the main scope handler with current CAS settings.
 *
 * The simple scope handler {@link org.jasig.cas.support.oauth.scope.handler.SimpleScopeHandler} is used as the CAS
 * scope handler by by default.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public class ScopeManager {

    /** The default scope handler. */
    private final ScopeHandler scopeHandler;

    /** The CAS scope handler. */
    private final ScopeHandler casScopeHandler;

    /**
     * Creates a new scope manager with only a default scope handler.
     *
     * @param scopeHandler The default scope handler
     */
    public ScopeManager(final ScopeHandler scopeHandler) {
        this(scopeHandler, new SimpleScopeHandler());
    }

    /**
     * Creates a new scope manager with a scope handler and an additional cas scope handler.
     *
     * @param scopeHandler The default scope handler
     * @param casScopeHandler The cas scope handler
     */
    public ScopeManager(final ScopeHandler scopeHandler, final ScopeHandler casScopeHandler) {
        this.scopeHandler = scopeHandler;
        this.casScopeHandler = casScopeHandler;
    }

    /**
     * Retrieve a scope by name.
     *
     * @param name the name of the scope
     * @return the retrieved scope
     */
    public Scope getScope(final String name) {
        return scopeHandler.getScope(name);
    }

    /**
     * Retrieve a set of default scopes.
     *
     * @return the set of scopes
     */
    public Set<Scope> getDefaults() {
        return scopeHandler.getDefaults();
    }

    /**
     * Retrieve a set of scopes specific to the CAS handler.
     *
     * @return the set of scopes
     */
    public Set<String> getCASScopes() {
        final Set<String> scopeSet = new HashSet<>();
        for (final Scope defaultScope : this.casScopeHandler.getDefaults()) {
            scopeSet.add(defaultScope.getName());
        }
        return scopeSet;
    }
}
