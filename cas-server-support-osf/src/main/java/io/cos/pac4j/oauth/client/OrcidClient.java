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
package io.cos.pac4j.oauth.client;

import io.cos.scribe.builder.api.OrcidApi20;
import org.apache.http.HttpStatus;
import org.pac4j.core.context.WebContext;
import org.pac4j.oauth.client.BaseOAuth20Client;
import org.pac4j.oauth.client.exception.OAuthCredentialsException;
import org.pac4j.oauth.profile.OAuthAttributesDefinitions;
import org.pac4j.oauth.profile.XmlHelper;
import org.pac4j.oauth.profile.orcid.OrcidAttributesDefinition;
import org.pac4j.oauth.profile.orcid.OrcidProfile;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthConfig;
import org.scribe.model.SignatureType;
import org.scribe.model.Token;
import org.scribe.oauth.ProxyOAuth20ServiceImpl;
import org.scribe.tokens.OrcidToken;
import org.scribe.model.ProxyOAuthRequest;
import org.scribe.model.Response;
import org.pac4j.core.exception.HttpCommunicationException;


/**
 * <p>This class is the OAuth client to authenticate users in ORCiD.</p>
 * <p>It returns a {@link org.pac4j.oauth.profile.orcid.OrcidProfile}.</p>
 * <p>More information at http://support.orcid.org/knowledgebase/articles/175079-tutorial-retrieve-data-from-an-orcid-record-with</p>
 *
 * @author Jens Tinglev
 * @author Michael Haselton
 * @author Longze Chen
 * @see org.pac4j.oauth.profile.orcid.OrcidProfile
 * @since 1.7.0
 */
public class OrcidClient extends BaseOAuth20Client<OrcidProfile> {

    /** The default scope. */
    protected static final String DEFAULT_SCOPE = "/orcid-profile/read-limited";

    /** The scope. */
    protected String scope = DEFAULT_SCOPE;

    /** The member flag. */
    protected Boolean member = Boolean.TRUE;

    /**
     * Create an instance of `OrcidClient` with given `key` and `secret`.
     *
     * @param key the key
     * @param secret the secret
     */
    public OrcidClient(final String key, final String secret) {
        setKey(key);
        setSecret(secret);
        setTokenAsHeader(true);
    }

    /**
     * Create an instance of `OrcidClient`.
     */
    public OrcidClient() {
        setTokenAsHeader(true);
    }

    /**
     * @return Returns whether use the `state` parameter for OAuth request.
     */
    @Override
    protected boolean requiresStateParameter() {
        return false;
    }

    /**
     * Check whether user has denied permissions.
     *
     * @param context the web context
     * @return if access has been denied by the user
     */
    @Override
    protected boolean hasBeenCancelled(final WebContext context) {
        final String error = context.getRequestParameter(OAuthCredentialsException.ERROR);
        final String errorDescription = context.getRequestParameter(OAuthCredentialsException.ERROR_DESCRIPTION);
        // user has denied permissions
        return ("access_denied".equals(error) && "User denied access".equals(errorDescription));
    }

    /**
     * Get the oauth url to retrieve user profile.
     *
     * @param accessToken the access token
     * @return profile uri
     * @throws OAuthException if wrong token type
     */
    @Override
    protected String getProfileUrl(final Token accessToken) {
        if (accessToken instanceof OrcidToken) {
            return String.format("https://%s.orcid.org/v1.2/%s/orcid-profile",
                    (this.getMember() ? "api" : "pub"), ((OrcidToken) accessToken).getOrcid());
        } else {
            throw new OAuthException("Token in getProfileUrl is not an OrcidToken");
        }
    }

    /**
     * The internal initialization.
     */
    @Override
    protected void internalInit() {
        super.internalInit();
        this.service = new ProxyOAuth20ServiceImpl(new OrcidApi20(this.getMember()), new OAuthConfig(this.key,
                this.secret,
                this.callbackUrl,
                SignatureType.Header,
                this.getScope(),
                null), this.connectTimeout, this.readTimeout, this.proxyHost, this.proxyPort, false, true);
    }

    /**
     * @return the oauth scope.
     */
    public String getScope() {
        return this.scope;
    }

    /**
     * @param scope the oauth scope.
     */
    public void setScope(final String scope) {
        this.scope = scope;
    }

    /**
     * @return the member flag.
     */
    public Boolean getMember() {
        return this.member;
    }

    /**
     * @param member the member flag.
     */
    public void setMember(final Boolean member) {
        this.member = member;
    }

    /**
     * Extract user profile.
     *
     * @param body the body
     * @return the profile
     */
    @Override
    protected OrcidProfile extractUserProfile(final String body) {
        final OrcidProfile profile = new OrcidProfile();
        profile.setId(XmlHelper.get(body, OrcidAttributesDefinition.ORCID));
        for(final String attribute : OAuthAttributesDefinitions.orcidDefinition.getAllAttributes()) {
            profile.addAttribute(attribute, XmlHelper.get(body, attribute));
        }
        return profile;
    }

    /**
     * Create a new `OrcidClient`.
     * @return the new cient
     */
    @Override
    protected OrcidClient newClient() {
        final OrcidClient newClient = new OrcidClient();
        newClient.setScope(this.scope);
        return newClient;
    }

    /**
     * Make a request to get the data of the authenticated user for the provider.
     *
     * @param accessToken the access token
     * @param dataUrl url of the data
     * @return the user data response
     * @throws HttpCommunicationException if fails to retrieve data
     */
    @Override
    protected String sendRequestForData(final Token accessToken, final String dataUrl) {
        logger.debug("accessToken : {} / dataUrl : {}", accessToken, dataUrl);
        final long t0 = System.currentTimeMillis();
        final ProxyOAuthRequest request = createProxyRequest(dataUrl);
        final Response response = request.send();
        final int code = response.getCode();
        final String body = response.getBody();
        final long t1 = System.currentTimeMillis();
        logger.debug(String.format("Request took : {} ms for : {} ", (t1 - t0), dataUrl));
        logger.debug("response code : {} / response body : {}", code, body);
        if (code != HttpStatus.SC_OK) {
            logger.error("Failed to get data, code : {} / body : {}", code, body);
            throw new HttpCommunicationException(code, body);
        }
        return body;
    }
}
