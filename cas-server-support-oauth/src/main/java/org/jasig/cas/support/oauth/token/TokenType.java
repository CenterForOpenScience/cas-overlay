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
package org.jasig.cas.support.oauth.token;

/**
 * Defines the enum data type for {@literal AbstractToken#type}.
 *
 * When talking about OAuth Token, we often refer to it in a way that is quite confusing. On one hand, from the
 * perspective of OSF and CAS, there are four types conceptually: Access Token, Refresh Token, Personal Access Token
 * and CAS Access Token.
 *
 * On the other hand, at the implementation level (defined in {@link TokenType}), the four types are OFFLINE - 0,
 * ONLINE - 1, PERSONAL - 2 and CAS - 3.
 *
 * There are also three implementations of {@link Token}. {@link AccessTokenImpl} and {@link RefreshTokenImpl} are
 * indeed OAuth Tokens while {@link AuthorizationCodeImpl} is actually OAuth Code.
 *
 * Here is a table that clarifies the confusion.
 *
 * | Conceptual-level Type  | Implementation-level Type | Implementation Class          |
 * | ---------------------- | --------------------------| ----------------------------- |
 * | Access Token           | 0 or 1                    | {@link AccessTokenImpl}       |
 * | Refresh Token          | 0                         | {@link RefreshTokenImpl}      |
 * | Personal Access Token  | 2                         | {@link AccessTokenImpl}       |
 * | CAS Access Token       | 3                         | {@link AccessTokenImpl}       |
 * | Authorization Code     | 0 or 1                    | {@link AuthorizationCodeImpl} |
 *
 * Note 1: {@link RefreshTokenImpl} is always of type 0 and {@link AccessTokenImpl} can be of any types from 0 to 3.
 *
 * Note 2: {@link org.jasig.cas.support.oauth.personal.PersonalAccessToken} is not the implementation for Personal
 * Access Token. It is a helper class that temporarily stores the token information which is retrieved from the OSF
 * database in {@literal io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkApiOauth2PersonalAccessToken}. Then
 * {@link AccessTokenImpl} uses the stored information to create the Personal Access Token.
 *
 * Note 3: Authorization Code is not an OAuth Token by concept. However, it does have a {@link Token} implementation
 * {@link AuthorizationCodeImpl} with type 0 or 1. Its type determines 1) whether a Refresh Token is issued during the\
 * Token-Code exchange process and 2) the type of the Access Token issued.
 *
 * Note 4: CAS access token is only granted by
 * {@link org.jasig.cas.support.oauth.CentralOAuthServiceImpl#grantCASAccessToken} during the CAS protocol 3.0 service
 * validation process {@link org.jasig.cas.support.oauth.web.OAuth20ServiceValidateController}. Compared with ONLINE
 * and OFFLINE access tokens, its behavior is slightly different. In addition, with current OSF settings, this access
 * token is only stored in the user's OSF session.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public enum TokenType {

    /** Offline tokens. */
    OFFLINE(0),

    /** Online tokens. */
    ONLINE(1),

    /** Personal access tokens. */
    PERSONAL(2),

    /** CAS access tokens. */
    CAS(3);

    /** The value of the token enumeration. */
    private final int value;

    /**
     * Constructs a new {@link TokenType}.
     *
     * @param newValue the value representing the token type.
     */
    TokenType(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }
}
