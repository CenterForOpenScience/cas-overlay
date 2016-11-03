package io.cos.cas.adaptors.api;

/*
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
*/
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.IOException;
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
        API_AUTHENTICATION_ERROR_LIST.add("ALREADY_REGISTERED");
        // login, initial verification failed
        API_AUTHENTICATION_ERROR_LIST.add("MISSING_CREDENTIALS");
        API_AUTHENTICATION_ERROR_LIST.add("ACCOUNT_NOT_FOUND");
        API_AUTHENTICATION_ERROR_LIST.add("INVALID_PASSWORD");
        API_AUTHENTICATION_ERROR_LIST.add("INVALID_VERIFICATION_KEY");
        API_AUTHENTICATION_ERROR_LIST.add("TWO_FACTOR_AUTHENTICATION_REQUIRED");
        // login, two factor verification failed
        API_AUTHENTICATION_ERROR_LIST.add("INVALID_ONE_TIME_PASSWORD");
        // login, invalid user status
        API_AUTHENTICATION_ERROR_LIST.add("USER_NOT_REGISTERED");
        API_AUTHENTICATION_ERROR_LIST.add("USER_NOT_CLAIMED");
        API_AUTHENTICATION_ERROR_LIST.add("USER_NOT_ACTIVE");
        API_AUTHENTICATION_ERROR_LIST.add("USER_MERGED");
        API_AUTHENTICATION_ERROR_LIST.add("USER_DISABLED");
    }

    @NotNull
    private String osfApiCasEndpointUrl;

    /**
     * Default Constructor.
     */
    public OpenScienceFrameworkApiCasEndpoint() {}

    /**
     * Instantiates an instance of Open Science Framework API CAS Endpoint and set endpoint url.
     *
     * @param osfApiCasEndpointUrl the OSF API CAS endpoint UL
     */
    public OpenScienceFrameworkApiCasEndpoint(final String osfApiCasEndpointUrl) {
        this.osfApiCasEndpointUrl = osfApiCasEndpointUrl;
    }

/*
    @NotNull
    private String apiCasEndpointJweSecret;

    @NotNull
    private String apiCasEndpointJwtSecret;

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
*/

    /**
     * Make authentication requests to API CAS endpoint with `endpoint`, `email` and `payload`.
     * Parse response and return a map object containing authentication status and exception messages.
     * `payload` is encrypted before the request is sent and decrypted after the response is received.
     *
     * @param endpoint the api cas endpoint
     * @param email the email or username
     * @param payload the plaintext payload
     * @return a Map object
     */
    public Map<String, Object> apiCasAuthentication(final String endpoint, final String email, final String payload) {

        final String url = osfApiCasEndpointUrl + endpoint + '/';
        final HttpResponse httpResponse;
        try {
            httpResponse = Request.Post(url)
                .addHeader(new BasicHeader("Content-Type", "text/plain"))
                .bodyString(payload, ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();
        } catch (final IOException e) {
            LOGGER.error(e.getMessage());
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
            return verifyResponse(statusCode, responseBody);
        }catch (final IOException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    /**
     * Parse and verify authentication response in JSON format.
     * Return a map object containing authentication status and exception messages.
     *
     * @param statusCode the response status code
     * @param responseBody the response body in JSON forat
     * @return a Map object
     */
    private Map<String, Object> verifyResponse(final int statusCode, final JSONObject responseBody) {
        final Map<String, Object> response = new HashMap<>();
        final Map<String, Object> attributesMap = new HashMap<>();
        try {
            if (statusCode == HttpStatus.SC_OK) {
                final String status = responseBody.getString("status");
                if ("REGISTRATION_SUCCESS".equals(status)) {
                    response.put("status", status);
                    return response;
                }
                if ("AUTHENTICATION_SUCCESS".equals(status)) {
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
                response.put("status", "AUTHENTICATION_FAILURE");
                response.put("detail", errorDetail);
                return response;
            } else {
                throw new IOException(String.format("INVALID_RESPONSE_SC_OTHER: status code = %d.", statusCode));
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
            response.put("status", "UNKNOWN");
            return response;
        }
    }
}
