package io.cos.cas.adaptors.postgres.daos;

import io.cos.cas.adaptors.postgres.models.*;

public interface OpenScienceFrameworkDao {

    OpenScienceFrameworkUser findOneUserByUsername(String username);

    OpenScienceFrameworkTimeBasedOneTimePassword findOneTimeBasedOneTimePasswordByOwnerId(Integer ownerId);

    OpenScienceFrameworkInstitution findOneInstitutionByProviderId(String providerId);

    OpenScienceFrameworkApiOauth2Scope findOneScopeByName(String name);

    OpenScienceFrameworkApiOauth2PersonalAccessToken findOnePersonalAccessTokenByTokenId(String tokenId);
}
