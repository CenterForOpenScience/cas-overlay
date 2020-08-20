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
package org.pac4j.oauth.client;

import org.apache.http.HttpStatus;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpCommunicationException;
import org.pac4j.oauth.client.exception.OAuthCredentialsException;
import org.pac4j.oauth.profile.OAuthAttributesDefinitions;
import org.pac4j.oauth.profile.orcid.OrcidAttributesDefinition;
import org.pac4j.oauth.profile.orcid.OrcidProfile;
import org.pac4j.oauth.profile.XmlHelper;

import org.scribe.builder.api.OrcidApi20;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthConfig;
import org.scribe.model.ProxyOAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.SignatureType;
import org.scribe.model.Token;
import org.scribe.oauth.ProxyOAuth20ServiceImpl;
import org.scribe.tokens.OrcidToken;

/**
 * The ORCiD Client.
 *
 * This class is the OAuth 2.0 client to authenticate users via ORCiD by authorization.
 *
 * @author Jens Tinglev
 * @author Michael Haselton
 * @author Longze Chen
 * @see org.pac4j.oauth.profile.orcid.OrcidProfile
 * @since 1.6.0
 */
public class OrcidClient extends BaseOAuth20Client<OrcidProfile> {

    /**
     * The default scope.
     *
     * OSF CAS uses the "/authenticate" scope for the purpose of authentication only. According to ORCiD API docs, this
     * scope is used when the client system will collect the ORCID iD but does not need access to read-limited data or
     * will use the ORCID system as an authentication provider. This scope is available on the Member or Public API.
     * For more information about ORCiD scopes, refer to https://members.orcid.org/api/oauth/orcid-scopes.
     *
     * In addition, "pac4j" version of the ORCiD OAuth 2.0 client implementation must use the 3-legged OAuth and the
     * "/authenticate" scope is such a 3-legged scope. For more information on 2-legged and 3-legged OAuth, refer to
     * https://members.orcid.org/api/oauth/2legged-oauth and https://members.orcid.org/api/oauth/3legged-oauth.
     */
    protected static final String DEFAULT_SCOPE = "/authenticate";

    /** The scope. */
    protected String scope = DEFAULT_SCOPE;

    /** The member flag. */
    protected Boolean member = Boolean.TRUE;

    /**
     * Instantiate a new {@link OrcidClient}.
     *
     * @param key the client key
     * @param secret the client secret
     * @param connectTimeout the timeout for connection
     * @param readTimeout the timeout for read
     */
    public OrcidClient(final String key, final String secret, final int connectTimeout, final int readTimeout) {
        this(key, secret);
        setConnectTimeout(connectTimeout);
        setReadTimeout(readTimeout);
    }

    /**
     * Instantiate a new {@link OrcidClient}.
     *
     * @param key the client key
     * @param secret the client secret
     */
    public OrcidClient(final String key, final String secret) {
        this();
        setKey(key);
        setSecret(secret);
    }

    /**
     * Instantiate a new {@link OrcidClient}.
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
     * This method overrides itself, and here are the customizations:
     *
     * 1. Replaces API 1.1 with API 2.0
     * 2. Add the option to use the free public API if {@link #member} is {@code false}
     *
     * @param accessToken the access token
     * @return profile uri
     * @throws OAuthException if wrong token type
     */
    @Override
    protected String getProfileUrl(final Token accessToken) {
        if (accessToken instanceof OrcidToken) {
            return String.format(
                    "https://%s.orcid.org/v2.0/%s/record",
                    this.getMember() ? "api" : "pub",
                    ((OrcidToken) accessToken).getOrcid()
            );
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
        this.service = new ProxyOAuth20ServiceImpl(
                new OrcidApi20(),
                new OAuthConfig(this.key, this.secret, this.callbackUrl, SignatureType.Header, this.getScope(), null),
                this.connectTimeout,
                this.readTimeout,
                this.proxyHost, this.proxyPort,
                false,
                true
        );
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
     * Extract the user profile from the profile response.
     *
     * This method replaces itself and here are the customizations:
     *
     * 1. Set the raw ORCiD ID as the profile ID
     * 2. Build the profile using normalized attributes so they can be successfully released to OSF
     *
     * @param body the response body
     * @return the profile
     */
    @Override
    protected OrcidProfile extractUserProfile(final String body) {

        final OrcidProfile profile = new OrcidProfile();
        profile.setId(XmlHelper.get(body, OrcidAttributesDefinition.ORCID));

        for(final String attribute : OAuthAttributesDefinitions.orcidDefinition.getAllAttributes()) {
            final String value = XmlHelper.get(body, attribute);
            switch (attribute) {
                case OrcidAttributesDefinition.NORMALIZED_FAMILY_NAME:
                case OrcidAttributesDefinition.NORMALIZED_GIVEN_NAME:
                    break;
                case OrcidAttributesDefinition.FAMILY_NAME:
                    profile.addAttribute(OrcidAttributesDefinition.NORMALIZED_FAMILY_NAME, value);
                    break;
                case OrcidAttributesDefinition.GIVEN_NAME:
                    profile.addAttribute(OrcidAttributesDefinition.NORMALIZED_GIVEN_NAME, value);
                    break;
                default:
                    profile.addAttribute(attribute, value);
                    break;
            }
        }
        return profile;
    }

    /**
     * Create a new {@link OrcidClient}.
     *
     * @return the new client
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
     * This method overrides {@link BaseOAuthClient#sendRequestForData(Token, String)}. Here are the customizations:
     *
     * 1. No longer appends the access token as a query parameter to the URL.
     * 2. Always includes the access token using the "Authorization" header.
     * 3. Improved the way how log messages are built.
     * 4. Use interface constants {@link HttpStatus#SC_OK} instead of number literals.
     *
     * @param accessToken the access token
     * @param dataUrl url of the data
     * @return the user data response
     * @throws HttpCommunicationException if fails to retrieve data
     */
    @Override
    protected String sendRequestForData(
            final Token accessToken,
            final String dataUrl
    ) throws HttpCommunicationException {

        logger.debug("accessToken : {} / dataUrl : {}", accessToken, dataUrl);
        final long t0 = System.currentTimeMillis();
        final ProxyOAuthRequest request = createProxyRequest(dataUrl);
        if (accessToken != null) {
            request.addHeader("Authorization", "Bearer " + accessToken.getToken());
        }
        final Response response = request.send();
        final int code = response.getCode();
        final String body = response.getBody();
        final long t1 = System.currentTimeMillis();
        logger.debug("Request took : {} ms for : {} ", (t1 - t0), dataUrl);
        logger.debug("response code : {} / response body : {}", code, body);
        if (code != HttpStatus.SC_OK) {
            logger.error("Failed to get data, code : {} / body : {}", code, body);
            throw new HttpCommunicationException(code, body);
        }
        return body;
    }
}
