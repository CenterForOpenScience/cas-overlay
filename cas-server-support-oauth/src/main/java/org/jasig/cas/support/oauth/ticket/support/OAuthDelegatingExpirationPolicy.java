/*
 * Copyright (c) 2015. Center for Open Science
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * For both TGT and ST, the refresh token policy {@link #oAuthRefreshTokenExpirationPolicy} uses the built-in
 * never-expire policy {@link org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy}. Thus, the refresh token
 * never expires.
 *
 * For both TGT and ST, the ONLINE / OFFLINE access token policy {@link #oAuthAccessTokenExpirationPolicy} uses the
 * built-in time-out policy {@link org.jasig.cas.ticket.support.TimeoutExpirationPolicy}, where
 * {@code timeToKillInMilliSeconds} is set to 3,600,000 milliseconds. Thus, both ONLINE and OFFLINE access tokens
 * expire after 1 hour.
 *
 * The PERSONAL access token never expires. Instead of using the built-in never-expire policy, this class simply let
 * the expiration check method {@link #isExpired} always return {@code False}.
 *
 * The session policy differs between TGT and ST. This policy affects the expiration time for CAS access token (which
 * is determined by the TGT) and the time for ONLINE / OFFLINE authorization code (which is determined by the ST).
 *
 * 1) ST uses the built-in {@link org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy}, where
 * {@code timeToKill} is set to 60 seconds. Thus, both ONLINE and OFFLINE authorization codes expire in 1 minute.
 *
 * 2) For TGTs, which policy to use depends on whether the "Remember Me" or "Stay Signed In" option is selected or not
 * at login. If selected, {@link org.jasig.cas.ticket.support.TimeoutExpirationPolicy} is used with
 * {@code timeToKillInMilliSeconds} set to 2,592,000 milliseconds, which is 30 days. Otherwise,
 * {@link org.jasig.cas.ticket.support.TicketGrantingTicketExpirationPolicy} is used with a 2-hour sliding expiration
 * with 8-hour maximum lifetime.
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
