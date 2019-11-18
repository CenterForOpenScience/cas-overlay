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
package org.jasig.cas.support.oauth;

import org.jasig.cas.authentication.RootCasException;

/**
 * The exception to throw when a parameter is invalid or missing.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public class InvalidParameterException extends RootCasException {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private static final long serialVersionUID = -6122987319096575896L;

    /** The code description. */
    private static final String CODE = "INVALID_PARAMETER";

    /** The name of the invalid or missing parameter. */
    private final String name;

    /**
     * Instantiates a {@link InvalidParameterException} with the default exception code and a given parameter name.
     *
     * @param name the name of the parameter that originally caused this exception to be thrown
     */
    public InvalidParameterException(final String name) {
        super(CODE);
        this.name = name;
    }

    /**
     * Instantiates a {@link InvalidParameterException} with the default exception code, the original exception that
     * was thrown and a given parameter name.
     *
     * @param throwable the chained exception
     * @param name the name of the parameter that originally caused this exception to be thrown.
     */
    public InvalidParameterException(final Throwable throwable, final String name) {
        super(CODE, throwable);
        this.name = name;
    }

    /**
     * Returns the message of this exception.
     *
     * @return the message
     * @see InvalidParameterException#name
     */
    @Override
    public String getMessage() {
        return String.format("Invalid or missing parameter '%s'", this.name);
    }
}
