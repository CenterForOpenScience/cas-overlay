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


import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;


/**
 * Open Science Framework Login Handler.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
public class OpenScienceFrameworkLoginHandler {

    /**
     * Decision making action that handles OSF login: osf, institution, campaign.
     *
     * @param context The request context
     * @return Event "osf", "institution" or "campaigns
     */
    public Event beforeLogin(final RequestContext context) {

        return isInstitution(context) ? new Event(this, "institution") : new Event(this, "osf");
    }

    /**
     * Check institution login. Return true if `campaign=institution` is present in request parameters.
     *
     * @param context The request context
     * @return Boolean
     */
    private boolean isInstitution(final RequestContext context) {
        final String campaign = context.getRequestParameters().get("campaign");
        return "institution".equals(campaign);
    }
}
