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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Open Science Framework Login View.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
public class OpenScienceFrameworkLoginViews {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkLoginViews.class);

    /** Default Constructor. */
    public OpenScienceFrameworkLoginViews() {}

    /**
     * Check the campaign in service url.
     * @param context The request context
     * @return Event
     */
    public Event checkOsfCampaigns(final RequestContext context) {

        final String service = context.getRequestParameters().get("service");
        String serviceCampaign = null;
        URL serviceUrl = null;

        try {
            serviceUrl = new URL(service);
        } catch (final MalformedURLException e) {
            LOGGER.error(String.format("Malformed Service URL: %s", e.toString()));
        }

        if (serviceUrl != null) {
            final String servicePath = serviceUrl.getPath();
            if (servicePath.startsWith("/prereg/")) {
                serviceCampaign = "PREREG";
            } else if (servicePath.startsWith("/erpc/")) {
                serviceCampaign = "ERPC";
            } else if (servicePath.startsWith("/preprints")) {
                serviceCampaign = "OSF-PREPRINTS";
            }
        }

        if (serviceCampaign != null) {
            context.getFlowScope().put("serviceCampaign", serviceCampaign);
            context.getFlowScope().put(
                    "registerCampaign",
                    String.format("?campaign=%s", serviceCampaign.toLowerCase())
            );
        }

        return new Event(this, "success");
    }
}
