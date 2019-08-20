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
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkApiOauth2TokenScope;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkEmail;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkGuid;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkInstitution;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkTimeBasedOneTimePassword;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkUser;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
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

    /**
     * Find one OSF User by username (primary email).
     *
     * @param username the username
     * @return OpenScienceFrameworkUser or null
     */
    private OpenScienceFrameworkUser findOneUserByUsername(final String username) {
        try {
            final TypedQuery<OpenScienceFrameworkUser> query = entityManager.createQuery(
                    "select u from OpenScienceFrameworkUser u where u.username = :username",
                    OpenScienceFrameworkUser.class
            );
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            return null;
        }
    }

    /**
     * Find one OSF Email by email address.
     *
     * @param address the address
     * @return OpenScienceFrameworkEmail or null
     */
    private OpenScienceFrameworkEmail findOneEmailByAddress(final String address) {

        try {
            final TypedQuery<OpenScienceFrameworkEmail> query = entityManager.createQuery(
                    "select e from OpenScienceFrameworkEmail e where e.address = :address",
                    OpenScienceFrameworkEmail.class
            );
            query.setParameter("address", address);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            return null;
        }
    }

    @Override
    public OpenScienceFrameworkUser findOneUserByEmail(final String address) {

        // check username (primary email) first
        final OpenScienceFrameworkUser user = findOneUserByUsername(address);
        if (user != null) {
            return user;
        }

        // check osf email
        final OpenScienceFrameworkEmail email = findOneEmailByAddress(address);
        return email != null ? email.getUser() : null;
    }

    @Override
    public OpenScienceFrameworkTimeBasedOneTimePassword findOneTimeBasedOneTimePasswordByOwnerId(final Integer ownerId) {
        try {
            final TypedQuery<OpenScienceFrameworkTimeBasedOneTimePassword> query = entityManager.createQuery(
                    "select p from OpenScienceFrameworkTimeBasedOneTimePassword p where p.owner.id = :ownerId",
                    OpenScienceFrameworkTimeBasedOneTimePassword.class
            );
            query.setParameter("ownerId", ownerId);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            return null;
        }
    }

    @Override
    public OpenScienceFrameworkInstitution findOneInstitutionById(final String id) {
        try {
            final TypedQuery<OpenScienceFrameworkInstitution> query = entityManager.createQuery(
                    "select i from OpenScienceFrameworkInstitution i where i.objectId = :id",
                    OpenScienceFrameworkInstitution.class
            );
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            return null;
        }
    }

    @Override
    public List<OpenScienceFrameworkInstitution> findAllInstitutions() {
        try {
            final TypedQuery query = entityManager.createQuery(
                    "select i from OpenScienceFrameworkInstitution i "
                            + "where (not i.delegationProtocol = '') and i.deleted = false",
                    OpenScienceFrameworkInstitution.class
            );
            return query.getResultList();
        } catch (final PersistenceException e) {
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
            return null;
        }
    }

    @Override
    public OpenScienceFrameworkApiOauth2Scope findOneScopeByScopePk(final Integer scopePk) {
        try {
            final TypedQuery<OpenScienceFrameworkApiOauth2Scope> query = entityManager.createQuery(
                    "select s from OpenScienceFrameworkApiOauth2Scope s where s.id = :id",
                    OpenScienceFrameworkApiOauth2Scope.class
            );
            query.setParameter("id", scopePk);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
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
            return null;
        }
    }

    @Override
    public OpenScienceFrameworkGuid findGuidByUser(final OpenScienceFrameworkUser user) {
        try {
            final TypedQuery<OpenScienceFrameworkGuid> query = entityManager.createQuery(
                    "select g from OpenScienceFrameworkGuid g where"
                    + " g.objectId = :userId"
                    + " and g.djangoContentType.appLabel = :appLable"
                    + " and g.djangoContentType.model = :model",
                    OpenScienceFrameworkGuid.class
            );
            query.setParameter("userId", user.getId());
            query.setParameter("appLable", "osf");
            query.setParameter("model", "osfuser");
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            return null;
        }
    }

    @Override
    public List<OpenScienceFrameworkApiOauth2TokenScope> findAllTokenScopesByTokenPk(final Integer tokenPk) {
        try {
            final TypedQuery<OpenScienceFrameworkApiOauth2TokenScope> query = entityManager.createQuery(
                    "select m from OpenScienceFrameworkApiOauth2TokenScope m where m.tokenPk = :tokenPk",
                    OpenScienceFrameworkApiOauth2TokenScope.class
            );
            query.setParameter("tokenPk", tokenPk);
            return query.getResultList();
        } catch (final PersistenceException e) {
            return null;
        }
    }
}
