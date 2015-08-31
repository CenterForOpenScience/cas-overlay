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

import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.support.oauth.token.AccessTokenImpl;
import org.jasig.cas.support.oauth.token.AuthorizationCode;
import org.jasig.cas.support.oauth.token.AuthorizationCodeImpl;
import org.jasig.cas.support.oauth.token.RefreshToken;
import org.jasig.cas.support.oauth.token.RefreshTokenImpl;
import org.jasig.cas.support.oauth.token.Token;
import org.jasig.cas.support.oauth.token.TokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
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
    public void addToken(final Token token) {
        entityManager.persist(token);
        logger.debug("Added token [{}] to registry.", token);
    }

    @Override
    public void updateToken(final Token token) {
        entityManager.merge(token);
        logger.debug("Updated token [{}].", token);
    }

    @Override
    public <T extends Token> T getToken(final String tokenId, final Class<T> clazz) throws ClassCastException {
        Assert.notNull(clazz, "clazz cannot be null");

        final T token = entityManager.find(getClassImplementation(clazz), tokenId);
        if (token == null) {
            return null;
        }

        if (!clazz.isAssignableFrom(token.getClass())) {
            throw new ClassCastException("Token [" + token.getId()
                    + "] is of type " + token.getClass()
                    + " when we were expecting " + clazz);
        }

        return token;
    }

    @Override
    public <T extends Token> Collection<T> getClientTokens(final String clientId, final Class<T> clazz) throws ClassCastException {
        Assert.notNull(clientId, "clientId cannot be null");
        Assert.notNull(clazz, "clazz cannot be null");

        final Class<T> clazzImpl = getClassImplementation(clazz);
        try {
            return entityManager
                    .createQuery("select t from " + clazzImpl.getSimpleName() + " t where t.clientId = :clientId", clazzImpl)
                    .setParameter("clientId", clientId)
                    .getResultList();
        } catch (final NoResultException e) {
            return null;
        }
    }

    @Override
    public <T extends Token> Collection<T> getClientPrincipalTokens(final String clientId, final String principalId, final Class<T> clazz)
            throws ClassCastException {
        return getClientPrincipalTokens(clientId, principalId, null, clazz);
    }

    @Override
    public <T extends Token> Collection<T> getClientPrincipalTokens(final String clientId, final String principalId, final TokenType type,
                                                                    final Class<T> clazz) throws ClassCastException {
        Assert.notNull(clientId, "clientId cannot be null");
        Assert.notNull(principalId, "principalId cannot be null");
        Assert.notNull(clazz, "clazz cannot be null");

        final Class<T> clazzImpl = getClassImplementation(clazz);
        try {
            if (type == null) {
                return entityManager
                        .createQuery("select t from " + clazzImpl.getSimpleName()
                                + " t where t.clientId = :clientId and t.principalId = :principalId", clazzImpl)
                        .setParameter("clientId", clientId)
                        .setParameter("principalId", principalId)
                        .getResultList();
            }

            return entityManager
                    .createQuery("select t from " + clazzImpl.getSimpleName()
                            + " t where t.clientId = :clientId and t.principalId = :principalId and t.type = :type", clazzImpl)
                    .setParameter("clientId", clientId)
                    .setParameter("principalId", principalId)
                    .setParameter("type", type)
                    .getResultList();
        } catch (final NoResultException e) {
            return null;
        }
    }

    @Override
    public <T extends Token> Collection<T> getPrincipalTokens(final String principalId, final Class<T> clazz) throws ClassCastException {
        Assert.notNull(principalId, "principalId cannot be null");
        Assert.notNull(clazz, "clazz cannot be null");

        final Class<T> clazzImpl = getClassImplementation(clazz);
        try {
            return entityManager
                    .createQuery("select t from " + clazzImpl.getSimpleName() + " t where t.principalId = :principalId", clazzImpl)
                    .setParameter("principalId", principalId)
                    .getResultList();
        } catch (final NoResultException e) {
            return null;
        }
    }

    @Override
    public <T extends Token> Boolean isToken(final String clientId, final String principalId, final Set<String> scopes,
                                             final Class<T> clazz) {
        return isToken(null, clientId, principalId, scopes, clazz);
    }

    @Override
    public <T extends Token> Boolean isToken(final TokenType type, final String clientId, final String principalId,
                                             final Set<String> scopes, final Class<T> clazz) {
        Assert.notNull(clientId, "clientId cannot be null");
        Assert.notNull(principalId, "principalId cannot be null");
        Assert.notNull(scopes, "scopes cannot be null");
        Assert.notNull(clazz, "clazz cannot be null");

        final Class<T> clazzImpl = getClassImplementation(clazz);
        final Collection<T> tokens;
        try {
            if (type == null) {
                tokens = entityManager
                        .createQuery("select t from " + clazzImpl.getSimpleName() + " t "
                                        + "where t.clientId = :clientId and t.principalId = :principalId and t.scopesHash = :scopesHash",
                                clazzImpl)
                        .setParameter("clientId", clientId)
                        .setParameter("principalId", principalId)
                        .setParameter("scopesHash", scopes.hashCode())
                        .getResultList();
            } else {
                tokens = entityManager
                        .createQuery("select t from " + clazzImpl.getSimpleName() + " t "
                                        + "where t.type = :type and t.clientId = :clientId and t.principalId = :principalId "
                                        + "and t.scopesHash = :scopesHash",
                                clazzImpl)
                        .setParameter("type", type)
                        .setParameter("clientId", clientId)
                        .setParameter("principalId", principalId)
                        .setParameter("scopesHash", scopes.hashCode())
                        .getResultList();
            }
        } catch (final NoResultException e) {
            return Boolean.FALSE;
        }

        for (final Token token : tokens) {
            if (!token.getTicket().isExpired()) {
                return Boolean.TRUE;
            }
        }

        return Boolean.TRUE;
    }

    @Override
    public Integer getPrincipalCount(final String clientId) {
        Assert.notNull(clientId, "clientId cannot be null");

        final Set<String> principals = new HashSet<>();
        try {
            principals.addAll(entityManager
                    .createQuery("select distinct t.principalId from RefreshTokenImpl t where t.clientId = :clientId", String.class)
                    .setParameter("clientId", clientId)
                    .getResultList());
        } catch (final NoResultException e) {
            // no results
        }

        try {
            principals.addAll(entityManager
                    .createQuery("select distinct t.principalId from AccessTokenImpl t where t.clientId = :clientId", String.class)
                    .setParameter("clientId", clientId)
                    .getResultList());
        } catch (final NoResultException e) {
            // no results
        }

        return principals.size();
    }

    /**
     * Retrieve the token implementation class of the clazz specified.
     *
     * @param clazz The assignable form of the implementation class.
     * @param <T> the generic token type to return that extends {@link Token}
     * @return the token implementation class.
     * @throws ClassCastException the class cast exception.
     */
    @SuppressWarnings("unchecked")
    private <T extends Token> Class<T> getClassImplementation(final Class<T> clazz) throws ClassCastException {
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
