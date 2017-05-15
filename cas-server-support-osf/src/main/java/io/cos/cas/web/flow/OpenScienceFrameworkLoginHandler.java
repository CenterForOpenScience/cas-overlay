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
import io.cos.cas.authentication.exceptions.ShouldNotHappenException;
import io.cos.cas.types.OsfLoginAction;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredServiceProperty;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;


/**
 * Open Science Framework Login Handler.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class OpenScienceFrameworkLoginHandler {

    private static final String PARAM_HELP = "help";
    private static final String PARAM_CHALLENGE = "challenge";
    private static final String PARAM_REGISTER = "register";
    private static final String PARAM_CAMPAIGN = "campaign";
    private static final String PARAM_INSTITUTION = "institution";
    private static final String PARAM_USER = "user";

    /**
     * Open Science Framework Login Context.
     *
     * @author Longze chen
     * @since  4.1.5
     */
    public static final class OpenScienceFrameworkLoginContext {

        private String serviceUrl;
        private String action;
        private String fullname;
        private String username;
        private String campaign;
        private boolean institutionLogin;
        private String handleErrorName;

        /**
         * Instantiate an Open Science Framework Login Context with Service URL, Institution Login and Register Flags.
         *
         * @param serviceUrl the service url
         * @param action the login action
         * @param institutionLogin login through institutions
         */
        private OpenScienceFrameworkLoginContext(
                final String serviceUrl,
                final String action,
                final boolean institutionLogin
        ) {
            this.serviceUrl = serviceUrl;
            this.action = action;
            this.username = null;
            this.fullname = null;
            this.campaign = null;
            this.handleErrorName = null;
            this.institutionLogin = institutionLogin;
        }

        public String getServiceUrl() {
            return serviceUrl;
        }

        public String getAction() {
            return action;
        }

        public String getUsername() {
            return username;
        }

        public String getFullname() {
            return fullname;
        }

        public String getCampaign() {
            return campaign;
        }

        public String getHandleErrorName() {
            return handleErrorName;
        }

        public void setServiceUrl(final String serviceUrl) {
            this.serviceUrl = serviceUrl;
        }

        public void setAction(final String action) {
            this.action = action;
        }

        public void setUsername(final String username) {
            this.username = username;
        }

        public void setFullname(final String fullname) {
            this.fullname = fullname;
        }

        public void setCampaign(final String campaign) {
            this.campaign = campaign;
        }

        public void setInstitutionLogin(final Boolean institutionLogin) {
            this.institutionLogin = institutionLogin;
        }

        public void setHandleErrorName(final String handleErrorName) {
            this.handleErrorName = handleErrorName;
        }


        /**
         * @return true if action is default login.
         */
        public Boolean isDefaultLogin() {
            return OsfLoginAction.isLogin(action);
        }

        /**
         * @return true if action is register.
         */
        public Boolean isRegister() {
            return OsfLoginAction.isRegister(action);
        }

        /**
         * @return true if action is login help.
         */
        public Boolean isLoginHelp() {
            return OsfLoginAction.isHelp(action);
        }

        /**
         * @return true if action is login help.
         */
        public Boolean isLoginChallenge() {
            return OsfLoginAction.isChallenge(action);
        }

        /**
         * @return true if institution login.
         */
        public boolean isInstitutionLogin() {
            return institutionLogin;
        }

        /**
         * @return true if service url exists.
         */
        public boolean isServiceUrl() {
            return serviceUrl != null;
        }

        /**
         * Serialize the class instance to a JSON string, which will be passed to the flow context.
         *
         * @return JSON string
         */
        public String toJson() {
            final Gson gson = new Gson();
            return gson.toJson(this);
        }

        /**
         * Construct an instance from the JSON string obtained from flow context.
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
     * @throws GeneralSecurityException on authentication failure
     */
    public Event beforeLogin(final RequestContext context) throws GeneralSecurityException {

        final String serviceUrl = getEncodedServiceUrl(context);
        final OsfLoginAction loginHelp = getLoginHelpFromRequest(context);
        final OsfLoginAction loginOrRegister = getLoginOrRegisterFromRequest(context);
        final String campaign = getCampaignFromRegisteredService(context);
        final boolean institutionLogin = isInstitutionLogin(context);

        if (loginHelp != null) {
            final OpenScienceFrameworkLoginContext osfLoginContext
                    = new OpenScienceFrameworkLoginContext(serviceUrl, loginHelp.getId(), false);
            context.getFlowScope().put("jsonLoginContext", osfLoginContext.toJson());
            return new Event(this, "osfLoginHelp");
        }

        if (loginOrRegister != null) {
            final OpenScienceFrameworkLoginContext osfLoginContext;
            final String jsonLoginContext = (String) context.getFlowScope().get("jsonLoginContext");
            if (jsonLoginContext == null) {
                osfLoginContext = new OpenScienceFrameworkLoginContext(serviceUrl, loginOrRegister.getId(), institutionLogin);
            } else {
                // If the web flow comes from authentication exception, the login context exists and contains error
                // information stored in "handlerErrorName" which is set during exception handler.
                osfLoginContext = OpenScienceFrameworkLoginContext.fromJson(jsonLoginContext);
                osfLoginContext.setServiceUrl(serviceUrl);
                osfLoginContext.setAction(loginOrRegister.getId());
                osfLoginContext.setInstitutionLogin(institutionLogin);
            }
            osfLoginContext.setCampaign(campaign);
            context.getFlowScope().put("jsonLoginContext", osfLoginContext.toJson());

            if (institutionLogin) {
                return new Event(this, "institutionLogin");
            }

            if (OsfLoginAction.isRegister(loginOrRegister.getId())) {
                return new Event(this, "osfDefaultRegister");
            }

            if (OsfLoginAction.isLogin(loginOrRegister.getId())) {
                return new Event(this, "osfDefaultLogin");
            }
        }

        throw new ShouldNotHappenException();
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
     * Check and verify request parameter "institution" (and "campaign" for backward compatibility).
     *
     * @param context the request context
     * @return true if "institution=true" or "campaign=institution"
     */
    private boolean isInstitutionLogin(final RequestContext context) {
        final String campaign = context.getRequestParameters().get(PARAM_CAMPAIGN);
        final String institution = context.getRequestParameters().get(PARAM_INSTITUTION);
        return Boolean.TRUE.toString().equalsIgnoreCase(institution) || PARAM_INSTITUTION.equals(campaign);
    }

    /**
     * Get campaign name from registered service.
     *
     * @param context the request context
     * @return the campaign name
     */
    private String getCampaignFromRegisteredService(final RequestContext context) {
        final RegexRegisteredService registeredService = (RegexRegisteredService) context.getFlowScope().get("registeredService");
        if (registeredService != null) {
            final RegisteredServiceProperty campaign = registeredService.getProperties().get("campaign");
            if (campaign != null) {
                return campaign.getValue();
            }
        }
        return null;
    }

    /**
     * Check and verify request parameter "help".
     *
     * @param context the request context
     * @return the action if the request has a valid help parameter
     */
    private OsfLoginAction getLoginHelpFromRequest(final RequestContext context) {
        final String help = context.getRequestParameters().get(PARAM_HELP);
        if (OsfLoginAction.isHelp(help)) {
            return OsfLoginAction.getType(help);
        }
        return null;
    }

    /**
     * Check and verify request parameter "register".
     *
     * @param context the request context
     * @return action "login" or "register"
     */
    private OsfLoginAction getLoginOrRegisterFromRequest(final RequestContext context) {
        final String register = context.getRequestParameters().get(PARAM_REGISTER);
        if (Boolean.TRUE.toString().equalsIgnoreCase(register)) {
            return OsfLoginAction.REGISTER;
        }
        return OsfLoginAction.LOGIN;
    }
}
