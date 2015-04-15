// /*
//  * Licensed to Jasig under one or more contributor license
//  * agreements. See the NOTICE file distributed with this work
//  * for additional information regarding copyright ownership.
//  * Jasig licenses this file to you under the Apache License,
//  * Version 2.0 (the "License"); you may not use this file
//  * except in compliance with the License.  You may obtain a
//  * copy of the License at the following location:
//  *
//  *   http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing,
//  * software distributed under the License is distributed on an
//  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//  * KIND, either express or implied.  See the License for the
//  * specific language governing permissions and limitations
//  * under the License.
//  */
// package org.jasig.cas.adaptors.jdbc;
//
// import java.security.GeneralSecurityException;
//
// import org.jasig.cas.authentication.HandlerResult;
// import org.jasig.cas.authentication.PreventedException;
// import org.jasig.cas.authentication.UsernamePasswordCredential;
// import org.jasig.cas.authentication.principal.SimplePrincipal;
// import org.springframework.beans.factory.InitializingBean;
// import org.springframework.dao.DataAccessException;
// import org.springframework.dao.IncorrectResultSizeDataAccessException;
// import org.springframework.security.crypto.bcrypt.BCrypt;
//
// import javax.security.auth.login.AccountNotFoundException;
// import javax.security.auth.login.FailedLoginException;
// import javax.validation.constraints.NotNull;
//
// public class TotpDatabaseAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler
//         implements InitializingBean {
//
//     // @NotNull
//     // private String fieldUser;
//     //
//     // @NotNull
//     // private String fieldPassword;
//     //
//     // @NotNull
//     // private String tableUsers;
//
//     private String sql;
//
//     @Override
//     protected final HandlerResult authenticateUsernamePasswordInternal(final OneTimePasswordCredential credential)
//             throws GeneralSecurityException, PreventedException {
//
//         final String password = credential.getPassword();
//         // final String plainTextPassword = credential.getPassword();
//         // final String encryptedPassword;
//
//         throw new FailedLoginException(username + " invalid password.");
//
//         // try {
//         //     encryptedPassword = getJdbcTemplate().queryForObject(this.sql, String.class, username);
//         // } catch (final IncorrectResultSizeDataAccessException e) {
//         //     if (e.getActualSize() == 0) {
//         //         throw new AccountNotFoundException(username + " not found with SQL query");
//         //     } else {
//         //         throw new FailedLoginException("Multiple records found for " + username);
//         //     }
//         // } catch (final DataAccessException e) {
//         //     throw new PreventedException("SQL exception while executing query for " + username, e);
//         // }
//         // if (!BCrypt.checkpw(plainTextPassword, encryptedPassword)) {
//         //     throw new FailedLoginException(username + " invalid password.");
//         // }
//         // return createHandlerResult(credential, this.principalFactory.createPrincipal(username), null);
//     }
//
//     @Override
//     public void afterPropertiesSet() throws Exception {
//     //     this.sql = "SELECT " + this.fieldPassword +
//     //         " FROM " + this.tableUsers +
//     //         " WHERE LOWER(" + this.fieldUser + ") = LOWER(?) AND active = TRUE";
//     }
//
//     /**
//      * @param fieldPassword The fieldPassword to set.
//      */
//     // public final void setFieldPassword(final String fieldPassword) {
//     //     this.fieldPassword = fieldPassword;
//     // }
//     //
//     // /**
//     //  * @param fieldUser The fieldUser to set.
//     //  */
//     // public final void setFieldUser(final String fieldUser) {
//     //     this.fieldUser = fieldUser;
//     // }
//     //
//     // /**
//     //  * @param tableUsers The tableUsers to set.
//     //  */
//     // public final void setTableUsers(final String tableUsers) {
//     //     this.tableUsers = tableUsers;
//     // }
// }


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
