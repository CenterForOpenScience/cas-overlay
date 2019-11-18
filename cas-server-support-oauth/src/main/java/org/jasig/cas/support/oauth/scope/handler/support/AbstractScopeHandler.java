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

import java.util.HashSet;
import java.util.Set;

/**
 * The abstract base class for OAuth scope handler classes.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public abstract class AbstractScopeHandler implements ScopeHandler {

    @Override
    public Set<Scope> getDefaults() {
        return new HashSet<>();
    }
}
