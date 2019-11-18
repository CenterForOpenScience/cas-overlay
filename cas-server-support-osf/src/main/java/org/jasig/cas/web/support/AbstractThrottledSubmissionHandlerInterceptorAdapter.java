/*
 * Copyright (c) 2018. Center for Open Science
 *
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
package org.jasig.cas.web.support;

import io.cos.cas.web.flow.OpenScienceFrameworkAuthenticationExceptionHandler;
import io.cos.cas.web.flow.OpenScienceFrameworkLoginHandler;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.http.HttpStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Abstract implementation of the handler that has all of the logic.
 *
 * @author Scott Battaglia
 * @author Longze Chen
 * @since 3.3.5
 */
public abstract class AbstractThrottledSubmissionHandlerInterceptorAdapter
        extends HandlerInterceptorAdapter implements InitializingBean {

    /** Default number of request allowed per time range. */
    private static final int DEFAULT_FAILURE_THRESHOLD = 3;

    /** Default time range in seconds. */
    private static final int DEFAULT_FAILURE_RANGE_IN_SECONDS = 1;

    /** Default parameter name for  username. */
    private static final String DEFAULT_USERNAME_PARAMETER = "username";

    /** The logger. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Min(0)
    private int failureThreshold = DEFAULT_FAILURE_THRESHOLD;

    @Min(0)
    private int failureRangeInSeconds = DEFAULT_FAILURE_RANGE_IN_SECONDS;

    @NotNull
    private String usernameParameter = DEFAULT_USERNAME_PARAMETER;

    private double thresholdRate;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.thresholdRate = (double) failureThreshold / (double) failureRangeInSeconds;
    }

    @Override
    public final boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object o
    ) throws Exception {

        // Only throttle post request
        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            return true;
        }

        // Check if request exceeds the limit
        if (exceedsThreshold(request)) {
            recordThrottle(request);
            request.setAttribute(WebUtils.CAS_ACCESS_DENIED_REASON, "screen.blocked.message");
            final String errorMessage = "Login is temporarily disabled for the following user. "
                    + "Please wait for a few minutes before trying again."
                    + "<br/><span style=\"white-space: nowrap\">Email: <b>"
                    + request.getParameter(usernameParameter)
                    + "</b></span>"
                    + "<br/><span style=\"white-space: nowrap\">IP Address: <b>"
                    + request.getRemoteAddr()
                    + "</b></span>";
            response.sendError(HttpStatus.SC_FORBIDDEN, errorMessage);
            return false;
        }

        return true;
    }

    @Override
    public final void postHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object o, final ModelAndView modelAndView
    ) throws Exception {

        // Only check post request for counting against rate limiting
        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            return;
        }

        // Retrieve flow request context. Only continue if it exists.
        final RequestContext context = (RequestContext) request.getAttribute("flowRequestContext");
        if (context == null || context.getCurrentEvent() == null) {
            logger.debug("Flow request context not found.");
            return;
        }

        // Retrieve the login context. Use `try ... catch ...` due to `.getFlowScope()` may not be available.
        final String loginContext;
        try {
            loginContext = (String) context.getFlowScope().get("jsonLoginContext");
        } catch (final IllegalStateException e) {
            logger.warn(e.getMessage());
            return;
        }

        // Retrieve the login context. Only continue if exists.
        final OpenScienceFrameworkLoginHandler.OpenScienceFrameworkLoginContext osfLoginContext
                = OpenScienceFrameworkLoginHandler.OpenScienceFrameworkLoginContext.fromJson(loginContext);
        if (osfLoginContext == null) {
            logger.debug("Login context not found");
            return;
        }

        // No authentication exception has happened
        if (osfLoginContext.getHandleErrorName() == null) {
            return;
        }

        // Record the login failure if the exception count against the rate limiting
        if (OpenScienceFrameworkAuthenticationExceptionHandler.countAgainstLoginRateLimiting(osfLoginContext.getHandleErrorName())) {
            recordSubmissionFailure(request);
        }

        // Clear the exception, update login context and flow request context
        osfLoginContext.setHandleErrorName(null);
        context.getFlowScope().put("jsonLoginContext", osfLoginContext.toJson());
    }

    protected double getThresholdRate() {
        return this.thresholdRate;
    }

    protected int getFailureThreshold() {
        return this.failureThreshold;
    }

    public final void setFailureThreshold(final int failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    protected int getFailureRangeInSeconds() {
        return this.failureRangeInSeconds;
    }

    public final void setFailureRangeInSeconds(final int failureRangeInSeconds) {
        this.failureRangeInSeconds = failureRangeInSeconds;
    }

    protected String getUsernameParameter() {
        return this.usernameParameter;
    }

    public final void setUsernameParameter(final String usernameParameter) {
        this.usernameParameter = usernameParameter;
    }

    /**
     * Record throttling event.
     *
     * @param request the request
     */
    protected void recordThrottle(final HttpServletRequest request) {
        logger.warn(
                "Throttling submission from {}. Login rate goes above threshold rate: {} logins/second.",
                request.getRemoteAddr(),
                thresholdRate
        );
    }

    /**
     * Record submission failure.
     *
     * @param request the request
     */
    protected abstract void recordSubmissionFailure(HttpServletRequest request);

    /**
     * Determine whether threshold has been exceeded.
     *
     * @param request the request
     * @return true, if successful
     */
    protected abstract boolean exceedsThreshold(HttpServletRequest request);


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("failureThreshold", this.failureThreshold)
                .append("failureRangeInSeconds", this.failureRangeInSeconds)
                .append("usernameParameter", this.usernameParameter)
                .append("thresholdRate", this.thresholdRate)
                .toString();
    }
}
