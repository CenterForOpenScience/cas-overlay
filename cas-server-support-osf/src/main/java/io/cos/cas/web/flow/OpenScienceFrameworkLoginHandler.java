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

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * Open Science Framework Login Handler.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
public class OpenScienceFrameworkLoginHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkLoginHandler.class);

    /**
     * Open Science Framework Login Context.
     *
     * @author Longze chen
     * @since  4.1.0
     */
    public static final class OpenScienceFrameworkLoginContext {
        private String serviceUrl;
        private boolean institutionLogin;

        /**
         * Construct an instance of `OpenScienceFrameworkLoginContext` with given settings.
         *
         * @param serviceUrl the service url
         * @param institutionLogin login through institutions
         */
        private OpenScienceFrameworkLoginContext(final String serviceUrl, final boolean institutionLogin) {
            this.serviceUrl = serviceUrl;
            this.institutionLogin = institutionLogin;
        }

        public String getServiceUrl() {
            return serviceUrl;
        }

        /**
         * Check if login through institutions.
         *
         * @return true if institution login, false otherwise
         */
        public boolean isInstitutionLogin() {
            return institutionLogin;
        }

        /**
         * Check if service url exists.
         *
         * @return true if service url exists, false otherwise
         */
        public boolean isServiceUrl() {
            return serviceUrl != null;
        }


        /**
         * Convert class instance to a JSON string, which will be passed to the flow context.
         *
         * @return JSON string
         */
        private String toJson() {
            final Gson gson = new Gson();
            return gson.toJson(this);
        }

        /**
         * Convert JSON string to a class instance.
         *
         * @param jsonString The json String
         * @return an instance of OpenScienceFrameworkCampaign
         */
        public static OpenScienceFrameworkLoginContext fromJson(final String jsonString) {
            final Gson gson = new Gson();
            return gson.fromJson(jsonString, OpenScienceFrameworkLoginContext.class);
        }
    }

    /**
     * Decision making action that handles OSF login: osf, institution, campaign.
     *
     * @param context The request context
     * @return Event "osf", "institution" or "campaigns
     */
    public Event beforeLogin(final RequestContext context) {

        final String serviceUrl = getEncodedServiceUrl(context);
        final boolean institutionLogin = isInstitutionLogin(context);

        final OpenScienceFrameworkLoginContext osfLoginContext = new OpenScienceFrameworkLoginContext(serviceUrl, institutionLogin);
        context.getFlowScope().put("jsonLoginContext", osfLoginContext.toJson());

        if (osfLoginContext.isInstitutionLogin()) {
            return new Event(this, "institutionLogin");
        } else {
            return new Event(this, "osfDefaultLogin");
        }
    }

    /**
     * Check institution login.
     * Return true if `campaign=institution` is present in request parameters.
     *
     * @param context The request context
     * @return Boolean
     */
    private boolean isInstitutionLogin(final RequestContext context) {
        final String campaign = context.getRequestParameters().get("campaign");
        return "institution".equals(campaign);
    }

    /**
     * Encode the decoded service url if service url exists.
     *
     * @param context The request context
     * @return the encoded service url
     * @throws AssertionError
     */
    private String getEncodedServiceUrl(final RequestContext context) {

        final String serviceUrl = context.getRequestParameters().get("service");
        if (serviceUrl == null) {
            return null;
        }
        try {
            return URLEncoder.encode(serviceUrl, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unknown");
        }
    }
}
