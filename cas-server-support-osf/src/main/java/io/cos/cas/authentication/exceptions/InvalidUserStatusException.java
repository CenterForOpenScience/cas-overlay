/*
 * Copyright (c) 2020. Center for Open Science
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
package io.cos.cas.authentication.exceptions;

import javax.security.auth.login.AccountException;

/**
 * Describes an error condition where user status is invalid, which currently includes the following cases.
 *
 * 1. is an unclaimed user which has been created as a new contributor
 * 2. is an inactive user which has been merged into another account
 * 3. has an external identity that can not be parsed
 * 4. has other undefined status, possibly due to internal bug or user model changes
 *
 * @author Longze Chen
 * @since 20.1.0
 */
public class InvalidUserStatusException extends AccountException {

    private static final long serialVersionUID = 8296529645368130304L;

    /** Instantiates a new exception (default). */
    public InvalidUserStatusException() {
        super();
    }

    /**
     * Instantiates a new exception with a given message.
     *
     * @param message the message
     */
    public InvalidUserStatusException(final String message) {
        super(message);
    }
}
