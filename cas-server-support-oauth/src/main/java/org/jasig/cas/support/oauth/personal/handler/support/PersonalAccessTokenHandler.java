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
package org.jasig.cas.support.oauth.personal.handler.support;

import org.jasig.cas.support.oauth.personal.PersonalAccessToken;

/**
 * The interface for personal access token handler classes.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public interface PersonalAccessTokenHandler {

    /**
     * Get a personal access token from the handler by the token id specified.
     *
     * @param tokenId the token id
     * @return a personal access token or null
     */
    PersonalAccessToken getToken(String tokenId);
}
