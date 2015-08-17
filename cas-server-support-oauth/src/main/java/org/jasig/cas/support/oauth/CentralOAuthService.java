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
package org.jasig.cas.support.oauth;

import org.jasig.cas.support.oauth.scope.OAuthScope;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.CodeToken;
import org.jasig.cas.support.oauth.token.RefreshToken;
import org.jasig.cas.support.oauth.token.Token;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.TicketException;

import java.util.Map;
import java.util.Set;

/**
 * Central OAuth Service
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public interface CentralOAuthService {

    CodeToken grantCodeToken(String ticketGrantingTicketId, String clientId, String callbackUrl, Set<String> scope) throws TicketException;

    RefreshToken grantRefreshToken(String code, String clientId, String clientSecret, String redirectUri) throws TicketException;

    AccessToken grantAccessToken(RefreshToken refreshToken) throws TicketException;

    AccessToken grantAccessToken(String ticketGrantingTicketId, String serviceId) throws TicketException;

    RefreshToken getRefreshToken(String clientId, String principalId);

    <T extends Token> T getToken(String tokenId, Class<? extends Token> clazz) throws InvalidTicketException;

    Map<String, OAuthScope> getScopeMap(String scope);

    OAuthScope getDefaultScope();
}
