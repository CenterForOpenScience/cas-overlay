/*
 * Copyright (c) 2016. Center for Open Science
 *
 * Copyright 2012 - 2015 pac4j organization
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
package org.scribe.builder.api;

import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.extractors.OrcidJsonExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Verb;
import org.scribe.utils.OAuthEncoder;

/**
 * This class represents the OAuth API implementation for ORCiD using OAuth protocol version 2.
 *
 * @author Jens Tinglev
 * @author Michael Haselton
 * @author Longze Chen
 * @since 1.6.0
 */
public class OrcidApi20 extends DefaultApi20 {

    /** The authorization url. */
    private static final String AUTH_URL = "https://orcid.org/oauth/authorize";

    /** The token exchange url. */
    private static final String TOKEN_URL = "https://orcid.org/oauth/token";

    /**
     * The default constructor.
     */
    public OrcidApi20() {}

    /**
     * @return Return the url for token exchange endpoint.
     */
    @Override
    public String getAccessTokenEndpoint() {
        return TOKEN_URL;
    }

    /**
     * Get the authorization url with `oAuthConfig`.
     * @param oAuthConfig the oauth configuration
     * @return the authorization url
     */
    @Override
    public String getAuthorizationUrl(final OAuthConfig oAuthConfig) {
        // #show_login skips showing the registration form, which is only clutter-some.
        return String.format(
                AUTH_URL + "?client_id=%s&scope=%s&response_type=%s&redirect_uri=%s#show_login",
                oAuthConfig.getApiKey(), OAuthEncoder.encode(oAuthConfig.getScope()),
                "code", OAuthEncoder.encode(oAuthConfig.getCallback()));
    }

    /**
     * @return Return a new `OrcidJsonExtractor`.
     */
    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new OrcidJsonExtractor();
    }

    /**
     * @return Return the VERB for token exchange.
     */
    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }
}
