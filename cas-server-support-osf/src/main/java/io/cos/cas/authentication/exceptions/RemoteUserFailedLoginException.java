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

package io.cos.cas.authentication.exceptions;

import javax.security.auth.login.AccountException;

/**
 * Describes an error condition where authentication occurs during delegated authentication.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public class RemoteUserFailedLoginException extends AccountException {

    private static final long serialVersionUID = 3472948140572518658L;

    /**
     * Instantiates a new remote user login failure exception.
     */
    public RemoteUserFailedLoginException() {
        super();
    }

    /**
     * Instantiates a new remote user login failure exception.
     *
     * @param message the message
     */
    public RemoteUserFailedLoginException(final String message) {
        super(message);
    }
}
