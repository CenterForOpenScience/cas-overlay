package io.cos.cas.adaptors.postgres.daos;

import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkInstitution;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkTimeBasedOneTimePassword;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

public class OpenScienceFrameworkDaoImpl implements OpenScienceFrameworkDao {

    private static final Logger logger = LoggerFactory.getLogger(OpenScienceFrameworkDaoImpl.class);

    @NotNull
    @PersistenceContext(unitName = "persistenceUnitOsf")
    private EntityManager entityManager;

    public OpenScienceFrameworkDaoImpl() {}

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public OpenScienceFrameworkUser findOneUserByUsername(String username) {
        try {
            TypedQuery<OpenScienceFrameworkUser> query = entityManager.createQuery(
                    "select u from OpenScienceFrameworkUser u where u.username = :username",
                    OpenScienceFrameworkUser.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        }
        catch  (PersistenceException e) {
            // TODO: more specific exception handling
            logger.error(e.toString());
            return null;
        }
    }

    @Override
    public OpenScienceFrameworkTimeBasedOneTimePassword findOneTimeBasedOneTimePasswordByOwnerId(Integer ownerId) {
        try {
            TypedQuery<OpenScienceFrameworkTimeBasedOneTimePassword> query = entityManager.createQuery(
                    "select p from OpenScienceFrameworkTimeBasedOneTimePassword p where p.id = :ownerId",
                    OpenScienceFrameworkTimeBasedOneTimePassword.class);
            query.setParameter("ownerId", ownerId);
            return query.getSingleResult();
        }
        catch  (PersistenceException e) {
            // TODO: more specific exception handling
            logger.error(e.toString());
            return null;
        }
    }

    @Override
    public OpenScienceFrameworkInstitution findOneInstitutionByProviderId(String providerId) {
        try {
            TypedQuery<OpenScienceFrameworkInstitution> query = entityManager.createQuery(
                    "select i from OpenScienceFrameworkInstitution i where i.providerId = :providerId",
                    OpenScienceFrameworkInstitution.class);
            query.setParameter("providerId", providerId);
            return query.getSingleResult();
        }
        catch  (PersistenceException e) {
            // TODO: more specific exception handling
            logger.error(e.toString());
            return null;
        }
    }
}
