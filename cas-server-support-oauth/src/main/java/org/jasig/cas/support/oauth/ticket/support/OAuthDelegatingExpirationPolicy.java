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
import org.jasig.cas.support.oauth.authentication.principal.OAuthCredential;
import org.jasig.cas.support.oauth.token.TokenType;
import org.jasig.cas.ticket.AbstractTicket;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketState;
import org.jasig.cas.ticket.support.AbstractCasExpirationPolicy;

import javax.validation.constraints.NotNull;

/**
 * Delegates to different expiration policies depending on the OAuth token type {@link TokenType} specified by the
 * OAuth credential access type {@literal OAuthCredential#accessType}. The primary CAS authentication (i.e. the CAS
 * Auth Service, as opposed to the CAS OAuth Service) {@link org.jasig.cas.CentralAuthenticationServiceImpl} has two
 * policies using the class, {@code ticketGrantingTicketExpirationPolicy} and {@code serviceTicketExpirationPolicy}.
 *
 * With current CAS settings, for both TGT and ST, the refresh token policy {@link #oAuthRefreshTokenExpirationPolicy}
 * uses the built-in never-expire policy {@link org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy}. Thus,
 * the refresh tokens never expire.
 *
 * Similarly, the access token policy {@link #oAuthAccessTokenExpirationPolicy} for both TGT and ST uses the built-in
 * time-out policy {@link org.jasig.cas.ticket.support.TimeoutExpirationPolicy}, where the time-to-kill property
 * {@code timeToKillInMilliSeconds} is set to 3,600,000. Thus, access tokens expire after 1 hour.
 *
 * Instead of using the the built-in never-expire policy, this class simply let {@link #isExpired} always return
 * {@code False} for the personal access token. Thus, PATs never expire.
 *
 * The session policy differs between TGT and ST. For ST, the expiration policy is instantiated using the built-in
 * {@link org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy} with 10 seconds set as the time-to-kill.
 * For TGTs, the policy further depends on whether "Remember Me" or "Stay Signed In" is selected or not. If selected,
 * {@link org.jasig.cas.ticket.support.TimeoutExpirationPolicy} is used with time-to-kill set as 1,209,600,000, which
 * is 336 days (awkwardly, 336 days is one full year with 12 months of 28 days). Otherwise, it is the same as the ST
 * which is 10 seconds.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public final class OAuthDelegatingExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Unique id for serialization. */
    private static final long serialVersionUID = 4461752518354198401L;

    @NotNull
    private ExpirationPolicy oAuthRefreshTokenExpirationPolicy;

    @NotNull
    private ExpirationPolicy oAuthAccessTokenExpirationPolicy;

    @NotNull
    private ExpirationPolicy sessionExpirationPolicy;

    @Override
    public boolean isExpired(final TicketState ticketState) {

        final Authentication authentication;
        final AbstractTicket ticket = (AbstractTicket) ticketState;
        final TicketGrantingTicket ticketGrantingTicket = ticket.getGrantingTicket();

        if (ticketGrantingTicket != null) {
            authentication = ticketGrantingTicket.getAuthentication();
        } else {
            authentication = ticket.getAuthentication();
        }

        final TokenType tokenType
                = (TokenType) authentication.getAttributes().get(OAuthCredential.AUTHENTICATION_ATTRIBUTE_ACCESS_TYPE);

        // OFFLINE refresh token never expires; OFFLINE access token expires after 1 hour
        if (tokenType == TokenType.OFFLINE) {
            return ticket instanceof TicketGrantingTicket
                    ? oAuthRefreshTokenExpirationPolicy.isExpired(ticketState)
                    : oAuthAccessTokenExpirationPolicy.isExpired(ticketState);
        }

        // ONLINE access token expires after 1 hour
        if (tokenType == TokenType.ONLINE && ticket instanceof TicketGrantingTicket) {
            return oAuthAccessTokenExpirationPolicy.isExpired(ticketState);
        }

        // PERSONAL access token never expires
        if (tokenType == TokenType.PERSONAL && ticket instanceof TicketGrantingTicket) {
            return false;
        }

        // Service validation and other, expiration time varies
        return sessionExpirationPolicy.isExpired(ticketState);
    }

    public void setOAuthRefreshTokenExpirationPolicy(final ExpirationPolicy oAuthRefreshTokenExpirationPolicy) {
        this.oAuthRefreshTokenExpirationPolicy = oAuthRefreshTokenExpirationPolicy;
    }

    public void setOAuthAccessTokenExpirationPolicy(final ExpirationPolicy oAuthAccessTokenExpirationPolicy) {
        this.oAuthAccessTokenExpirationPolicy = oAuthAccessTokenExpirationPolicy;
    }

    public void setSessionExpirationPolicy(final ExpirationPolicy sessionExpirationPolicy) {
        this.sessionExpirationPolicy = sessionExpirationPolicy;
    }
}
