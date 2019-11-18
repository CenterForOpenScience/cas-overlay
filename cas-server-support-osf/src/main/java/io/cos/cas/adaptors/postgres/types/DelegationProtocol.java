/*
 * Copyright (c) 2017. Center for Open Science
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
package io.cos.cas.adaptors.postgres.types;

/**
 * Delegation Protocol Types.
 *
 * @author Michael Haselton
 * @since 4.1.5
 */
public enum DelegationProtocol {
    /** OAuth Protocol by pac4j. */
    OAUTH_PAC4J("oauth-pac4j"),
    /** CAS Protocol by pac4j. */
    CAS_PAC4J("cas-pac4j"),
    /** SAML Protocol by Shibboleth. */
    SAML_SHIB("saml-shib");

    private final String id;

    /**
     * Delegation Protocol constructor.
     *
     * @param id the id of the delegation type
     */
    DelegationProtocol(final String id) {
        this.id = id;
    }

    /**
     * Matches and returns the Delegation Protocol enumeration type of the id specified.
     *
     * @param id the id of the delegation type
     * @return the specific Delegation Protocol enumeration or null
     * @throws IllegalArgumentException if delegation protocol does not match
     */
    public static DelegationProtocol getType(final String id) throws IllegalArgumentException {
        if (id == null) {
            return null;
        }

        for (final DelegationProtocol type : DelegationProtocol.values()) {
            if (id.equals(type.getId())) {
                return type;
            }
        }
        throw new IllegalArgumentException("No matching type for id " + id);
    }

    public final String getId() {
        return id;
    }
}
