/*
  Copyright 2012 - 2015 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
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
 * @since 1.6.0
 */
public class OrcidApi20 extends DefaultApi20 {

    private static final String AUTH_URL = "http://www.orcid.org/oauth/authorize";
    private static final String TOKEN_URL = "https://%s.orcid.org/oauth/token";
    private final Boolean member;

    public OrcidApi20() {
        this(Boolean.TRUE);
    }

    public OrcidApi20(final Boolean member) {
        this.member = member;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return String.format(TOKEN_URL, (this.member ? "api" : "pub"));
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig oAuthConfig) {
        // #show_login skips showing the registration form, which is only
        // cluttersome.
        return String.format(AUTH_URL + "?client_id=%s&scope=%s&response_type=%s&redirect_uri=%s#show_login",
                oAuthConfig.getApiKey(), OAuthEncoder.encode(oAuthConfig.getScope()), "code", OAuthEncoder.encode(oAuthConfig.getCallback()));
    }

    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new OrcidJsonExtractor();
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

}