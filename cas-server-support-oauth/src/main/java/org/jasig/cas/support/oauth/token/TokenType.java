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
package org.jasig.cas.support.oauth.token;

/**
 * Token Type.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public enum TokenType {
    /**
     *  Offline Token.
     */
    OFFLINE(0),
    /**
     *  Online Token.
     */
    ONLINE(1),
    /**
     *  Personal Token.
     */
    PERSONAL(2);

    /**
     * The value of the token enumeration.
     */
    private final int value;

    /**
     * Constructs a new Token Type.
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
