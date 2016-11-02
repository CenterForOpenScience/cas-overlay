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
import org.springframework.util.comparator.BooleanComparator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;


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
        private boolean register;

        /**
         * Default Constructor.
         * Create an instance of OpenScienceFrameworkCampaign with CAS default login setting.
         */
        private OpenScienceFrameworkCampaign() {
            name = "OSF";
            titleLong = "Open&nbsp;Science&nbsp;Framework";
            titleShort = "OSF";
            registerUrl = "";
            institutionLogin = Boolean.FALSE;
            register = Boolean.FALSE;

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

        private void setRegister(final boolean register) {
            this.register = register;
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
         * Check if show sign up page instead of login page.
         *
         * @return true if register, false otherwise.
         */
        public boolean isRegister() {
            return register;
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
        } else if (osfCampaign.isRegister()) {
            if (osfCampaign.isOsfCampaign()) {
                return new Event(this, "osfCampaignRegister");
            }
            return new Event(this, "osfDefaultRegister");
        } else {
            if (osfCampaign.isOsfCampaign()) {
                return new Event(this, "osfCampaignLogin");
            }
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
                        "?campaign=prereg"
                );
            } else if (servicePath.startsWith("/erpc/") || "/erpc".equals(servicePath)) {
                osfCampaign.setOsfCampaign(
                        "erpc",
                        "Open&nbsp;Science&nbsp;Framework<br><br>Election&nbsp;Research&nbsp;Preacceptance&nbsp;Competition",
                        "OSF<br><br>Election&nbsp;Research<br>Preacceptance<br>Competition",
                        "?campaign=erpc"
                );
            } else if (servicePath.startsWith("/preprints/") || "/preprints".equals(servicePath)) {
                osfCampaign.setOsfCampaign(
                        "osf-preprints",
                        "OSF&nbsp;Preprints",
                        "OSF&nbsp;Preprints",
                        "?campaign=osf-preprints"
                );
            } else if ("/login/".equals(servicePath) || "/login".equals(servicePath)) {
                final String serviceQuery = serviceUrl.getQuery();
                URL nextUrl = null;
                if (serviceQuery != null) {
                    final Map<String, String> queryPairs = parseUrlQuery(serviceQuery);
                    try {
                        nextUrl = new URL(queryPairs.get("next"));
                    } catch (final MalformedURLException e) {
                        LOGGER.error(String.format("Malformed Service URL: %s", e.toString()));
                    }
                    if (nextUrl != null && nextUrl.getPath() != null) {
                        if (nextUrl.getPath().startsWith("/preprints/") || "/preprints".equals(nextUrl.getPath())) {
                            osfCampaign.setOsfCampaign(
                                    "osf-preprints",
                                    "OSF&nbsp;Preprints",
                                    "OSF&nbsp;Preprints",
                                    "?campaign=osf-preprints"
                            );
                        }
                    }
                }
            }
        }

        if (isInstitutionLogin(context)) {
            osfCampaign.setInstitutionLogin(Boolean.TRUE);
        }

        if (isRegister(context)) {
            osfCampaign.setRegister(Boolean.TRUE);
        }

        return osfCampaign;
    }

    /**
     * Parse the query string in URL and store the result as key-value pairs in a map.
     * Support query parameter with empty value. Does not support multiple parameter with the same key.
     *
     * @param query the query string
     * @return a map of query key-value pairs
     */
    private Map<String, String> parseUrlQuery(final String query) {
        final Map<String, String> queryPairs = new HashMap<>();
        for (final String pair: query.split("&")) {
            final int index = pair.indexOf('=');
            try {
                final String key = index > 0 ? URLDecoder.decode(pair.substring(0, index), "UTF-8") : pair;
                final String value = index > 0 && pair.length() > index + 1 ? URLDecoder.decode(pair.substring(index + 1), "UTF-8") : "";
                if (!queryPairs.containsKey(key)) {
                    queryPairs.put(key, value);
                }
            } catch (final UnsupportedEncodingException e){
                throw new AssertionError("UTF-8 is unknown");
            }
        }
        return queryPairs;
    }
}
