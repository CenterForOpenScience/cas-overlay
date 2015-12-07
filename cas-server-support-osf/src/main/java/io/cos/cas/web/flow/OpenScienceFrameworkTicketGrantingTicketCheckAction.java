/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.web.flow.TicketGrantingTicketCheckAction;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;

/**
 * Webflow action that checks whether the Action parameter is specified and invalidates the TGT.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public class OpenScienceFrameworkTicketGrantingTicketCheckAction extends TicketGrantingTicketCheckAction {

    /**
     * The Central authentication service.
     */
    @NotNull
    private final CentralAuthenticationService centralAuthenticationService;

    /**
     * Creates a new instance with the given ticket registry.
     *
     * @param centralAuthenticationService the central authentication service
     */
    public OpenScienceFrameworkTicketGrantingTicketCheckAction(final CentralAuthenticationService centralAuthenticationService) {
        super(centralAuthenticationService);
        this.centralAuthenticationService = centralAuthenticationService;
    }

    /**
     * Determines whether the TGT in the flow request context is valid.
     *
     * @param requestContext Flow request context.
     */
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final Event event = super.doExecute(requestContext);
        if (event.getId().equals(VALID)) {
            final String auto = requestContext.getRequestParameters().get("auto");
            if (auto != null && auto.equalsIgnoreCase(Boolean.TRUE.toString())) {
                final String tgtId = WebUtils.getTicketGrantingTicketId(requestContext);
                this.centralAuthenticationService.destroyTicketGrantingTicket(tgtId);
                return new Event(this, NOT_EXISTS);
            }
        }
        return event;
    }
}
