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
package org.jasig.cas.support.oauth;

import org.jasig.cas.authentication.RootCasException;

/**
 * The exception to throw when a parameter is invalid or missing.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public class InvalidParameterException extends RootCasException {

    private static final long serialVersionUID = -6122987319096575896L;

    /** The code description. */
    private static final String CODE = "INVALID_PARAMETER";

    private final String name;

    /**
     * Constructs a InvalidParameterException with the default exception code.
     * @param name the name of the parameter that originally caused this exception to be thrown.
     */
    public InvalidParameterException(final String name) {
        super(CODE);
        this.name = name;
    }

    /**
     * Constructs a InvalidParameterException with the default exception code and
     * the original exception that was thrown.
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
     * @return the message
     * @see InvalidParameterException#name
     */
    @Override
    public String getMessage() {
        return String.format("Invalid or missing parameter '%s'", this.name);
    }
}
