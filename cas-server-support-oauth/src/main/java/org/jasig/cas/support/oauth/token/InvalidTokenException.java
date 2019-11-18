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
package org.jasig.cas.support.oauth.token;

import org.jasig.cas.authentication.RootCasException;

/**
 * The exception to throw when we cannot verify the token.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public class InvalidTokenException extends RootCasException {

    /** Unique id for serialization. */
    private static final long serialVersionUID = -5875760146336410828L;

    /** The code description. */
    private static final String CODE = "INVALID_TOKEN";

    private final String tokenId;

    /**
     * Instantiates a new {@link InvalidTokenException} with the default exception code.
     *
     * @param tokenId the token id that originally caused this exception to be thrown
     */
    public InvalidTokenException(final String tokenId) {
        super(CODE);
        this.tokenId = tokenId;
    }

    /**
     * Instantiates a new {@link InvalidTokenException} with the default exception code and the original exception.
     *
     * @param throwable the chained exception
     * @param tokenId the token id that originally caused this exception to be thrown
     */
    public InvalidTokenException(final Throwable throwable, final String tokenId) {
        super(CODE, throwable);
        this.tokenId = tokenId;
    }

    /**
     * Returns the message of this exception, token is not included for security purposes.
     *
     * @return the message
     * @see InvalidTokenException#tokenId
     */
    @Override
    public String getMessage() {
        return "Invalid Token";
    }
}
