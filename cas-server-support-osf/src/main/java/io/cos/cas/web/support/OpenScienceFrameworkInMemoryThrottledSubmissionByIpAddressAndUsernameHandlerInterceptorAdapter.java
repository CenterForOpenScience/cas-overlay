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
package io.cos.cas.web.support;

import org.jasig.inspektr.common.web.ClientInfoHolder;
import javax.servlet.http.HttpServletRequest;

/**
 * Attempts to throttle by both IP Address and username. This is our improved version of Apereo's
 * InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter.
 *
 * @author Longze Chen
 * @since 4.1.0
 *
 */
public class OpenScienceFrameworkInMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter
        extends AbstractOpenScienceFrameworkInMemoryThrottledSubmissionHandlerInterceptorAdapter{

    @Override
    protected String constructKey(final HttpServletRequest request) {
        final String username = request.getParameter(getUsernameParameter());

        if (username == null) {
            return request.getRemoteAddr();
        }

        return ClientInfoHolder.getClientInfo().getClientIpAddress() + ';' + username.toLowerCase();
    }
}
