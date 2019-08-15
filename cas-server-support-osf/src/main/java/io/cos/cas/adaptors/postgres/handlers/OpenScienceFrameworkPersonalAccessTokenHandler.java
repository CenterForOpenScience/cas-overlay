/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

package io.cos.cas.adaptors.postgres.handlers;

import io.cos.cas.adaptors.postgres.daos.OpenScienceFrameworkDaoImpl;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkApiOauth2PersonalAccessToken;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkApiOauth2Scope;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkApiOauth2TokenScope;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkGuid;

import org.jasig.cas.support.oauth.personal.PersonalAccessToken;
import org.jasig.cas.support.oauth.personal.handler.support.AbstractPersonalAccessTokenHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InitializingBean;

import javax.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The OSF API OAuth2 Personal Access Token Handler.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public class OpenScienceFrameworkPersonalAccessTokenHandler extends AbstractPersonalAccessTokenHandler
        implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkPersonalAccessTokenHandler.class);

    @NotNull
    private OpenScienceFrameworkDaoImpl openScienceFrameworkDao;

    /** Default Constructor. */
    public OpenScienceFrameworkPersonalAccessTokenHandler() {}

    /**
     * @param openScienceFrameworkDao the open science framework data access object.
     */
    public void setOpenScienceFrameworkDao(final OpenScienceFrameworkDaoImpl openScienceFrameworkDao) {
        this.openScienceFrameworkDao = openScienceFrameworkDao;
    }

    @Override
    public void afterPropertiesSet() throws Exception {}

    @Override
    public PersonalAccessToken getToken(final String tokenId) {

        // Find the token by token id
        final OpenScienceFrameworkApiOauth2PersonalAccessToken token
                = openScienceFrameworkDao.findOnePersonalAccessTokenByTokenId(tokenId);
        if (token == null || !token.isActive()) {
            return null;
        }

        // Find the scopes associated with this token
        final List<OpenScienceFrameworkApiOauth2TokenScope> tokenScopeList
                = openScienceFrameworkDao.findAllTokenScopesByTokenPk(token.getId());
        final Set<String> scopeSet = new HashSet<>();
        for (final OpenScienceFrameworkApiOauth2TokenScope tokenScope : tokenScopeList) {
            final OpenScienceFrameworkApiOauth2Scope scope
                    = openScienceFrameworkDao.findOneScopeByScopePk(tokenScope.getScopePk());
            if (scope != null) {
                scopeSet.add(scope.getName());
            }
        }

        // Find the owner of the token
        final OpenScienceFrameworkGuid guid = openScienceFrameworkDao.findGuidByUser(token.getOwner());
        if (guid == null) {
            return null;
        }

        // Return a PAT of the CAS model, which is created based on the token, scope and owner of the OSF model.
        return new PersonalAccessToken(token.getTokenId(), guid.getGuid(), scopeSet);
    }
}
