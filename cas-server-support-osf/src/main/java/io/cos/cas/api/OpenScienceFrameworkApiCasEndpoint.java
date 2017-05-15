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

package io.cos.cas.api;

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

import io.cos.cas.types.ApiEndpoint;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The Open Science Framework API CAS Endpoint.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
public class OpenScienceFrameworkApiCasEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkApiCasEndpoint.class);

    private static final int SIXTY_SECONDS = 60 * 1000;

    private static final Set<String> API_AUTHENTICATION_ERROR_LIST = new HashSet<>();

    static {

        // register, user already registered
        API_AUTHENTICATION_ERROR_LIST.add(AbstractOpenScienceFrameworkApiUtils.ALREADY_REGISTERED);

        // login, initial verification failed
        API_AUTHENTICATION_ERROR_LIST.add(AbstractOpenScienceFrameworkApiUtils.MISSING_CREDENTIALS);
        API_AUTHENTICATION_ERROR_LIST.add(AbstractOpenScienceFrameworkApiUtils.ACCOUNT_NOT_FOUND);
        API_AUTHENTICATION_ERROR_LIST.add(AbstractOpenScienceFrameworkApiUtils.INVALID_PASSWORD);
        API_AUTHENTICATION_ERROR_LIST.add(AbstractOpenScienceFrameworkApiUtils.INVALID_VERIFICATION_KEY);
        API_AUTHENTICATION_ERROR_LIST.add(AbstractOpenScienceFrameworkApiUtils.TWO_FACTOR_AUTHENTICATION_REQUIRED);

        // login, two factor verification failed
        API_AUTHENTICATION_ERROR_LIST.add(AbstractOpenScienceFrameworkApiUtils.INVALID_ONE_TIME_PASSWORD);

        // login, invalid user status
        API_AUTHENTICATION_ERROR_LIST.add(AbstractOpenScienceFrameworkApiUtils.USER_NOT_CONFIRMED);
        API_AUTHENTICATION_ERROR_LIST.add(AbstractOpenScienceFrameworkApiUtils.USER_NOT_CLAIMED);
        API_AUTHENTICATION_ERROR_LIST.add(AbstractOpenScienceFrameworkApiUtils.USER_STATUS_INVALID);
        API_AUTHENTICATION_ERROR_LIST.add(AbstractOpenScienceFrameworkApiUtils.USER_DISABLED);
    }

    @NotNull
    private String osfApiCasEndpointUrl;

    @NotNull
    private String apiCasEndpointJweSecret;

    @NotNull
    private String apiCasEndpointJwtSecret;

    /**
     * Instantiates an instance of Open Science Framework API CAS Endpoint and set endpoint url.
     *
     * @param osfApiCasEndpointUrl the OSF API CAS endpoint URL
     * @param apiCasEndpointJweSecret the Jwe Secret
     * @param apiCasEndpointJwtSecret the Jwt Secret
     */
    public OpenScienceFrameworkApiCasEndpoint(
        final String osfApiCasEndpointUrl,
        final String apiCasEndpointJweSecret,
        final String apiCasEndpointJwtSecret
    ) {
        this.osfApiCasEndpointUrl = osfApiCasEndpointUrl;
        this.apiCasEndpointJweSecret = apiCasEndpointJweSecret;
        this.apiCasEndpointJwtSecret = apiCasEndpointJwtSecret;
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
     * Make service requests to API CAS "Service" endpoint.
     * Return the response as a JSON object if `HttpStatus.SC_OK`.
     * Otherwise, log the error and return `null`.
     *
     * @param endpoint the api cas service endpoint
     * @param payload the jwe/jwt encrypted payload
     * @return a JSON object or null
     */
    public JSONObject apiCasService(final ApiEndpoint endpoint, final String payload) {

        final String url = osfApiCasEndpointUrl + endpoint.getId() + '/';
        final HttpResponse httpResponse;
        try {
            httpResponse = Request.Post(url)
                .addHeader(new BasicHeader("Content-Type", "text/plain"))
                .bodyString(payload, ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();
        } catch (final IOException e) {
            LOGGER.debug(e.getMessage());
            return null;
        }

        final int statusCode = httpResponse.getStatusLine().getStatusCode();
        LOGGER.info(
            "API CAS Endpoint \"service/{}/\": {}",
            endpoint,
            statusCode
        );
        try {
            final JSONObject responseBody =  new JSONObject(new BasicResponseHandler().handleEntity(httpResponse.getEntity()));
            if (statusCode == HttpStatus.SC_OK) {
                return responseBody;
            } else if (statusCode == HttpStatus.SC_FORBIDDEN) {
                LOGGER.debug(responseBody.toString());
            }
            return null;
        }catch (final IOException | JSONException e) {
            LOGGER.debug(e.getMessage());
            return null;
        }
    }

    /**
     * Make authentication requests to API CAS "Auth" endpoint.
     * Parse response and return a map object containing authentication status and exception messages.
     *
     * @param endpoint the api cas auth endpoint
     * @param email the email or username
     * @param payload the jwe/jwt encrypted payload
     * @return a Map object
     */
     public Map<String, Object> apiCasAuthentication(final ApiEndpoint endpoint, final String email, final String payload) {

        final String url = osfApiCasEndpointUrl + endpoint.getId() + '/';
        final HttpResponse httpResponse;
        try {
            httpResponse = Request.Post(url)
                .addHeader(new BasicHeader("Content-Type", "text/plain"))
                .bodyString(payload, ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();
        } catch (final IOException e) {
            LOGGER.debug(e.getMessage());
            return null;
        }

        final int statusCode = httpResponse.getStatusLine().getStatusCode();
        LOGGER.info(
            "API CAS Endpoint {} Response: <{}> Status Code {}",
            endpoint,
            email,
            statusCode
        );
        try {
            final JSONObject responseBody =  new JSONObject(new BasicResponseHandler().handleEntity(httpResponse.getEntity()));
            return verifyAuthenticationResponse(statusCode, responseBody);
        }catch (final IOException | JSONException e) {
            LOGGER.debug(e.getMessage());
            return null;
        }
    }

    /**
     * Parse and verify authentication response.
     * Return a map object containing authentication status and exception messages.
     *
     * @param statusCode the response status code
     * @param responseBody the response body in JSON format
     * @return a Map object
     */
    private Map<String, Object> verifyAuthenticationResponse(final int statusCode, final JSONObject responseBody) {
        final Map<String, Object> response = new HashMap<>();
        final Map<String, Object> attributesMap = new HashMap<>();
        try {
            if (statusCode == HttpStatus.SC_OK) {
                final String status = responseBody.getString("status");
                if (AbstractOpenScienceFrameworkApiUtils.REGISTRATION_SUCCESS.equals(status)) {
                    response.put("status", status);
                    return response;
                }
                if (AbstractOpenScienceFrameworkApiUtils.AUTHENTICATION_SUCCESS.equals(status)) {
                    response.put("status", status);
                    response.put("userId", responseBody.getString("userId"));
                    final JSONObject attributes = responseBody.getJSONObject("attributes");
                    attributesMap.put("username", attributes.getString("username"));
                    attributesMap.put("givenName", attributes.getString("givenName"));
                    attributesMap.put("familyName", attributes.getString("familyName"));
                    response.put("attributes", attributesMap);
                    return response;
                }
                throw new IOException(String.format("INVALID_RESPONSE_SC_OK: status = %s.", status));
            } else if (statusCode == HttpStatus.SC_FORBIDDEN) {
                final JSONArray errorList = responseBody.getJSONArray("errors");
                if (errorList.length() != 1) {
                    throw new IOException("INVALID_RESPONSE_SC_FORBIDDEN: multiple errors.");
                }
                final JSONObject error = errorList.getJSONObject(0);
                final String errorDetail = error.getString("detail");
                if (!API_AUTHENTICATION_ERROR_LIST.contains(errorDetail)) {
                    throw new IOException(String.format("INVALID_RESPONSE_SC_FORBIDDEN: status = %s.", errorDetail));
                }
                response.put("status", AbstractOpenScienceFrameworkApiUtils.AUTHENTICATION_FAILURE);
                response.put("detail", errorDetail);
                LOGGER.debug("Authentication Failure: {}", errorDetail);
                return response;
            } else {
                throw new IOException(String.format("INVALID_RESPONSE_SC_OTHER: status code = %d.", statusCode));
            }
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage());
            response.put("status", "UNKNOWN");
            return response;
        }
    }
}
