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
package org.jasig.cas.support.oauth.token.registry;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;

import org.jasig.cas.support.oauth.token.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * todo...
 *
 * @author Michael Haselton
 *
 * @since 4.2.1
 *
 */
public final class JpaTokenRegistry implements TokenRegistry {

    /** The Commons Logging logger instance. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @NotNull
    @PersistenceContext
    private EntityManager entityManager;

    public void updateToken(final Token token) {
        entityManager.merge(token);
        logger.debug("Updated token [{}].", token);
    }

    public void addToken(final Token token) {
        entityManager.persist(token);
        logger.debug("Added token [{}] to registry.", token);
    }

    @SuppressWarnings("unchecked")
    public final <T extends Token> T getToken(final String tokenId, final Class<? extends Token> clazz)
            throws ClassCastException {
        Assert.notNull(clazz, "clazz cannot be null");

        Class<? extends Token> tokenImplClass;
        if (CodeToken.class.isAssignableFrom(clazz)) {
            tokenImplClass = CodeTokenImpl.class;
        } else if (RefreshToken.class.isAssignableFrom(clazz)) {
            tokenImplClass = RefreshTokenImpl.class;
        } else if (AccessToken.class.isAssignableFrom(clazz)) {
            tokenImplClass = AccessTokenImpl.class;
        } else {
            throw new ClassCastException("Could not cast " + clazz
                    + " to a suitable token implementation class");
        }

        final Token token = entityManager.find(tokenImplClass, tokenId);
        if (token == null) {
            return null;
        }

        if (!clazz.isAssignableFrom(token.getClass())) {
            throw new ClassCastException("Token [" + token.getId()
                    + "] is of type " + token.getClass()
                    + " when we were expecting " + clazz);
        }

        return (T) token;
    }

    public final RefreshToken getRefreshToken(final String clientId, final String principalId) {
        Assert.notNull(clientId, "clientId cannot be null");
        Assert.notNull(principalId, "principalId cannot be null");

        try {
            return entityManager
                    .createQuery("select t from RefreshTokenImpl t where t.clientId = :clientId and t.principalId = :principalId", RefreshTokenImpl.class)
                    .setParameter("clientId", clientId)
                    .setParameter("principalId", principalId)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
}
