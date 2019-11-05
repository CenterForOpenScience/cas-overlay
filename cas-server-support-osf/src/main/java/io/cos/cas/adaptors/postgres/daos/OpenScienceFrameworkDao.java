/*
 * Copyright (c) 2016. Center for Open Science
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
package io.cos.cas.adaptors.postgres.daos;

import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkApiOauth2Application;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkApiOauth2PersonalAccessToken;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkApiOauth2Scope;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkApiOauth2TokenScope;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkGuid;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkInstitution;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkTimeBasedOneTimePassword;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkUser;

import java.util.List;

/**
 * The Open Science Framework Data Access Object Interface.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
public interface OpenScienceFrameworkDao {

    /**
     * Find one user by user's emails, including username.
     *
     * @param email the user's email
     * @return OpenScienceFrameworkUser or null
     */
    OpenScienceFrameworkUser findOneUserByEmail(final String email);

    /**
     * Find one "time based one-time password" by owner.
     *
     * @param ownerId the owner id
     * @return OpenScienceFrameworkTimeBasedOneTimePassword or null
     */
    OpenScienceFrameworkTimeBasedOneTimePassword findOneTimeBasedOneTimePasswordByOwnerId(final Integer ownerId);

    /**
     * Find one institution by institution id, a.k.a `objectId` @"_id".
     *
     * @param id the institution id
     * @return OpenScienceFrameworkInstitution or null
     */
    OpenScienceFrameworkInstitution findOneInstitutionById(final String id);

    /**
     * Find a list of institutions that support institution login.
     *
     * @return a list of institutions
     */
    List<OpenScienceFrameworkInstitution> findAllInstitutions();

    /**
     * Find one scope by scope name.
     *
     * @param name the scope name
     * @return OpenScienceFrameworkApiOauth2Scope or null
     */
    OpenScienceFrameworkApiOauth2Scope findOneScopeByName(final String name);

    /**
     * Find one scope by the scope's primary key id.
     *
     * @param scopePk the scope's primary key
     * @return OpenScienceFrameworkApiOauth2Scope or null
     */
    OpenScienceFrameworkApiOauth2Scope findOneScopeByScopePk(final Integer scopePk);

    /**
     * Find one personal access token by token id (i.e the column token_id, not the primary key id).
     *
     * @param tokenId the token id
     * @return OpenScienceFrameworkApiOauth2PersonalAccessToken or null
     */
    OpenScienceFrameworkApiOauth2PersonalAccessToken findOnePersonalAccessTokenByTokenId(final String tokenId);

    /**
     * Find a list of active oauth applications.
     *
     * @return OpenScienceFrameworkApiOauth2Application List or null
     */
    List<OpenScienceFrameworkApiOauth2Application> findOauthApplications();

    /**
     * Find the guid object associated with the user.
     *
     * @param user the user
     * @return the GUID object
     */
    OpenScienceFrameworkGuid findGuidByUser(final OpenScienceFrameworkUser user);

    /**
     * Fine all the token-scope relationships by the token's primary key.
     *
     * @param tokenPk the token's primary key
     * @return OpenScienceFrameworkApiOauth2TokenScope List or null
     */
    List<OpenScienceFrameworkApiOauth2TokenScope> findAllTokenScopesByTokenPk(final Integer tokenPk);
}
