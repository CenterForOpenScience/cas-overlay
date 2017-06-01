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

import io.cos.cas.web.util.AbstractFlowUtils;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Web Flow Action to Prepare Different Login Options.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class OpenScienceFrameworkLoginHandler {

    /**
     * Prepare Login Manager Context and Select the Default Login or Institution Login.
     *
     * @param requestContext The request requestContext
     * @return the event
     */
    public Event beforeLogin(final RequestContext requestContext) {

        final String serviceUrl = AbstractFlowUtils.getEncodedServiceUrl(requestContext);
        final String campaign = AbstractFlowUtils.getCampaignFromRegisteredService(requestContext);
        final boolean institutionLogin = AbstractFlowUtils.isInstitutionLogin(requestContext);

        LoginManager loginManagerContext = AbstractFlowUtils.getLoginManagerFromRequestContext(requestContext);
        if (loginManagerContext == null) {
            loginManagerContext = new LoginManager(serviceUrl, campaign, institutionLogin);
        } else {
            loginManagerContext.setServiceUrl(serviceUrl);
            loginManagerContext.setCampaign(serviceUrl);
            loginManagerContext.setInstitutionLogin(institutionLogin);
        }
        AbstractFlowUtils.putLoginManagerToRequestContext(requestContext, loginManagerContext);

        if (institutionLogin) {
            return new Event(this, "institutionLogin");
        }
        return new Event(this, "osfDefaultLogin");
    }
}
