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
package org.jasig.cas.support.oauth.scope.handler;

import org.jasig.cas.support.oauth.scope.Scope;
import org.jasig.cas.support.oauth.scope.handler.support.AbstractScopeHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple OAuth scope handler.
 *
 * With current CAS settings, this handler is not used as the primary scope handler but only as the CAS scope handler
 * in the scope manager {@link org.jasig.cas.support.oauth.scope.ScopeManager}, which uses the OSF scope handler
 * {@literal io.cos.cas.adaptors.postgres.handlers.OpenScienceFrameworkScopeHandler} as its primary one.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public class SimpleScopeHandler extends AbstractScopeHandler {

    /** The set of scopes added to the handler. */
    private final Set<Scope> scopes;

    /** Constructs a new instance of the scope handler with a blank list of scopes. */
    public SimpleScopeHandler() {
        this(new HashSet<>());
    }

    /**
     * Constructors a new instance of the scope handler with the list of scopes specified.
     *
     * @param scopes the list of scopes
     */
    public SimpleScopeHandler(final Set<Scope> scopes) {
        this.scopes = scopes;
    }

    @Override
    public Scope getScope(final String name) {
        for (final Scope scope : this.scopes) {
            if (scope.getName().equalsIgnoreCase(name)) {
                return scope;
            }
        }
        return null;
    }

    @Override
    public Set<Scope> getDefaults() {
        final Set<Scope> defaultScopes = new HashSet<>();
        for (final Scope scope : this.scopes) {
            if (scope.getIsDefault()) {
                defaultScopes.add(scope);
            }
        }
        return defaultScopes;
    }
}
