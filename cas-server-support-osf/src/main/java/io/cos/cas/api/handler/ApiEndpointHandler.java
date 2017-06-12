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

package io.cos.cas.api.handler;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.cos.cas.api.type.ApiEndpoint;
import io.cos.cas.web.util.AbstractFlowUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicHeader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

import java.io.IOException;
import java.util.Date;

/**
 * Handles Communication between CAS and API.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class ApiEndpointHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiEndpointHandler.class);

    private static final int SIXTY_SECONDS = 60 * 1000;

    @NotNull
    private String casLoginUrl;

    @NotNull
    private String casCreateOrLinkOsfAccountUrl;

    @NotNull
    private String apiCasEndpointUrl;

    @NotNull
    private String apiCasEndpointJweSecret;

    @NotNull
    private String apiCasEndpointJwtSecret;

    /**
     * Instantiates an instance of Open Science Framework API CAS Endpoint and set endpoint url.
     *
     * @param casLoginUrl the CAS login URL
     * @param casCreateOrLinkOsfAccountUrl the CAS account manager URL for create or link OSF account with external id
     * @param apiCasEndpointUrl the API CAS Endpoint URL
     * @param apiCasEndpointJweSecret the Jwe Secret
     * @param apiCasEndpointJwtSecret the Jwt Secret
     */
    public ApiEndpointHandler(
            final String casLoginUrl,
            final String casCreateOrLinkOsfAccountUrl,
            final String apiCasEndpointUrl,
            final String apiCasEndpointJweSecret,
            final String apiCasEndpointJwtSecret
    ) {
        this.casLoginUrl = casLoginUrl;
        this.casCreateOrLinkOsfAccountUrl = casCreateOrLinkOsfAccountUrl;
        this.apiCasEndpointUrl = apiCasEndpointUrl;
        this.apiCasEndpointJweSecret = apiCasEndpointJweSecret;
        this.apiCasEndpointJwtSecret = apiCasEndpointJwtSecret;
    }

    public String getCasLoginUrl() {
        return casLoginUrl;
    }

    public String getCasCreateOrLinkOsfAccountUrl() {
        return casCreateOrLinkOsfAccountUrl;
    }

    /**
     * Encrypt the payload to be posted to the OSF API endpoint.
     *
     * @param subject the subject
     * @param payload the payload
     * @return String, a serialized JWE object
     */
     public String encryptPayload(final String subject, final String payload) {

        final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(subject)
                .claim("data", payload)
                .expirationTime(new Date(new Date().getTime() + SIXTY_SECONDS))
                .build();

        final SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

        final JWSSigner signer;
        try {
            signer = new MACSigner(apiCasEndpointJwtSecret.getBytes());
            signedJWT.sign(signer);
        } catch (final JOSEException e){
            return null;
        }

        final JWEObject jweObject = new JWEObject(
                new JWEHeader.Builder(
                        JWEAlgorithm.DIR,
                        EncryptionMethod.A256GCM
                ).contentType("JWT") .build(),
                new Payload(signedJWT));
        try {
            jweObject.encrypt(new DirectEncrypter(apiCasEndpointJweSecret.getBytes()));
            return jweObject.serialize();
        } catch (final JOSEException e) {
            return null;
        }
    }

    /**
     * Handle API Request and Response.
     *
     * @param endpoint the API endpoint
     * @param payload the payload
     * @return JSONObject
     */
    public JSONObject handle(final ApiEndpoint endpoint, final String payload) {

        final String url = apiCasEndpointUrl + endpoint.getId() + '/';
        final HttpResponse response = makeApiRequest(url, payload);
        if (response != null) {
            final JSONObject parsedResponse = parseApiResponse(url, response);
            if (parsedResponse != null) {
                LOGGER.info("API Response Received {} {}.", url, parsedResponse.getInt("status"));
                return parsedResponse;
            }
        }

        return null;
    }

    /**
     * Get the error message from HTTP 400s response.
     *
     * @param responseBody the 400s response body
     * @return the error detail in 400s response
     */
    public String getErrorMessageFromResponseBody(final JSONObject responseBody) {
        if (responseBody != null && responseBody.has("errors")) {
            final JSONArray errorList = responseBody.getJSONArray("errors");
            if (errorList.length() == 1 && errorList.getJSONObject(0).has("detail")) {
                return errorList.getJSONObject(0).getString("detail");
            }
        }
        LOGGER.error("Invalid HTTP 403/401 response.");
        return AbstractFlowUtils.DEFAULT_SERVER_ERROR_MESSAGE;
    }

    /**
     * Make HTTP POST Request to API CAS Endpoint.
     *
     * @param url the full request URL
     * @param payload the payload
     * @return  HttpResponse
     */
    private HttpResponse makeApiRequest(final String url, final String payload) {

        LOGGER.info("Making API Request {}.", url);
        try {
            return Request.Post(url)
                    .addHeader(new BasicHeader("Content-Type", "application/json"))
                    .bodyString(payload, ContentType.APPLICATION_JSON)
                    .execute()
                    .returnResponse();
        } catch (final IOException e) {
            LOGGER.error("An exception has occurred during API request: {}", url);
            LOGGER.debug(e.getMessage());
            return null;
        }
    }

    /**
     * Parse HTTP POST Response from API CAS Endpoint.
     *
     * @param url the full request URL
     * @param response the http response
     * @return JSONObject
     */
    private JSONObject parseApiResponse(final String url, final HttpResponse response) {

        final int statusCode = response.getStatusLine().getStatusCode();
        final JSONObject parsedResponse = new JSONObject();

        if (statusCode == HttpStatus.SC_NO_CONTENT) {
            return parsedResponse.put("status", statusCode);
        }

        if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_UNAUTHORIZED || statusCode == HttpStatus.SC_FORBIDDEN) {
            try {
                parsedResponse.put("status", statusCode);
                parsedResponse.put("body", new JSONObject(new BasicResponseHandler().handleEntity(response.getEntity())));
                return parsedResponse;
            } catch (final IOException | JSONException e) {
                LOGGER.error("An exception has occurred when parsing API response: {}", url);
                LOGGER.debug(e.getMessage());
                return null;
            }
        }

        LOGGER.error("API Request Failed: {} {}", url, statusCode);
        return null;
    }
}
