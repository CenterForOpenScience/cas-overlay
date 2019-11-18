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
package org.jasig.cas.support.oauth.personal;

import org.jasig.cas.support.oauth.personal.handler.support.PersonalAccessTokenHandler;

/**
 * Personal access token manager.
 *
 * This PAT manager is a property of the CAS OAuth Service {@link org.jasig.cas.support.oauth.CentralOAuthServiceImpl}
 * and is used to retrieve PATs via its PAT handler. With current CAS settings, the OSF PAT handler is the one being
 * used. See {@link io.cos.cas.adaptors.postgres.handlers.OpenScienceFrameworkPersonalAccessTokenHandler} for details.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public class PersonalAccessTokenManager {

    /** An instance of the personal access token handler. */
    private final PersonalAccessTokenHandler personalAccessTokenHandler;

    /**
     * Constructs a new personal access token manager with the handler specified.
     *
     * @param personalAccessTokenHandler the handler
     */
    public PersonalAccessTokenManager(final PersonalAccessTokenHandler personalAccessTokenHandler) {
        this.personalAccessTokenHandler = personalAccessTokenHandler;
    }

    /**
     * Get a personal access token from the handler by the token id specified.
     *
     * @param tokenId the token id
     * @return a personal access token or null
     */
    public PersonalAccessToken getToken(final String tokenId) {
        return personalAccessTokenHandler.getToken(tokenId);
    }
}
