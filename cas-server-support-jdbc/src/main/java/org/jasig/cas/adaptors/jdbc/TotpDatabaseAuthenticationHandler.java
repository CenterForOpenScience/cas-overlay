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
package org.jasig.cas.adaptors.jdbc;

import java.security.GeneralSecurityException;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
// import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.AbstractAuthenticationHandler;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.OneTimePasswordCredential;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;

import org.springframework.util.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;

public class TotpDatabaseAuthenticationHandler extends AbstractAuthenticationHandler {
    /** Handler name. */
    // private String name;

    public TotpDatabaseAuthenticationHandler() {
    }

    @Override
    public HandlerResult authenticate(final Credential credential)
            throws GeneralSecurityException, PreventedException {
        final OneTimePasswordCredential otp = (OneTimePasswordCredential) credential;

        // return new HandlerResult(this, new BasicCredentialMetaData(otp),
        //     new DefaultPrincipalFactory().createPrincipal(otp.getId()));
        return new HandlerResult(this, new BasicCredentialMetaData(credential), this.principalFactory.createPrincipal("admin"), null);

        // return new HandlerResult(this, new BasicCredentialMetaData(credential));

        // throw new FailedLoginException("OTP NO! " + credential);
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OneTimePasswordCredential;
    }

    // @Override
    // public String getName() {
    //     if (StringUtils.hasText(this.name)) {
    //         return this.name;
    //     } else {
    //         return getClass().getSimpleName();
    //     }
    // }

    // public void setName(final String name) {
    //     this.name = name;
    // }
}
