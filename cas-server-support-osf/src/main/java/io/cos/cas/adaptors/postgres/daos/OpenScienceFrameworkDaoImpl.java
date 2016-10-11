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

package io.cos.cas.adaptors.postgres.daos;

import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkApiOauth2Application;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkApiOauth2PersonalAccessToken;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkApiOauth2Scope;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkInstitution;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkTimeBasedOneTimePassword;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The Open Science Framework Data Access Object Implementation.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
public class OpenScienceFrameworkDaoImpl implements OpenScienceFrameworkDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkDaoImpl.class);

    /** The entity manager for persistenceUnitOsf. */
    @NotNull
    @PersistenceContext(unitName = "persistenceUnitOsf")
    private EntityManager entityManager;

    /** Default Constructor. */
    public OpenScienceFrameworkDaoImpl() {}

    /**
     * @param entityManager the entity manager.
     */
    public void setEntityManager(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public OpenScienceFrameworkUser findOneUserByUsername(final String username) {
        try {
            final TypedQuery<OpenScienceFrameworkUser> query = entityManager.createQuery(
                    "select u from OpenScienceFrameworkUser u where u.username = :username",
                    OpenScienceFrameworkUser.class
            );
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            LOGGER.error(e.toString());
            return null;
        }
    }

    @Override
    public OpenScienceFrameworkUser findOneUserByEmail(final String email) {

        // check username (primary email) first
        final OpenScienceFrameworkUser user = findOneUserByUsername(email);
        if (user != null) {
            return user;
        }

        // check emails
        try {
            // JPA Hibernate does not support postgres query array operations, use postgres native queries
            // `query.setParameter()` does not work, use string concatenation instead
            final Query query= entityManager.createNativeQuery(
                    "select u.* from osf_osfuser u where u.emails @> '{" + email + "}'\\:\\:varchar[]",
                    OpenScienceFrameworkUser.class
            );
            return (OpenScienceFrameworkUser) query.getSingleResult();
        } catch (final PersistenceException e) {
            LOGGER.error(e.toString());
            return null;
        }
    }

    @Override
    public OpenScienceFrameworkTimeBasedOneTimePassword findOneTimeBasedOneTimePasswordByOwnerId(final Integer ownerId) {
        try {
            final TypedQuery<OpenScienceFrameworkTimeBasedOneTimePassword> query = entityManager.createQuery(
                    "select p from OpenScienceFrameworkTimeBasedOneTimePassword p where p.id = :ownerId",
                    OpenScienceFrameworkTimeBasedOneTimePassword.class
            );
            query.setParameter("ownerId", ownerId);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            LOGGER.error(e.toString());
            return null;
        }
    }

    @Override
    public OpenScienceFrameworkInstitution findOneInstitutionById(final String id) {
        try {
            final TypedQuery<OpenScienceFrameworkInstitution> query = entityManager.createQuery(
                    "select i from OpenScienceFrameworkInstitution i where i._id = :id",
                    OpenScienceFrameworkInstitution.class
            );
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            LOGGER.error(e.toString());
            return null;
        }
    }

    @Override
    public OpenScienceFrameworkApiOauth2Scope findOneScopeByName(final String name) {
        try {
            final TypedQuery<OpenScienceFrameworkApiOauth2Scope> query = entityManager.createQuery(
                    "select s from OpenScienceFrameworkApiOauth2Scope s where s.name = :name",
                    OpenScienceFrameworkApiOauth2Scope.class
            );
            query.setParameter("name", name);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            LOGGER.error(e.toString());
            return null;
        }
    }

    @Override
    public OpenScienceFrameworkApiOauth2PersonalAccessToken findOnePersonalAccessTokenByTokenId(final String tokenId) {
        try {
            final TypedQuery<OpenScienceFrameworkApiOauth2PersonalAccessToken> query = entityManager.createQuery(
                    "select t from OpenScienceFrameworkApiOauth2PersonalAccessToken t where t.tokenId = :tokenId",
                    OpenScienceFrameworkApiOauth2PersonalAccessToken.class
            );
            query.setParameter("tokenId", tokenId);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            LOGGER.error(e.toString());
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<OpenScienceFrameworkApiOauth2Application> findOauthApplications() {
        try {
            final TypedQuery query = entityManager.createQuery(
                    "select a from OpenScienceFrameworkApiOauth2Application a where a.isActive = true",
                    OpenScienceFrameworkApiOauth2Application.class
            );
            return query.getResultList();
        } catch (final PersistenceException e) {
            LOGGER.error(e.toString());
            return null;
        }
    }
}
