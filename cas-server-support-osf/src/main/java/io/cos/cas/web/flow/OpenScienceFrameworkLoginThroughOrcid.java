/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

package io.cos.cas.web.flow;

import io.cos.cas.authentication.RemoteUserFailedLoginException;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.security.auth.login.AccountException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Open Science Framework Login Through ORCID.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
public class OpenScienceFrameworkLoginThroughOrcid {

    /** The Logger Instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** ORCID Authorize URL. */
    private final String authorizeUrl;

    /** ORCID Token URL. */
    private final String tokenUrl;

    /** Client ID. */
    private final String clientId;

    /** Client Secret. */
    private final String clientSecret;

    /** OAuth Scope. */
    private final String oauthScope;

    /** Redirect URI. */
    private final String redirectUri;

    /**
     * Create a new instance with given parameters.
     *
     * @param authorizeUrl  ORCID OAuth2 Authorize End Point
     * @param tokenUrl      ORCID OAuth2 Token Exchange End Point
     * @param clientId      ORCID OSF Application Client ID
     * @param clientSecret  ORCID OSF Application Client Secret
     * @param oauthScope    ORCID OSF Application OAuth Scope
     * @param redirectUri   ORCID OSF Redirect URI
     */
    public OpenScienceFrameworkLoginThroughOrcid(
            String authorizeUrl,
            String tokenUrl,
            String clientId,
            String clientSecret,
            String oauthScope,
            String redirectUri) {
        this.authorizeUrl = authorizeUrl;
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.oauthScope = oauthScope;
        this.redirectUri = redirectUri;
    }

    /**
     * Build OAuth2 authorization url for ORCID. Store `service url` in `state` parameter.
     * Redirect to ORCID authorization end point.
     *
     * @param context the Request Context
     * @return
     * @throws AccountException
     */
    public Event generateOAuthAuhtorizationUrl(final RequestContext context) throws AccountException {

        // TODO: add csrf token in state parameter, encrypt it with private key and then encode it
        String state = (String) context.getFlowScope().get("state");
        StringBuilder url = new StringBuilder();
        url.append(authorizeUrl)
                .append("?response_type=code")
                .append("&client_id=")
                .append(this.clientId)
                .append("&scope=")
                .append(oauthScope);
        try {
            url.append("&redirect_uri=")
                    .append(URLEncoder.encode(this.redirectUri, "UTF-8"))
                    .append("&state=")
                    .append(URLEncoder.encode(state, "UTF-8"));
        } catch (final UnsupportedEncodingException e) {
            logger.error("Unsupported Encoding Exception");
            throw new RemoteUserFailedLoginException("Unable to Build URL for ORCID Authorize Endpoint");
        }

        context.getFlowScope().put("orcidRedirectUrl", url.toString());
        return new Event(this, "success");
    }

    /**
     * Build and `POST` OAuth2 token exchange request. Receive user's credential including `username` and `orcid`.
     *
     * @param context the Request Context
     * @return
     * @throws AccountException
     */
    public Event exchangeForOAuthAccessToken(final RequestContext context) throws AccountException {

        logger.info(String.format("service url: %s", context.getRequestParameters().get("state")));

        Form postBodyForm = Form.form();
        postBodyForm.add("client_id", this.clientId)
                .add("client_secret", this.clientSecret)
                .add("code", context.getRequestParameters().get("code"))
                .add("grant_type", "authorization_code")
                .add("redirect_uri", this.redirectUri);
        try{
            // TODO: need to check the encoding scheme and choose the correct one
            final HttpResponse httpResponse = Request.Post(this.tokenUrl)
                    .addHeader(new BasicHeader("Accept", "application/json"))
                    .bodyForm(postBodyForm.build(), Consts.UTF_8)
                    .execute()
                    .returnResponse();

            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            InputStream in = httpResponse.getEntity().getContent();
            final String body = IOUtils.toString(in, "UTF-8");
            logger.info(String.format("status code: %d", statusCode));
            logger.info(String.format("response body: %s", body));
        } catch (final IOException e) {
            logger.error("Post for Access Token Exception: {}", e.getMessage());
            logger.trace("Post for Access Token Exception: {}", e);
            throw new RemoteUserFailedLoginException("Unable to Obtain Access Token from ORCID Token Endpoint");
        }

        // TODO: put credential in flow scope for building osf credential, grant TGT and ST
        return new Event(this, "success");
    }
}
