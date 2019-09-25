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
package org.jasig.cas.support.oauth.authentication.principal;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.support.oauth.token.TokenType;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth credential.
 *
 * As an extension of the built-in credential {@link Credential}, this class is used to generate the ticket granting
 * ticket during the process of granting three types of OAuth tokens: ONLINE access token, OFFLINE refresh token and
 * PERSONAL access token.
 *
 * In addition, since the OAuth credential rely on the primary CAS authentication, this class wraps the existing
 * authorization so that we can apply specific expiration policies based on the access type {@link #accessType}. For
 * more information, see {@link org.jasig.cas.support.oauth.ticket.support.OAuthDelegatingExpirationPolicy}.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuthCredential implements Credential {

    /** Authentication attribute name for access type. **/
    public static final String AUTHENTICATION_ATTRIBUTE_ACCESS_TYPE = "oAuthAccessType";

    /** Unique id for serialization. */
    private static final long serialVersionUID = -98723987239832729L;

    private final String id;

    private final Map<String, Object> attributes;

    private final TokenType accessType;

    /**
     * Instantiates a new {@link OAuthCredential}.
     *
     * @param id the user id
     * @param accessType the access type
     */
    public OAuthCredential(final String id, final TokenType accessType) {
        this(id, new HashMap<>(), accessType);
    }

    /**
     * Instantiates a new {@link OAuthCredential}.
     *
     * @param id the user id
     * @param attributes the attributes
     * @param accessType the access type
     */
    public OAuthCredential(final String id, final Map<String, Object> attributes, final TokenType accessType) {
        this.id = id;
        this.attributes = attributes;
        this.accessType = accessType;
    }

    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public String getId() {
        return this.id;
    }

    TokenType getAccessType() {
        return this.accessType;
    }

    @Override
    public String toString() {
        return this.getId();
    }
}
