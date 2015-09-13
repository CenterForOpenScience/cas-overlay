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
package org.jasig.cas.support.oauth.token;

import org.jasig.cas.authentication.RootCasException;

/**
 * The exception to throw when we cannot verify the token.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public class InvalidTokenException extends RootCasException {

    private static final long serialVersionUID = -5875760146336410828L;

    /** The code description. */
    private static final String CODE = "INVALID_TOKEN";

    private final String tokenId;

    /**
     * Constructs a InvalidTokenException with the default exception code.
     * @param tokenId the token id that originally caused this exception to be thrown.
     */
    public InvalidTokenException(final String tokenId) {
        super(CODE);
        this.tokenId = tokenId;
    }

    /**
     * Constructs a InvalidTokenException with the default exception code and
     * the original exception that was thrown.
     *
     * @param throwable the chained exception
     * @param tokenId the token id that originally caused this exception to be thrown.
     */
    public InvalidTokenException(final Throwable throwable, final String tokenId) {
        super(CODE, throwable);
        this.tokenId = tokenId;
    }

    /**
     * Returns the message of this exception, token is not included for security purposes.
     * @return the message
     * @see InvalidTokenException#tokenId
     */
    @Override
    public String getMessage() {
        return "Invalid Token";
    }
}
