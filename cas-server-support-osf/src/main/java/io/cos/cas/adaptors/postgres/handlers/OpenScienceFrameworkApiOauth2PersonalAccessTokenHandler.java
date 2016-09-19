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
import org.jasig.cas.support.oauth.personal.PersonalAccessToken;
import org.jasig.cas.support.oauth.personal.handler.support.AbstractPersonalAccessTokenHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashSet;

/**
 * The Open Science FrameWork API OAuth2 Personal Access Token Handler.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.0
 */
public class OpenScienceFrameworkApiOauth2PersonalAccessTokenHandler extends AbstractPersonalAccessTokenHandler
        implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(OpenScienceFrameworkApiOauth2PersonalAccessTokenHandler.class);

    @NotNull
    private OpenScienceFrameworkDaoImpl openScienceFrameworkDao;

    /** Default Constructor */
    public OpenScienceFrameworkApiOauth2PersonalAccessTokenHandler() {}

    public void setOpenScienceFrameworkDao(OpenScienceFrameworkDaoImpl openScienceFrameworkDao) {
        this.openScienceFrameworkDao = openScienceFrameworkDao;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public PersonalAccessToken getToken(final String tokenId) {
        final OpenScienceFrameworkApiOauth2PersonalAccessToken token = openScienceFrameworkDao.findOnePersonalAccessTokenByTokenId(tokenId);

        if (token == null || !token.isActive()) {
            return null;
        }

        final String scopes = token.getScopes() == null ? "" : token.getScopes();
        return new PersonalAccessToken(token.getTokenId(), token.getOwner().getUsername(), new HashSet<>(Arrays.asList(scopes.split(" "))));
    }

}
