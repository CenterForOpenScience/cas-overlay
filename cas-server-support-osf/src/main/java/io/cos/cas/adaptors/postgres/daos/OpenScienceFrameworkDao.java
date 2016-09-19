package io.cos.cas.adaptors.postgres.daos;

import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkInstitution;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkTimeBasedOneTimePassword;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkUser;

public interface OpenScienceFrameworkDao {

    OpenScienceFrameworkUser findOneUserByUsername(String username);

    OpenScienceFrameworkTimeBasedOneTimePassword findOneTimeBasedOneTimePasswordByOwnerId(Integer ownerId);

    OpenScienceFrameworkInstitution findOneInstitutionByProviderId(String providerId);

}
