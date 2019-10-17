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

import javax.security.auth.login.FailedLoginException;

/**
 * The Delegated Login Exception.
 *
 * This is a generic login exception for authentication via delegated client.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class DelegatedLoginException extends FailedLoginException {

    /** Unique id for serialization. */
    private static final long serialVersionUID = 2120800337731452548L;

    /**
     * Instantiate a new {@link DelegatedLoginException} with no detail message.
     */
    public DelegatedLoginException() {
        super();
    }

    /**
     * Instantiate a new {@link DelegatedLoginException} with the specified detail message.
     *
     * @param msg the detail message
     */
    public DelegatedLoginException(final String msg) {
        super(msg);
    }
}
