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
import java.util.*;

public class OpenScienceFrameworkApiCasEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkApiCasEndpoint.class);

    private static final int SIXTY_SECONDS = 60 * 1000;

    private static final Set<String> API_AUTHENTICATION_ERROR_LIST = new HashSet<>();

    static {
        API_AUTHENTICATION_ERROR_LIST.add("MISSING_CREDENTIALS");
        API_AUTHENTICATION_ERROR_LIST.add("ACCOUNT_NOT_FOUND");
        API_AUTHENTICATION_ERROR_LIST.add("INVALID_PASSWORD");
        API_AUTHENTICATION_ERROR_LIST.add("INVALID_VERIFICATION_KEY");
        API_AUTHENTICATION_ERROR_LIST.add("INVALID_ONE_TIME_PASSWORD");
        API_AUTHENTICATION_ERROR_LIST.add("TWO_FACTOR_AUTHENTICATION_REQUIRED");
    }

    @NotNull
    private String osfApiCasEndpointUrl;

    public OpenScienceFrameworkApiCasEndpoint() {}

    public OpenScienceFrameworkApiCasEndpoint(String osfApiCasEndpointUrl) {
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

    public Map<String, Object> apiCasAuthentication(final String endpoint, final String email, final String payload) {

        final String url = osfApiCasEndpointUrl + endpoint + '/';
        final HttpResponse httpResponse;
        try {
            httpResponse = Request.Post(url)
                .addHeader(new BasicHeader("Content-Type", "text/plain"))
                .bodyString(payload, ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();
        } catch (IOException e) {
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
            JSONObject responseBody =  new JSONObject(new BasicResponseHandler().handleEntity(httpResponse.getEntity()));
            return verifyResponse(statusCode, responseBody);
        }catch (IOException e) {
            return null;
        }
    }

    private Map<String, Object> verifyResponse(final int statusCode, final JSONObject responseBody) {
        final Map<String, Object> response = new HashMap<>();
        final Map<String, Object> attributesMap = new HashMap<>();
        try {
            if (statusCode == HttpStatus.SC_OK) {
                final String status = responseBody.getString("status");
                if (!"AUTHENTICATION_SUCCESS".equals(status)) {
                    throw new IOException(String.format("INVALID_RESPONSE_SC_OK: status = %s.", status));
                }
                response.put("status", status);
                response.put("userId", responseBody.getString("userId"));
                final JSONObject attributes = responseBody.getJSONObject("attributes");
                attributesMap.put("username", attributes.getString("username"));
                attributesMap.put("givenName", attributes.getString("givenName"));
                attributesMap.put("familyName", attributes.getString("familyName"));
                response.put("attributes", attributesMap);
                return response;
            } else if (statusCode == HttpStatus.SC_FORBIDDEN) {
                final JSONArray errorList = responseBody.getJSONArray("errors");
                if (errorList.length() != 1) {
                    throw new IOException(String.format("INVALID_RESPONSE_SC_FORBIDDEN: errorList.length() = %d.", errorList.length()));
                }
                final JSONObject error = errorList.getJSONObject(0);
                final String errorDetail = error.getString("detail");
                if (!API_AUTHENTICATION_ERROR_LIST.contains(errorDetail))
                    throw new IOException(String.format("INVALID_RESPONSE_SC_FORBIDDEN: status = %s.", errorDetail));
                response.put("status", "AUTHENTICATION_FAILURE");
                response.put("detail", errorDetail);
                return response;
            } else {
                throw new IOException(String.format("INVALID_RESPONSE_SC_OTHER: status code = %d.", statusCode));
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            response.put("status", "UNKNOWN");
            return response;
        }
    }
}
