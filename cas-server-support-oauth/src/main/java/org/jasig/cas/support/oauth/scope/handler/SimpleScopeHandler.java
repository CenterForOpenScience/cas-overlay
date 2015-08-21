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
package org.jasig.cas.support.oauth.scope.handler;

import org.jasig.cas.support.oauth.scope.Scope;
import org.jasig.cas.support.oauth.scope.handler.support.AbstractScopeHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple OAuth Scope Handler
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public class SimpleScopeHandler extends AbstractScopeHandler {

    private Set<Scope> scopes;

    public SimpleScopeHandler() {
        this(new HashSet<Scope>());
    }

    public SimpleScopeHandler(Set<Scope> scopes) {
        this.scopes = scopes;
    }

    @Override
    public Scope getScope(String name) {
        for (Scope scope : this.scopes) {
            if (scope.getName().equalsIgnoreCase(name)) {
                return scope;
            }
        }
        return null;
    }

    @Override
    public Set<Scope> getDefaults() {
        final Set<Scope> defaultScopes = new HashSet<>();
        for (Scope scope : this.scopes) {
            if (scope.getIsDefault()) {
                defaultScopes.add(scope);
            }
        }
        return defaultScopes;
    }
}
