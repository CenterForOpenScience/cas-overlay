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
package org.jasig.cas.support.oauth.ticket.support;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.authentication.principal.OAuthCredential;
import org.jasig.cas.ticket.AbstractTicket;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketState;
import org.jasig.cas.ticket.support.AbstractCasExpirationPolicy;

import javax.validation.constraints.NotNull;

/**
 * Delegates to different expiration policies depending on whether oauth
 * is true or not.
 *
 * @author Michael Haselton
 * @since 4.1.0
 *
 */
public final class OAuthDelegatingExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serialization support. */
    private static final long serialVersionUID = 4461752518354198401L;

    @NotNull
    private ExpirationPolicy oAuthGrantTypeAuthorizationCodeExpirationPolicy;

    @NotNull
    private ExpirationPolicy oAuthGrantTypePasswordExpirationPolicy;

    @NotNull
    private ExpirationPolicy sessionExpirationPolicy;

    @Override
    public boolean isExpired(final TicketState ticketState) {
        AbstractTicket ticket = (AbstractTicket) ticketState;
        Authentication authentication = ticket.getAuthentication();
        if (authentication == null) {
            authentication = ticket.getGrantingTicket().getAuthentication();
        }

        final Boolean b = (Boolean) authentication.getAttributes().
            get(OAuthCredential.AUTHENTICATION_ATTRIBUTE_OAUTH);
        if (b == null || b.equals(Boolean.FALSE)) {
            return this.sessionExpirationPolicy.isExpired(ticketState);
        }

        final String grantType = (String) authentication.getAttributes().get(OAuthConstants.GRANT_TYPE);
        if (grantType.equalsIgnoreCase(OAuthConstants.AUTHORIZATION_CODE)) {
            return this.oAuthGrantTypeAuthorizationCodeExpirationPolicy.isExpired(ticketState);
        } else if (grantType.equalsIgnoreCase(OAuthConstants.PASSWORD)) {
            return this.oAuthGrantTypePasswordExpirationPolicy.isExpired(ticketState);
        }

        return this.sessionExpirationPolicy.isExpired(ticketState);
    }

    public void setOAuthGrantTypeAuthorizationCodeExpirationPolicy(
            final ExpirationPolicy oAuthGrantTypeAuthorizationCodeExpirationPolicy) {
        this.oAuthGrantTypeAuthorizationCodeExpirationPolicy = oAuthGrantTypeAuthorizationCodeExpirationPolicy;
    }

    public void setOAuthGrantTypePasswordExpirationPolicy(
            final ExpirationPolicy oAuthGrantTypePasswordExpirationPolicy) {
        this.oAuthGrantTypePasswordExpirationPolicy = oAuthGrantTypePasswordExpirationPolicy;
    }

    public void setSessionExpirationPolicy(final ExpirationPolicy sessionExpirationPolicy) {
        this.sessionExpirationPolicy = sessionExpirationPolicy;
    }
}
