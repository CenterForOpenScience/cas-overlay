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

import java.net.MalformedURLException;
import java.net.URL;


/**
 * Open Science Framework Login Handler.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
public class OpenScienceFrameworkLoginHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkLoginHandler.class);

    /**
     * Open Science Framework Campaign.
     *
     * @author Longze chen
     * @since  4.1.0
     */
    public static final class OpenScienceFrameworkCampaign {

        private String name;
        private String titleLong;
        private String titleShort;
        private String registerUrl;
        private boolean institutionLogin;

        /**
         * Default Constructor.
         * Create an instance of OpenScienceFrameworkCampaign with CAS default login setting.
         */
        private OpenScienceFrameworkCampaign() {
            name = "OSF";
            titleLong = "Open&nbsp;Science&nbsp;Framework";
            titleShort = "OSF";
            registerUrl = "";
            institutionLogin = false;
        }

        /**
         * Set campaign information.
         *
         * @param name Campaign name
         * @param titleLong Full campaign title to display
         * @param titleShort Short campaign title to display on small/mobile screens
         * @param registerUrl Campaign register URL for "Create Account"
         */
        private void setOsfCampaign(
                final String name,
                final String titleLong,
                final String titleShort,
                final String registerUrl) {
            this.name = name;
            this.titleLong = titleLong;
            this.titleShort = titleShort;
            this.registerUrl = registerUrl;
        }

        public String getTitleLong() {
            return titleLong;
        }

        public String getTitleShort() {
            return titleShort;
        }

        public String getRegisterUrl() {
            return registerUrl;
        }

        private void setInstitutionLogin(final boolean institutionLogin) {
            this.institutionLogin = institutionLogin;
        }

        /**
         * Check OSF campaign information.
         *
         * @return true if OSF campaign login, false otherwise.
         */
        private boolean isOsfCampaign() {
            return !"OSF".equals(name);
        }

        /**
         * Check OSF institution information.
         *
         * @return true if institution login, false otherwise.
         */
        public boolean isInstitutionLogin() {
            return institutionLogin;
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
        public static OpenScienceFrameworkCampaign fromJson(final String jsonString) {
            final Gson gson = new Gson();
            return gson.fromJson(jsonString, OpenScienceFrameworkCampaign.class);
        }
    }

    /**
     * Decision making action that handles OSF login: osf, institution, campaign.
     *
     * @param context The request context
     * @return Event "osf", "institution" or "campaigns
     */
    public Event beforeLogin(final RequestContext context) {

        final OpenScienceFrameworkCampaign osfCampaign = getOsfCampaigns(context);
        context.getFlowScope().put("campaign", osfCampaign.toJson());

        if (osfCampaign.isInstitutionLogin()) {
            return new Event(this, "institutionLogin");
        } else if (osfCampaign.isOsfCampaign()) {
            return new Event(this, "osfCampaignLogin");
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
     * Check login request for osf, osf campaign and institution login. Return an object containing necessary
     * information which will be passed to the flow context and the front end page.
     *
     * @param context The request context
     * @return an instance of OpenScienceFrameworkCampaign
     */
    private OpenScienceFrameworkCampaign getOsfCampaigns(final RequestContext context) {

        final String service = context.getRequestParameters().get("service");
        final OpenScienceFrameworkCampaign osfCampaign = new OpenScienceFrameworkCampaign();
        URL serviceUrl = null;

        try {
            serviceUrl = new URL(service);
        } catch (final MalformedURLException e) {
            LOGGER.error(String.format("Malformed Service URL: %s", e.toString()));
        }

        if (serviceUrl != null) {
            final String servicePath = serviceUrl.getPath();
            if (servicePath.startsWith("/prereg/") || "/prereg".equals(servicePath)) {
                osfCampaign.setOsfCampaign(
                        "prereg",
                        "OSF&nbsp;Preregistration&nbsp;Challenge",
                        "OSF<br><br>Preregistration<br>Challenge",
                        "/register/?campaign=prereg"
                );
            } else if (servicePath.startsWith("/erpc/") || "/erpc".equals(servicePath)) {
                osfCampaign.setOsfCampaign(
                        "erpc",
                        "Open&nbsp;Science&nbsp;Framework<br><br>Election&nbsp;Research&nbsp;Preacceptance&nbsp;Competition",
                        "OSF<br><br>Election&nbsp;Research<br>Preacceptance<br>Competition",
                        "/register/?campaign=erpc"
                );
            } else if (servicePath.startsWith("/preprints/") || "/preprints".equals(servicePath)) {
                osfCampaign.setOsfCampaign(
                        "osf-preprints",
                        "OSF&nbsp;Preprints",
                        "OSF&nbsp;Preprints",
                        "/register/?campaign=osf-preprints"
                );
            }
        }

        if (isInstitutionLogin(context)) {
            osfCampaign.setInstitutionLogin(true);
        }

        return osfCampaign;
    }
}
