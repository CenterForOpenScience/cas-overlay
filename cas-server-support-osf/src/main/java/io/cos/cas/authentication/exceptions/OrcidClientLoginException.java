/*
 * Copyright (c) 2019. Center for Open Science
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

/**
 * The ORCiD Client Login Exception.
 *
 * This is the special login exception class for authentication failures during delegated client login via
 * {@link org.jasig.cas.support.pac4j.web.flow.ClientAction} and {@link org.pac4j.oauth.client.OrcidClient}.
 *
 * @author Longze Chen
 * @since 19.3.0
 */
public class OrcidClientLoginException extends DelegatedLoginException {

    /** Unique id for serialization. */
    private static final long serialVersionUID = 2120800337731452548L;

    /**
     * Instantiate a new {@link OrcidClientLoginException} with no detail message.
     */
    public OrcidClientLoginException() {
        super();
    }

    /**
     * Instantiate a new {@link OrcidClientLoginException} with the specified detail message.
     *
     * @param msg the detail message
     */
    public OrcidClientLoginException(final String msg) {
        super(msg);
    }
}
