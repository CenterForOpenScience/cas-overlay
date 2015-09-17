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
package org.jasig.cas.support.oauth.personal.handler;

import org.jasig.cas.support.oauth.personal.PersonalAccessToken;
import org.jasig.cas.support.oauth.personal.handler.support.AbstractPersonalAccessTokenHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple Personal Token Handler.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public class SimplePersonalAccessTokenHandler extends AbstractPersonalAccessTokenHandler {

    /**
     * A list of personal access tokens.
     */
    private final Set<PersonalAccessToken> tokens;

    /**
     * Constructs a new instance of simple personal access token handler without any assigned tokens.
     */
    public SimplePersonalAccessTokenHandler() {
        this(new HashSet<PersonalAccessToken>());
    }

    /**
     * Constructs a new instance of simple personal access token handler with the set of tokens specified.
     *
     * @param tokens the set of tokens.
     */
    public SimplePersonalAccessTokenHandler(final Set<PersonalAccessToken> tokens) {
        this.tokens = tokens;
    }

    @Override
    public PersonalAccessToken getToken(final String tokenId) {
        for (final PersonalAccessToken token : this.tokens) {
            if (token.getId().equals(tokenId)) {
                return token;
            }
        }
        return null;
    }
}
