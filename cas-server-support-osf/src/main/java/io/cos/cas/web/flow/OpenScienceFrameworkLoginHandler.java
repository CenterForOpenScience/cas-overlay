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
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredServiceProperty;
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
        private String campaign;
        private String handleErrorName;
        private boolean institutionLogin;
        private boolean register;

        /**
         * Construct an instance of `OpenScienceFrameworkLoginContext` with given settings.
         *
         * @param serviceUrl the service url
         * @param institutionLogin login through institutions
         * @param register show register page instead of login
         */
        private OpenScienceFrameworkLoginContext(
                final String serviceUrl,
                final boolean institutionLogin,
                final boolean register
        ) {
            this.serviceUrl = serviceUrl;
            this.campaign = null;
            this.handleErrorName = null;
            this.institutionLogin = institutionLogin;
            this.register = register;
        }

        public String getServiceUrl() {
            return serviceUrl;
        }

        public String getCampaign() {
            return campaign;
        }

        public String getHandleErrorName() {
            return handleErrorName;
        }

        private void setServiceUrl(final String serviceUrl) {
            this.serviceUrl = serviceUrl;
        }

        public void setCampaign(final String campaign) {
            this.campaign = campaign;
        }

        public void setHandleErrorName(final String handleErrorName) {
            this.handleErrorName =handleErrorName;
        }

        private void setInstitutionLogin(final Boolean institutionLogin) {
            this.institutionLogin = institutionLogin;
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
         * Check if show sign up page instead of login page.
         *
         * @return true if register, false otherwise.
         */
        public boolean isRegister() {
            return register;
        }

        /**
         * Convert class instance to a JSON string, which will be passed to the flow context.
         *
         * @return JSON string
         */
        public String toJson() {
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
     * Decision making action that handles OSF login.
     *
     * @param context The request context
     * @return Event "osfDefaultLogin", "institutionLogin"
     */
    public Event beforeLogin(final RequestContext context) {

        final String serviceUrl = getEncodedServiceUrl(context);
        final String campaign = getCampaignFromService(context);
        final boolean institutionLogin = isInstitutionLogin(context);
        final boolean register = isRegister(context);

        final String jsonLoginContext = (String) context.getFlowScope().get("jsonLoginContext");
        final OpenScienceFrameworkLoginContext osfLoginContext;

        if (jsonLoginContext == null) {
            // Create a new Login Context with service URL and institution flag
            osfLoginContext = new OpenScienceFrameworkLoginContext(serviceUrl, institutionLogin, register);
        } else {
            // Use current Login Context which contains information of Authentication Exceptions
            // Update and override the service URL and the institution flag
            osfLoginContext = OpenScienceFrameworkLoginContext.fromJson(jsonLoginContext);
            osfLoginContext.setServiceUrl(serviceUrl);
            osfLoginContext.setInstitutionLogin(institutionLogin);
        }
        osfLoginContext.setCampaign(campaign);
        context.getFlowScope().put("jsonLoginContext", osfLoginContext.toJson());

        if (osfLoginContext.isInstitutionLogin()) {
            return new Event(this, "institutionLogin");
        } else if (osfLoginContext.isRegister()) {
            return new Event(this, "osfDefaultRegister");
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
     * Check if show sign up page instead of login page.
     * Return true if `register=true` is present in request parameters.
     *
     * @param context The request context
     * @return Boolean
     */
    private boolean isRegister(final RequestContext context) {
        final String register = context.getRequestParameters().get("register");
        return "true".equals(register);
    }


    /**
     * Encode the decoded service url if service url exists.
     *
     * @param context The request context

     * @return the encoded service url
     * @throws AssertionError if fails to encode the URL
     */
    private String getEncodedServiceUrl(final RequestContext context) throws AssertionError {

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

    /**
     * Get campaign name from service.
     *
     * @param context The request context
     * @return The campaign name
     */
    private String getCampaignFromService(final RequestContext context) {
        final RegexRegisteredService registeredService = (RegexRegisteredService) context.getFlowScope().get("registeredService");
        if (registeredService != null) {
            final RegisteredServiceProperty campaign = registeredService.getProperties().get("campaign");
            if (campaign != null) {
                campaign.getValue();
            }
        }
        return null;
    }
}
