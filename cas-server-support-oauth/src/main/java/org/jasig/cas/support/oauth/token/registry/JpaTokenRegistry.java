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
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.jasig.cas.support.oauth.token.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Set;

/**
 * JPA Token Registry.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public final class JpaTokenRegistry implements TokenRegistry {

    /** The Commons Logging logger instance. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @NotNull
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void updateToken(final Token token) {
        entityManager.merge(token);
        logger.debug("Updated token [{}].", token);
    }

    @Override
    public void addToken(final Token token) {
        entityManager.persist(token);
        logger.debug("Added token [{}] to registry.", token);
    }

    @SuppressWarnings("unchecked")
    public final <T extends Token> T getToken(final String tokenId, final Class<T> clazz) throws ClassCastException {
        Assert.notNull(clazz, "clazz cannot be null");

        final Token token = entityManager.find(getClassImplementation(clazz), tokenId);
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

    @Override
    public <T extends Token> Collection<T> getTokens(String clientId, Class<T> clazz) throws ClassCastException {
        return getTokens(clientId, null, clazz);
    }

    @Override
    public <T extends Token> Collection<T> getTokens(String clientId, String principalId, Class<T> clazz) throws ClassCastException {
        Assert.notNull(clientId, "clientId cannot be null");
        Assert.notNull(clazz, "clazz cannot be null");

        final Class<T> clazzImpl = getClassImplementation(clazz);
        final Collection<T> tokens;
        try {
            if (principalId == null) {
                tokens = entityManager
                        .createQuery("select t from " + clazzImpl.getSimpleName() + " t where t.clientId = :clientId", clazzImpl)
                        .setParameter("clientId", clientId)
                        .getResultList();
            } else {
                tokens = entityManager
                        .createQuery("select t from " + clazzImpl.getSimpleName() + " t where t.clientId = :clientId and t.principalId = :principalId", clazzImpl)
                        .setParameter("clientId", clientId)
                        .setParameter("principalId", principalId)
                        .getResultList();
            }
        } catch (NoResultException ex) {
            return null;
        }

        return tokens;
    }

    @Override
    public <T extends Token> Boolean isToken(final String clientId, final String principalId, final Set<String> scopes, Class<T> clazz) {
        return isToken(null, clientId, principalId, scopes, clazz);
    }

    @Override
    public <T extends Token> Boolean isToken(final TokenType type, final String clientId, final String principalId, final Set<String> scopes, Class<T> clazz) {
        Assert.notNull(clientId, "clientId cannot be null");
        Assert.notNull(principalId, "principalId cannot be null");
        Assert.notNull(scopes, "scopes cannot be null");
        Assert.notNull(clazz, "clazz cannot be null");

        final Class<T> clazzImpl = getClassImplementation(clazz);
        final Collection<T> tokens;
        try {
            if (type == null) {
                tokens = entityManager
                        .createQuery("select t from " + clazzImpl.getSimpleName() + " t where t.clientId = :clientId and t.principalId = :principalId and t.scopesHash = :scopesHash", clazzImpl)
                        .setParameter("clientId", clientId)
                        .setParameter("principalId", principalId)
                        .setParameter("scopesHash", scopes.hashCode())
                        .getResultList();
            } else {
                tokens = entityManager
                        .createQuery("select t from " + clazzImpl.getSimpleName() + " t where t.type = :type and t.clientId = :clientId and t.principalId = :principalId and t.scopesHash = :scopesHash", clazzImpl)
                        .setParameter("type", type)
                        .setParameter("clientId", clientId)
                        .setParameter("principalId", principalId)
                        .setParameter("scopesHash", scopes.hashCode())
                        .getResultList();
            }
        } catch (NoResultException ex) {
            return false;
        }

        for (final Token token : tokens) {
            if (!token.getTicket().isExpired()) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private <T extends Token> Class<T> getClassImplementation(Class<T> clazz) throws ClassCastException {
        if (AuthorizationCode.class.isAssignableFrom(clazz)) {
            return (Class<T>) AuthorizationCodeImpl.class;
        } else if (RefreshToken.class.isAssignableFrom(clazz)) {
            return (Class<T>) RefreshTokenImpl.class;
        } else if (AccessToken.class.isAssignableFrom(clazz)) {
            return (Class<T>) AccessTokenImpl.class;
        }

        throw new ClassCastException("Could not cast " + clazz
                + " to a suitable token implementation class");
    }
}
