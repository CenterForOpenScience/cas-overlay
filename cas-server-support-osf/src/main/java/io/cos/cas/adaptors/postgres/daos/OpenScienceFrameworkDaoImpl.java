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

import javax.persistence.*;
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
                    OpenScienceFrameworkUser.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            // TO-DO: more specific exception handling
            LOGGER.error(e.toString());
            return null;
        }
    }

    public OpenScienceFrameworkUser findOneUserByEmail(final String email) {
        try {
            final Query query= entityManager.createNativeQuery(
                    "select u.* from osf_models_osfuser u where u.emails @> '{" + email + "}'\\:\\:varchar[]",
                    OpenScienceFrameworkUser.class
            );
            // TO-DO use `query.setParameter("email", email)`
            // The issue is JPA does not recognize `:email` in query "select u.* from osf_models_osfuser u where u.emails @> '{:email}'\\:\\:varchar[]".
            return (OpenScienceFrameworkUser) query.getSingleResult();
        } catch (final Exception e) {
            // TO-DO: more specific exception handling
            LOGGER.error(e.toString());
            return null;
        }
    }

    @Override
    public OpenScienceFrameworkTimeBasedOneTimePassword findOneTimeBasedOneTimePasswordByOwnerId(final Integer ownerId) {
        try {
            final TypedQuery<OpenScienceFrameworkTimeBasedOneTimePassword> query = entityManager.createQuery(
                    "select p from OpenScienceFrameworkTimeBasedOneTimePassword p where p.id = :ownerId",
                    OpenScienceFrameworkTimeBasedOneTimePassword.class);
            query.setParameter("ownerId", ownerId);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            // TO-DO: more specific exception handling
            LOGGER.error(e.toString());
            return null;
        }
    }

    @Override
    public OpenScienceFrameworkInstitution findOneInstitutionById(final String _id) {
        try {
            final TypedQuery<OpenScienceFrameworkInstitution> query = entityManager.createQuery(
                    "select i from OpenScienceFrameworkInstitution i where i._id = :_id",
                    OpenScienceFrameworkInstitution.class);
            query.setParameter("_id", _id);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            // TO-DO: more specific exception handling
            LOGGER.error(e.toString());
            return null;
        }
    }

    @Override
    public OpenScienceFrameworkApiOauth2Scope findOneScopeByName(final String name) {
        try {
            final TypedQuery<OpenScienceFrameworkApiOauth2Scope> query = entityManager.createQuery(
                    "select s from OpenScienceFrameworkApiOauth2Scope s where s.name = :name",
                    OpenScienceFrameworkApiOauth2Scope.class);
            query.setParameter("name", name);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            // TO-DO: more specific exception handling
            LOGGER.error(e.toString());
            return null;
        }
    }

    @Override
    public OpenScienceFrameworkApiOauth2PersonalAccessToken findOnePersonalAccessTokenByTokenId(final String tokenId) {
        try {
            final TypedQuery<OpenScienceFrameworkApiOauth2PersonalAccessToken> query = entityManager.createQuery(
                    "select t from OpenScienceFrameworkApiOauth2PersonalAccessToken t where t.tokenId = :tokenId",
                    OpenScienceFrameworkApiOauth2PersonalAccessToken.class);
            query.setParameter("tokenId", tokenId);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            // TO-DO: more specific exception handling
            LOGGER.error(e.toString());
            return null;
        }
    }

    @Override
    public List<OpenScienceFrameworkApiOauth2Application> findOauthApplications() {
        try {
            final TypedQuery query = entityManager.createQuery(
                    "select a from OpenScienceFrameworkApiOauth2Application a where a.isActive = true",
                    OpenScienceFrameworkApiOauth2Application.class);
            return query.getResultList();
        } catch (final PersistenceException e) {
            // TO-DO: more specific exception handling
            LOGGER.error(e.toString());
            return null;
        }
    }
}
