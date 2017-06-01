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

import io.cos.cas.authentication.OpenScienceFrameworkCredential;
import io.cos.cas.authentication.exceptions.InvalidVerificationKeyException;
import io.cos.cas.authentication.exceptions.OneTimePasswordFailedLoginException;
import io.cos.cas.authentication.exceptions.OneTimePasswordRequiredException;
import io.cos.cas.authentication.exceptions.RemoteUserFailedLoginException;
import io.cos.cas.authentication.exceptions.ShouldNotHappenException;
import io.cos.cas.authentication.exceptions.AccountNotVerifiedException;

import io.cos.cas.web.util.AbstractFlowUtils;
import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.AccountPasswordMustChangeException;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.InvalidLoginLocationException;
import org.jasig.cas.authentication.InvalidLoginTimeException;
import org.jasig.cas.web.flow.AuthenticationExceptionHandler;

import org.jasig.cas.web.support.WebUtils;
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
 * @since 4.1.5
 */
public class OpenScienceFrameworkAuthenticationExceptionHandler extends AuthenticationExceptionHandler {

    /** Default list of errors this class knows how to handle. */
    private static final List<Class<? extends Exception>> DEFAULT_ERROR_LIST = new ArrayList<>();

    /** A set of errors that trigger throttle increase. */
    private static final Set<String> THROTTLE_INCREASE_SET = new HashSet<>();

    //  Built-in Exceptions that are not explicitly used
    static {
        DEFAULT_ERROR_LIST.add(AccountPasswordMustChangeException.class);
        DEFAULT_ERROR_LIST.add(AccountLockedException.class);
        DEFAULT_ERROR_LIST.add(CredentialExpiredException.class);
        DEFAULT_ERROR_LIST.add(InvalidLoginLocationException.class);
        DEFAULT_ERROR_LIST.add(InvalidLoginTimeException.class);
    }

    // Open Science Framework Exceptions
    static {

        // Login Exceptions
        DEFAULT_ERROR_LIST.add(AccountDisabledException.class);
        DEFAULT_ERROR_LIST.add(AccountNotFoundException.class);
        DEFAULT_ERROR_LIST.add(AccountNotVerifiedException.class);
        DEFAULT_ERROR_LIST.add(FailedLoginException.class);
        DEFAULT_ERROR_LIST.add(InvalidVerificationKeyException.class);
        DEFAULT_ERROR_LIST.add(ShouldNotHappenException.class);

        // Remote Login Exceptions
        DEFAULT_ERROR_LIST.add(RemoteUserFailedLoginException.class);

        // Two factor Login Exceptions
        DEFAULT_ERROR_LIST.add(OneTimePasswordFailedLoginException.class);
        DEFAULT_ERROR_LIST.add(OneTimePasswordRequiredException.class);
    }

    // Login Throttle Triggering Exceptions
    static {
        THROTTLE_INCREASE_SET.add(AccountNotFoundException.class.getSimpleName());
        THROTTLE_INCREASE_SET.add(FailedLoginException.class.getSimpleName());
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
     * The authentication exception handler event.
     * Record the exception and put it in `jsonLoginContext` in flow scope.
     *
     * @param requestContext the request context
     * @param e the authentication exception
     * @param messageContext the message context
     * @return an Event with the name of the exception
     */
    public Event preHandle(
            final RequestContext requestContext,
            final AuthenticationException e,
            final MessageContext messageContext
    ) {
        final String handleErrorName = super.handle(e, messageContext);
        final LoginManager loginMangerContext
                = AbstractFlowUtils.getLoginManagerFromRequestContext(requestContext);
        if (loginMangerContext != null) {
            loginMangerContext.setHandleErrorName(handleErrorName);
            loginMangerContext.setUsername(((OpenScienceFrameworkCredential) WebUtils.getCredential(requestContext)).getUsername());
            AbstractFlowUtils.putLoginManagerToRequestContext(requestContext, loginMangerContext);
        }
        return new Event(this, handleErrorName);
    }
}
