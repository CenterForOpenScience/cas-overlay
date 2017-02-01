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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.cos.cas.authentication.exceptions.LoginNotAllowedException;
import io.cos.cas.authentication.exceptions.OneTimePasswordFailedLoginException;
import io.cos.cas.authentication.exceptions.OneTimePasswordRequiredException;
import io.cos.cas.authentication.exceptions.RemoteUserFailedLoginException;
import io.cos.cas.authentication.exceptions.RegistrationFailureUserAlreadyExistsException;
import io.cos.cas.authentication.exceptions.RegistrationSuccessConfirmationRequiredException;
import io.cos.cas.authentication.exceptions.UserAlreadyMergedException;
import io.cos.cas.authentication.exceptions.UserNotActiveException;
import io.cos.cas.authentication.exceptions.UserNotClaimedException;
import io.cos.cas.authentication.exceptions.UserNotConfirmedException;

import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.AccountPasswordMustChangeException;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.InvalidLoginLocationException;
import org.jasig.cas.authentication.InvalidLoginTimeException;

// TODO: move this to io.cos.cas.authentication.exceptions after fix merge conflicts
import io.cos.cas.authentication.ShouldNotHappenException;

import org.jasig.cas.web.flow.AuthenticationExceptionHandler;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;

/**
 * The Open Science Framework authentication exception handler.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.0
 */
public class OpenScienceFrameworkAuthenticationExceptionHandler extends AuthenticationExceptionHandler {

    /** Default list of errors this class knows how to handle. */
    private static final List<Class<? extends Exception>> DEFAULT_ERROR_LIST = new ArrayList<>();

    /** A set of errors that trigger throttle increase. */
    private static final Set<String> THROTTLE_INCREASE_SET = new HashSet<>();

    /** A set of errors that is caused by invalid user status. */
    private static final Set<String> INVALID_USER_STATUS_SET = new HashSet<>();

    static {
        DEFAULT_ERROR_LIST.add(AccountLockedException.class);
        DEFAULT_ERROR_LIST.add(CredentialExpiredException.class);
        DEFAULT_ERROR_LIST.add(InvalidLoginLocationException.class);
        DEFAULT_ERROR_LIST.add(AccountPasswordMustChangeException.class);
        DEFAULT_ERROR_LIST.add(InvalidLoginTimeException.class);

        // Open Science Framework Exceptions

        // Account Creation Exceptions
        DEFAULT_ERROR_LIST.add(RegistrationFailureUserAlreadyExistsException.class);
        DEFAULT_ERROR_LIST.add(RegistrationSuccessConfirmationRequiredException.class);

        // Login Exceptions: Account not found, invalid password or verification key
        DEFAULT_ERROR_LIST.add(AccountNotFoundException.class);
        DEFAULT_ERROR_LIST.add(FailedLoginException.class);

        // TODO: udpate the error list after fix merge conflicts
        DEFAULT_ERROR_LIST.add(LoginNotAllowedException.class);
        DEFAULT_ERROR_LIST.add(ShouldNotHappenException.class);

        // Login Exceptions: Remote login failure
        DEFAULT_ERROR_LIST.add(RemoteUserFailedLoginException.class);

        // Login Exceptions: Two factor required or failed
        DEFAULT_ERROR_LIST.add(OneTimePasswordFailedLoginException.class);
        DEFAULT_ERROR_LIST.add(OneTimePasswordRequiredException.class);

        // Login Exceptions: Invalid user status
        DEFAULT_ERROR_LIST.add(UserAlreadyMergedException.class);
        DEFAULT_ERROR_LIST.add(UserNotActiveException.class);
        DEFAULT_ERROR_LIST.add(UserNotClaimedException.class);
        DEFAULT_ERROR_LIST.add(UserNotConfirmedException.class);
        DEFAULT_ERROR_LIST.add(AccountDisabledException.class);
    }

    static {
        INVALID_USER_STATUS_SET.add(UserAlreadyMergedException.class.getSimpleName());
        INVALID_USER_STATUS_SET.add(UserNotActiveException.class.getSimpleName());
        INVALID_USER_STATUS_SET.add(UserNotClaimedException.class.getSimpleName());
        INVALID_USER_STATUS_SET.add(UserNotConfirmedException.class.getSimpleName());
        INVALID_USER_STATUS_SET.add(AccountDisabledException.class.getSimpleName());
    }

    static {
        // Username does not exist
        THROTTLE_INCREASE_SET.add(AccountNotFoundException.class.getSimpleName());
        // Wrong password
        THROTTLE_INCREASE_SET.add(FailedLoginException.class.getSimpleName());
        // Wrong one time password
        THROTTLE_INCREASE_SET.add(OneTimePasswordFailedLoginException.class.getSimpleName());
    }

    /**
     * The Open Science Framework Authentication Exception Handler.
     */
    public OpenScienceFrameworkAuthenticationExceptionHandler() {
        super.setErrors(DEFAULT_ERROR_LIST);
    }

    /**
     * Check if the authentication exception should trigger throttle increase.
     *
     * @param handleErrorName the simple name of the exception
     * @return true if trigger, false otherwise
     */
    public static Boolean isTriggerThrottleIncrease(final String handleErrorName) {
        return THROTTLE_INCREASE_SET.contains(handleErrorName);
    }

    /**
     * Check if the authentication exception is caused by invalid user status.
     *
     * @param handleErrorName the simple name of the exception
     * @return true if user status invalid, false otherwise
     */
    public static Boolean isInvalidUserStatus(final String handleErrorName) {
        return handleErrorName != null && INVALID_USER_STATUS_SET.contains(handleErrorName);
    }

    /**
     * The authentication exception handler event.
     * 1. handle authentication exception
     * 2. recognize those failures that should trigger login throttle
     * 3. update flow scope in context
     *
     * @param context the request context
     * @param e the authentication exception
     * @param messageContext the message context
     * @return an Event with the name of the exception
     */
    public Event preHandle(final RequestContext context, final AuthenticationException e, final MessageContext messageContext) {
        final String handleErrorName = super.handle(e, messageContext);

        final String loginContext = (String) context.getFlowScope().get("jsonLoginContext");
        final OpenScienceFrameworkLoginHandler.OpenScienceFrameworkLoginContext osfLoginContext
                = OpenScienceFrameworkLoginHandler.OpenScienceFrameworkLoginContext.fromJson(loginContext);
        if (osfLoginContext != null) {
            osfLoginContext.setHandleErrorName(handleErrorName);
            context.getFlowScope().put("jsonLoginContext", osfLoginContext.toJson());
        }


        return new Event(this, handleErrorName);
    }
}
