/*
 * Copyright (c) 2016. Center for Open Science
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
package io.cos.cas.web.flow;

import com.google.gson.Gson;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Open Science Framework Login Handler.
 *
 * @author Longze Chen
 * @author Fitzhugh Elliott
 * @since 19.3.0
 */
public class OpenScienceFrameworkLoginHandler {

    /**
     * Open Science Framework Login Context.
     *
     * @author Longze chen
     * @since  4.1.5
     */
    public static final class OpenScienceFrameworkLoginContext {

        /** The service URL in the context. */
        private String serviceUrl;

        /** The simple name of the authentication exception that just happened. */
        private String handleErrorName;

        /** The flag for institution login instead of normal OSF login. */
        private boolean institutionLogin;

        /** The flag for institution login instead of normal OSF login. */
        private boolean unsupportedInstitutionLogin;

        /** The OSF institution ID for an auto-selected institution. */
        private String institutionId;

        /** The flag for redirect to ORCiD login instead of normal OSF login. */
        private boolean orcidRedirect;

        /**
         * Construct an instance with the service URL, the institution login and ORCiD redirect flags.
         *
         * @param serviceUrl the service URL
         * @param institutionLogin the flag for institution login
         * @param institutionId the auto-selected institution ID
         * @param unsupportedInstitutionLogin the flag for unsupported institution login
         * @param orcidRedirect the flag for ORCiD redirect
         */
        private OpenScienceFrameworkLoginContext(
                final String serviceUrl,
                final boolean institutionLogin,
                final String institutionId,
                final boolean unsupportedInstitutionLogin,
                final boolean orcidRedirect
        ) {
            this.serviceUrl = serviceUrl;
            this.handleErrorName = null;
            this.institutionLogin = institutionLogin;
            this.institutionId = institutionId;
            this.unsupportedInstitutionLogin = unsupportedInstitutionLogin;
            this.orcidRedirect = orcidRedirect;
        }

        // Must be public to be accessible in the JSP page
        public String getServiceUrl() {
            return serviceUrl;
        }

        void setServiceUrl(final String serviceUrl) {
            this.serviceUrl = serviceUrl;
        }

        public String getHandleErrorName() {
            return handleErrorName;
        }

        public void setHandleErrorName(final String handleErrorName) {
            this.handleErrorName =handleErrorName;
        }

        // Must be public to be accessible in the JSP page
        public boolean isInstitutionLogin() {
            return institutionLogin;
        }

        void setInstitutionLogin(final boolean institutionLogin) {
            this.institutionLogin = institutionLogin;
        }

        // Must be public to be accessible in the JSP page
        public String getInstitutionId() {
            return institutionId;
        }

        void setInstitutionId(final String institutionId) {
            this.institutionId = institutionId;
        }

        public boolean isUnsupportedInstitutionLogin() {
            return unsupportedInstitutionLogin;
        }

        void setUnsupportedInstitutionLogin(final boolean unsupportedInstitutionLogin) {
            this.unsupportedInstitutionLogin = unsupportedInstitutionLogin;
        }

        boolean isOrcidRedirect() {
            return orcidRedirect;
        }

        void setOrcidRedirect(final boolean orcidRedirect) {
            this.orcidRedirect = orcidRedirect;
        }

        /**
         * Check if the service URL exists. Must be public to be accessible in the JSP page.
         *
         * @return true if service url exists, false otherwise
         */
        public boolean isServiceUrl() {
            return serviceUrl != null;
        }

        /**
         * Convert the class instance to a JSON string, which will be passed to the flow context.
         *
         * @return JSON string
         */
        public String toJson() {
            final Gson gson = new Gson();
            return gson.toJson(this);
        }

        /**
         * Convert the JSON string to a class instance.
         *
         * @param jsonString a JSON string previously generated by the method .toJson()
         * @return an reconstructed instance of OpenScienceFrameworkLoginContext
         */
        public static OpenScienceFrameworkLoginContext fromJson(final String jsonString) {
            final Gson gson = new Gson();
            return gson.fromJson(jsonString, OpenScienceFrameworkLoginContext.class);
        }
    }

    /**
     * Prepare for the login request and make decisions for next steps.
     *
     * @param context the request context
     * @return the next event to go to
     */
    public Event beforeLogin(final RequestContext context) {

        final OpenScienceFrameworkLoginContext osfLoginContext;
        final String serviceUrl = getEncodedServiceUrlFromRequestContext(context);
        final boolean institutionLogin = isInstitutionLogin(context);
        final String institutionId = getInstitutionIdFromRequestContext(context);
        final boolean unsupportedInstitutionLogin = isUnsupportedInstitutionLogin(context);
        final boolean orcidRedirect = checkOrcidRedirectFromRequestContext(context);

        String jsonLoginContext = (String) context.getFlowScope().get("jsonLoginContext");
        if (jsonLoginContext == null) {
            // Create a new login context with service URL, institution login and ORCiD redirect flags
            osfLoginContext = new OpenScienceFrameworkLoginContext(
                    serviceUrl,
                    institutionLogin,
                    institutionId,
                    unsupportedInstitutionLogin,
                    orcidRedirect
            );
        } else {
            // If the login context already exists, update the service URL and the institution login flag while keeping
            // the errors and disabling ORCiD login redirect
            osfLoginContext = OpenScienceFrameworkLoginContext.fromJson(jsonLoginContext);
            osfLoginContext.setServiceUrl(serviceUrl);
            osfLoginContext.setInstitutionLogin(institutionLogin);
            osfLoginContext.setInstitutionId(institutionId);
            osfLoginContext.setUnsupportedInstitutionLogin(unsupportedInstitutionLogin);
            // Only allow ORCiD login redirect from a brand new login flow
            osfLoginContext.setOrcidRedirect(false);
        }
        jsonLoginContext = osfLoginContext.toJson();
        context.getFlowScope().put("jsonLoginContext", jsonLoginContext);
        context.getFlowScope().put("casViewErrorCode", "0000");

        // Go to the institution login page. Note: the institution login flag rules over the ORCiD redirect flag
        if (osfLoginContext.isInstitutionLogin()) {
            return new Event(this, "institutionLogin");
        }
        // Go to the unsupported institution login page.
        if (osfLoginContext.isUnsupportedInstitutionLogin()) {
            return new Event(this, "unsupportedInstitutionLogin");
        }
        // Go to the dedicated redirect view for ORCiD login
        if (osfLoginContext.isOrcidRedirect()) {
            return new Event(this, "orcidLoginRedirect");
        }
        // Go to the default username/ password login page
        return new Event(this, "osfDefaultLogin");
    }

    /**
     * Check if the request is institution login.
     *
     * @param context the request context
     * @return true if `campaign=institution` is present in the request parameters
     */
    private boolean isInstitutionLogin(final RequestContext context) {
        final String campaign = context.getRequestParameters().get("campaign");
        return campaign != null && "institution".equals(campaign.toLowerCase());
    }

    /**
     * Obtain the institution ID in the request parameters for auto institution selection.
     *
     * @param context the request context
     * @return the institution ID
     */
    private String getInstitutionIdFromRequestContext(final RequestContext context) {
        final String institutionId = context.getRequestParameters().get("institutionId");
        return (institutionId == null || institutionId.isEmpty()) ? null : institutionId;
    }

    /**
     * Check if the request is unsupported institution login.
     *
     * @param context the request context
     * @return true if `campaign=unsupported-institution` is present in the request parameters
     */
    private boolean isUnsupportedInstitutionLogin(final RequestContext context) {
        final String campaign = context.getRequestParameters().get("campaign");
        return campaign != null && "unsupportedinstitution".equals(campaign.toLowerCase());
    }

    /**
     * Check if the request is ORCiD login redirect.
     *
     * @param context the request context
     * @return true if `redirectOrcid=true` is present in the request parameters
     */
    private boolean checkOrcidRedirectFromRequestContext(final RequestContext context) {
        final String orcidRedirect = context.getRequestParameters().get("redirectOrcid");
        return orcidRedirect != null && "true".equals(orcidRedirect.toLowerCase());
    }

    /**
     * Obtain the service URL in the request parameters.
     *
     * The service URL from the request context is decoded (one level from what is originally in the request URL). Must
     * encode it before sending it to the page via the login context. If the service URL doesn't exist, return `null`.
     *
     * @param context the request context
     * @return the encoded service url
     * @throws AssertionError if encoding fails
     */
    private String getEncodedServiceUrlFromRequestContext(final RequestContext context) throws AssertionError {
        final String serviceUrl = context.getRequestParameters().get("service");
        if (serviceUrl == null || serviceUrl.isEmpty()) {
            return null;
        }
        try {
            return URLEncoder.encode(serviceUrl, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unknown");
        }
    }
}
