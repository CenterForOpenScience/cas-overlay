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
package org.jasig.cas.support.oauth;

import org.jasig.cas.support.oauth.web.OAuth20AuthorizeCallbackActionControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20MetadataClientControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20MetadataPrincipalControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20RevokeClientPrincipalTokensControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20RevokeClientTokensControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20RevokeTokenControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20ServiceValidateControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20TokenAuthorizationCodeControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20AuthorizeControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20AuthorizeCallbackControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20ProfileControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20TokenRefreshTokenControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20WrapperControllerTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * OAuth test suite that runs all test in a batch.
 *
 * @author Misagh Moayyed
 * @author Longze Chen
 * @since 4.1.5
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        OAuth20WrapperControllerTests.class,
        OAuth20AuthorizeControllerTests.class,
        OAuth20AuthorizeCallbackControllerTests.class,
        OAuth20AuthorizeCallbackActionControllerTests.class,
        OAuth20TokenRefreshTokenControllerTests.class,
        OAuth20TokenAuthorizationCodeControllerTests.class,
        OAuth20ProfileControllerTests.class,
        OAuth20MetadataClientControllerTests.class,
        OAuth20MetadataPrincipalControllerTests.class,
        OAuth20RevokeTokenControllerTests.class,
        OAuth20RevokeClientTokensControllerTests.class,
        OAuth20RevokeClientPrincipalTokensControllerTests.class,
        OAuth20ServiceValidateControllerTests.class,
})
public class OAuthTestSuite {}
