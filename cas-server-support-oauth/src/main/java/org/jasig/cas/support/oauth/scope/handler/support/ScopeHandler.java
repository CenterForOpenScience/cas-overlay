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
package org.jasig.cas.support.oauth.scope.handler.support;

import org.jasig.cas.support.oauth.scope.Scope;

import java.util.Set;

/**
 * The interface for OAuth scope handler classes.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public interface ScopeHandler {

    /**
     * Get the scope specified by name.
     *
     * @param name the name of the scope
     * @return the scope
     */
    Scope getScope(String name);

    /**
     * Get the list of default scopes.
     *
     * @return a list of default scopes
     */
    Set<Scope> getDefaults();
}
