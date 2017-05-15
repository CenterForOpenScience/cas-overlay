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

import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredServiceProperty;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * Web Flow Action to Prepare Different Login Options.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class OpenScienceFrameworkLoginHandler {

    private static final String PARAM_CAMPAIGN = "campaign";
    private static final String PARAM_INSTITUTION = "institution";

    /**
     * Prepare Login Manager Context and Select the Default Login or Institution Login.
     *
     * @param requestContext The request requestContext
     * @return the event
     */
    public Event beforeLogin(final RequestContext requestContext) {

        final String serviceUrl = getEncodedServiceUrl(requestContext);
        final String campaign = getCampaignFromRegisteredService(requestContext);
        final boolean institutionLogin = isInstitutionLogin(requestContext);

        LoginManager loginManagerContext = getLoginManagerFromRequestContext(requestContext);
        if (loginManagerContext == null) {
            loginManagerContext = new LoginManager(serviceUrl, campaign, institutionLogin);
        } else {
            loginManagerContext.setServiceUrl(serviceUrl);
            loginManagerContext.setCampaign(serviceUrl);
            loginManagerContext.setInstitutionLogin(institutionLogin);
        }
        putLoginManagerToRequestContext(requestContext, loginManagerContext);

        if (institutionLogin) {
            return new Event(this, "institutionLogin");
        }
        return new Event(this, "osfDefaultLogin");
    }

    /**
     * Get and Encode the Service URL from Request Context.
     *
     * @param context The request context

     * @return the encoded service url
     * @throws AssertionError if fails to encode the URL
     */
    private String getEncodedServiceUrl(final RequestContext context) throws AssertionError {
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
     * Get the OSF Campaign Name from the Registered Service.
     *
     * @param context the request context
     * @return the campaign name
     */
    private String getCampaignFromRegisteredService(final RequestContext context) {
        final RegexRegisteredService registeredService = (RegexRegisteredService) context.getFlowScope().get("registeredService");
        if (registeredService != null) {
            final RegisteredServiceProperty campaign = registeredService.getProperties().get("campaign");
            if (campaign != null && !campaign.getValue().isEmpty()) {
                return campaign.getValue();
            }
        }
        return null;
    }

    /**
     * Get the Login Manager from Flow Scope in Request Context.
     *
     * @param context the request context
     * @return the login manager
     */
    public static LoginManager getLoginManagerFromRequestContext(final RequestContext context) {
        return LoginManager.fromJson((String) context.getFlowScope().get(LoginManager.ATTRIBUTE_NAME));
    }

    /**
     * Put the Login Manager to Flow Scope in Request Context.
     *
     * @param context the request context
     * @param manager the login manager
     */
    public static void putLoginManagerToRequestContext(final RequestContext context, final LoginManager manager) {
        context.getFlowScope().put(LoginManager.ATTRIBUTE_NAME, manager.toJson());
    }
}
