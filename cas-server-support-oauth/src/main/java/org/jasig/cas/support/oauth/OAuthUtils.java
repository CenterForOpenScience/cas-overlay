/*
 * Copyright (c) 2015. Center for Open Science
 *
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
package org.jasig.cas.support.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * A helpful utility class for the OAuth 2.0 implementation.
 *
 * This class has some useful methods including outputting data in plain text, handling redirects, add parameter to a
 * url and find the right provider (i.e. registered service).
 *
 * @author Jerome Leleu
 * @author Longze Chen
 * @since 3.5.0
 */
public final class OAuthUtils {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthUtils.class);

    /**
     * Instantiates a new {@link OAuthUtils}.
     */
    private OAuthUtils() {}

    /**
     * Write to the output a given error message as JSON and return a null view.
     *
     * @param response the http response
     * @param error the error message
     * @param description the error description
     * @param status the status code
     * @return a null view
     */
    public static ModelAndView writeJsonError(
            final HttpServletResponse response,
            final String error,
            final String description,
            final int status
    ) {
        final Map<String, String> map = new HashMap<>();
        map.put("error", error);
        if (description != null) {
            map.put("error_description", description);
        }
        try {
            response.setContentType("application/json");
            return writeText(response, new ObjectMapper().writeValueAsString(map), status);
        } catch (final JsonProcessingException e) {
            LOGGER.error("Failed to write the JSON error to response", e);
        }
        return null;
    }

    /**
     * Write to the output a given error text and return a null view.
     *
     * @param response the http response
     * @param error the error message
     * @param status the status code
     * @return a null view
     */
    public static ModelAndView writeTextError(
            final HttpServletResponse response,
            final String error, final int status
    ) {
        response.setContentType("text/plain");
        return OAuthUtils.writeText(response, "error=" + error, status);
    }

    /**
     * Write to the output a given text and return a null view.
     *
     * @param response the http response
     * @param text the output text
     * @param status the status code
     * @return a null view
     */
    public static ModelAndView writeText(final HttpServletResponse response, final String text, final int status) {
        try (PrintWriter printWriter = response.getWriter()) {
            response.setStatus(status);
            printWriter.print(text);
        } catch (final IOException e) {
            LOGGER.error("Failed to write the text to response", e);
        }
        return null;
    }

    /**
     * Return a view which is a redirection to an url with an error parameter.
     *
     * @param url the redirect url
     * @param error the error message
     * @return a view which is a redirection to an url with an error parameter
     */
    public static ModelAndView redirectToError(final String url, final String error) {
        String useUrl = url;
        if (StringUtils.isBlank(useUrl)) {
            useUrl = "/";
        }
        return OAuthUtils.redirectTo(OAuthUtils.addParameter(useUrl, "error", error));
    }

    /**
     * Return a view which is a redirection to an url.
     *
     * @param url the redirect url
     * @return a view which is a redirection to an url
     */
    public static ModelAndView redirectTo(final String url) {
        return new ModelAndView(new RedirectView(url));
    }

    /**
     * Add a parameter with given name and value to an url.
     *
     * @param url the url to which the parameter will be added
     * @param name the name of the parameter
     * @param value the value of the parameter
     * @return the updated url with the parameter
     */
    public static String addParameter(final String url, final String name, final String value) {
        final StringBuilder sb = new StringBuilder();
        sb.append(url);
        if (url.indexOf('?') >= 0) {
            sb.append('&');
        } else {
            sb.append('?');
        }
        sb.append(name);
        sb.append('=');
        if (value != null) {
            try {
                sb.append(URLEncoder.encode(value, "UTF-8"));
            } catch (final UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return sb.toString();
    }

    /**
     * Locate the requested instance of {@link OAuthRegisteredService} by the given client id.
     *
     * @param servicesManager the service manager
     * @param clientId the client id by which the {@link OAuthRegisteredService} is to be located
     * @return null, or the located {@link OAuthRegisteredService} instance in the service registry
     */
    public static OAuthRegisteredService getRegisteredOAuthService(
            final ServicesManager servicesManager,
            final String clientId
    ) {
        for (final RegisteredService registeredService : servicesManager.getAllServices()) {
            if (registeredService instanceof OAuthRegisteredService) {
                final OAuthRegisteredService oAuthRegisteredService = (OAuthRegisteredService) registeredService;
                if (oAuthRegisteredService.getClientId().equals(clientId)) {
                    return oAuthRegisteredService;
                }
            }
        }
        return null;
    }
}
