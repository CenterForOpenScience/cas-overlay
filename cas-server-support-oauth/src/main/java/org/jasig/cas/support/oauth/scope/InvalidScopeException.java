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

import org.jasig.cas.authentication.RootCasException;

/**
 * The exception to throw when we cannot find a requested scope.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public class InvalidScopeException extends RootCasException {

    /** Unique id for serialization. */
    private static final long serialVersionUID = -8143505357364890832L;

    /** The code description. */
    private static final String CODE = "INVALID_SCOPE";

    private final String scope;

    /**
     * Instantiates a {@link InvalidScopeException} with the default exception code.
     *
     * @param scope the scope that originally caused this exception to be thrown
     */
    public InvalidScopeException(final String scope) {
        super(CODE);
        this.scope = scope;
    }

    /**
     * Instantiates a {@link InvalidScopeException} with the default exception code and the original exception.
     *
     * @param throwable the chained exception
     * @param scope the scope that originally caused this exception to be thrown
     */
    public InvalidScopeException(final Throwable throwable, final String scope) {
        super(CODE, throwable);
        this.scope = scope;
    }

    /**
     * Returns the message of this exception.
     *
     * @return the message
     * @see InvalidScopeException#scope
     */
    @Override
    public String getMessage() {
        return String.format("Invalid scope '%s'", this.scope);
    }
}
