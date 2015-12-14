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
package io.cos.cas.authentication.handler.support;

import io.cos.cas.authentication.OpenScienceFrameworkCredential;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.web.flow.AbstractNonInteractiveCredentialsAction;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

/**
 * Implementation of the NonInteractiveCredentialsAction that looks for a remote
 * user that is set in the <code>HttpServletRequest</code> and attempts to
 * construct a Principal (and thus a PrincipalBearingCredential). If it doesn't
 * find one, this class returns and error event which tells the web flow it
 * could not find any credentials.
 *
 * @author Michael Haselton
 * @since 4.1.1
 */
public final class OpenScienceFrameworkPrincipalFromRequestRemoteUserNonInteractiveCredentialsAction
            extends AbstractNonInteractiveCredentialsAction {

    private static final String REMOTE_USER = "REMOTE_USER";

    private static final String ATTRIBUTE_PREFIX = "AUTH-";

    private static final String SHIBBOLETH_COOKIE_PREFIX = "_shibsession_";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected Credential constructCredentialsFromRequest(
            final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final OpenScienceFrameworkCredential credential = (OpenScienceFrameworkCredential) context.getFlowScope().get("credential");

        // Clear the shibboleth session cookie, allows the user to logout of our system and login as a different user.
        // Assumes we would redirect the user to the proper (custom) Shibboleth logout endpoint from OSF.
        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
        for (final Cookie cookie : request.getCookies()) {
            if (cookie.getName().startsWith(SHIBBOLETH_COOKIE_PREFIX)) {
                final Cookie shibbolethCookie = new Cookie(cookie.getName(), null);
                shibbolethCookie.setMaxAge(0);
                response.addCookie(shibbolethCookie);
            }
        }

        // WARNING: Do not assume this works w/o acceptance testing in a Production environment.
        // The call is made to trust these headers only because we assume the Apache Shibboleth Service Provider module
        // rejects any conflicting / forged headers.
        final String remoteUser = request.getHeader(REMOTE_USER);
        if (StringUtils.hasText(remoteUser)) {
            logger.debug("Remote  User [{}] found in HttpServletRequest", remoteUser);
            credential.setRemotePrincipal(Boolean.TRUE);
            credential.setUsername(remoteUser);

            for (final String headerName : Collections.list(request.getHeaderNames())) {
                if (headerName.startsWith(ATTRIBUTE_PREFIX)) {
                    credential.getAuthenticationHeaders().put(
                            headerName.substring(ATTRIBUTE_PREFIX.length()),
                            request.getHeader(headerName)
                    );
                }
            }
            return credential;
        }

        logger.debug("Remote User not found in HttpServletRequest.");
        return null;
    }
}
