/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.jasig.cas.support.shibboleth.authentication.principal;

import org.jasig.cas.authentication.principal.AbstractWebApplicationService;
import org.jasig.cas.authentication.principal.DefaultResponse;
import org.jasig.cas.authentication.principal.Response;
//import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.jasig.cas.support.shibboleth.ShibbolethConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to represent that this service wants to use SAML. We use this in
 * combination with the CentralAuthenticationServiceImpl to choose the right
 * UniqueTicketIdGenerator.
 *
 * @author Michael Haselton
 * @since 4.1.1
 */
public final class ShibbolethService extends AbstractWebApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShibbolethService.class);

    /**
     * Unique Id for serialization.
     */
    private static final long serialVersionUID = -6867572626767140223L;

    private static final String CONST_START_ARTIFACT_XML_TAG_NO_NAMESPACE = "<AssertionArtifact>";

    private static final String CONST_END_ARTIFACT_XML_TAG_NO_NAMESPACE = "</AssertionArtifact>";

    private static final String CONST_START_ARTIFACT_XML_TAG = "<samlp:AssertionArtifact>";

    private static final String CONST_END_ARTIFACT_XML_TAG = "</samlp:AssertionArtifact>";

    private static final int CONST_REQUEST_ID_LENGTH = 11;

    private String requestId;

    /**
     * Instantiates a new SAML service.
     *
     * @param id the service id
     */
    protected ShibbolethService(final String id) {
        super(id, id, null);
    }

    /**
     * Instantiates a new SAML service.
     *
     * @param id the service id
     * @param originalUrl the original url
     * @param artifactId the artifact id
     * @param requestId the request id
     */
    protected ShibbolethService(final String id, final String originalUrl,
                          final String artifactId, final String requestId) {
        super(id, originalUrl, artifactId);
        this.requestId = requestId;
    }

    public String getRequestID() {
        return this.requestId;
    }

    /**
     * Creates the SAML service from the request.
     *
     * @param request the request
     * @return the SAML service
     */
    public static ShibbolethService createServiceFrom(
            final HttpServletRequest request) {
//        final String service = request.getParameter("test");
//        final String artifactId;
//        final String requestBody = getRequestBody(request);
//        final String requestId;

//        if (!StringUtils.hasText(service) && !StringUtils.hasText(requestBody)) {
////            LOGGER.debug("Request does not specify a {} or request body is empty",
////                    SamlProtocolConstants.CONST_PARAM_TARGET);
//            return null;
//        }
//
//        final String id = cleanupUrl(service);
//
//        if (StringUtils.hasText(requestBody)) {
//
//            final String tagStart;
//            final String tagEnd;
//            if (requestBody.contains(CONST_START_ARTIFACT_XML_TAG)) {
//                tagStart = CONST_START_ARTIFACT_XML_TAG;
//                tagEnd = CONST_END_ARTIFACT_XML_TAG;
//            } else {
//                tagStart = CONST_START_ARTIFACT_XML_TAG_NO_NAMESPACE;
//                tagEnd = CONST_END_ARTIFACT_XML_TAG_NO_NAMESPACE;
//            }
//            final int startTagLocation = requestBody.indexOf(tagStart);
//            final int artifactStartLocation = startTagLocation + tagStart.length();
//            final int endTagLocation = requestBody.indexOf(tagEnd);
//
//            artifactId = requestBody.substring(artifactStartLocation, endTagLocation).trim();
//
//            // is there a request id?
//            requestId = extractRequestId(requestBody);
//        } else {
//            artifactId = null;
//            requestId = null;
//        }
//
//        LOGGER.debug("Attempted to extract Request from HttpServletRequest. Results:");
//        LOGGER.debug("Request Body: {}", requestBody);
//        LOGGER.debug("Extracted ArtifactId: {}", artifactId);
//        LOGGER.debug("Extracted Request Id: {}", requestId);

        Enumeration en = request.getHeaderNames();
        while (en.hasMoreElements()) {
            final String headerName = (String) en.nextElement();
            LOGGER.info("Header: {} : {}", headerName, request.getHeader(headerName));
        }

        for (final String headerName : Collections.list(request.getHeaderNames())) {
            final String attributePrefix = "SAML-";
//            if (headerName.startsWith(attributePrefix)) {
            LOGGER.info("Header: {} : {}", headerName, request.getHeader(headerName));
//            }
        }

        return new ShibbolethService("id", "originalUrl", "artifactId", "requestId");
    }

    @Override
    public Response getResponse(final String ticketId) {
        final Map<String, String> parameters = new HashMap<>();
//        parameters.put(SamlProtocolConstants.CONST_PARAM_ARTIFACT, ticketId);
        return DefaultResponse.getRedirectResponse(getOriginalUrl(), parameters);
    }

    /**
     * Extract request id from the body.
     *
     * @param requestBody the request body
     * @return the string
     */
    protected static String extractRequestId(final String requestBody) {
        if (!requestBody.contains("RequestID")) {
            LOGGER.debug("Request body does not contain a request id");
            return null;
        }

        try {
            final int position = requestBody.indexOf("RequestID=\"") + CONST_REQUEST_ID_LENGTH;
            final int nextPosition = requestBody.indexOf('"', position);

            return requestBody.substring(position,  nextPosition);
        } catch (final Exception e) {
            LOGGER.debug("Exception parsing RequestID from request.", e);
            return null;
        }
    }

    /**
     * Gets the request body from the request.
     *
     * @param request the request
     * @return the request body
     */
    protected static String getRequestBody(final HttpServletRequest request) {
        final StringBuilder builder = new StringBuilder();
        try (final BufferedReader reader = request.getReader()) {

            if (reader == null) {
                LOGGER.debug("Request body could not be read because it's empty.");
                return null;
            }
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (final Exception e) {
            LOGGER.trace("Could not obtain the saml request body from the http request", e);
            return null;
        }
    }
}