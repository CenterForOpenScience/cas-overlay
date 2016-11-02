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
package io.cos.cas.web.flow;

import java.util.ArrayList;
import java.util.List;

import io.cos.cas.authentication.exceptions.LoginNotAllowedException;
import io.cos.cas.authentication.exceptions.OneTimePasswordFailedLoginException;
import io.cos.cas.authentication.exceptions.OneTimePasswordRequiredException;
import io.cos.cas.authentication.exceptions.RemoteUserFailedLoginException;
import io.cos.cas.authentication.exceptions.RegistrationFailureUserAlreadyExistsException;
import io.cos.cas.authentication.exceptions.RegistrationSuccessConfirmationRequiredException;
import org.jasig.cas.web.flow.AuthenticationExceptionHandler;

/**
 * The Open Science Framework authentication exception handler.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public class OpenScienceFrameworkAuthenticationExceptionHandler extends AuthenticationExceptionHandler {

    /** Default list of errors this class knows how to handle. */
    private static final List<Class<? extends Exception>> DEFAULT_ERROR_LIST = new ArrayList<>();

    static {
        DEFAULT_ERROR_LIST.add(javax.security.auth.login.AccountLockedException.class);
        DEFAULT_ERROR_LIST.add(javax.security.auth.login.FailedLoginException.class);
        DEFAULT_ERROR_LIST.add(javax.security.auth.login.CredentialExpiredException.class);
        DEFAULT_ERROR_LIST.add(javax.security.auth.login.AccountNotFoundException.class);
        DEFAULT_ERROR_LIST.add(org.jasig.cas.authentication.AccountDisabledException.class);
        DEFAULT_ERROR_LIST.add(org.jasig.cas.authentication.InvalidLoginLocationException.class);
        DEFAULT_ERROR_LIST.add(org.jasig.cas.authentication.AccountPasswordMustChangeException.class);
        DEFAULT_ERROR_LIST.add(org.jasig.cas.authentication.InvalidLoginTimeException.class);
        // Open Science Framework Exceptions
        DEFAULT_ERROR_LIST.add(LoginNotAllowedException.class);
        DEFAULT_ERROR_LIST.add(RemoteUserFailedLoginException.class);
        // One Time Password Exceptions
        DEFAULT_ERROR_LIST.add(OneTimePasswordFailedLoginException.class);
        DEFAULT_ERROR_LIST.add(OneTimePasswordRequiredException.class);
        // Account Creation Exceptions
        DEFAULT_ERROR_LIST.add(RegistrationFailureUserAlreadyExistsException.class);
        DEFAULT_ERROR_LIST.add(RegistrationSuccessConfirmationRequiredException.class);
    }

    /**
     * The Open Science Framework Authentication Exception Handler.
     */
    public OpenScienceFrameworkAuthenticationExceptionHandler() {
        super.setErrors(DEFAULT_ERROR_LIST);
    }
}
