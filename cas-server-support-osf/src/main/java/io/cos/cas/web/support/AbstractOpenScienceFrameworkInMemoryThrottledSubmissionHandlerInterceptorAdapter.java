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
package io.cos.cas.web.support;

import org.jasig.cas.web.support.AbstractThrottledSubmissionHandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Implementation of a HandlerInterceptorAdapter that keeps track of a mapping of login request to a queue of failed
 * attempts. This is our improved version of Apereo's AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter.
 *
 * Login will be throttled if the queue is full. It relies on an external method which runs periodically to remove the
 * oldest attempt from the queue and runs independent of the threshold of the parent.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
public abstract class AbstractOpenScienceFrameworkInMemoryThrottledSubmissionHandlerInterceptorAdapter
        extends AbstractThrottledSubmissionHandlerInterceptorAdapter {

    private final ConcurrentMap<String, ConcurrentLinkedQueue<Date>> throttlerMap = new ConcurrentHashMap<>();

    @Override
    protected final boolean exceedsThreshold(final HttpServletRequest request) {
        final ConcurrentLinkedQueue<Date> dateQueue = this.throttlerMap.get(constructKey(request));
        // throttle login if the dateQueue is full
        return dateQueue != null && dateQueue.size() >= getFailureThreshold();
    }

    @Override
    protected final void recordSubmissionFailure(final HttpServletRequest request) {
        final String requestSignature = constructKey(request);
        ConcurrentLinkedQueue<Date> dateQueue = this.throttlerMap.get(requestSignature);
        if (dateQueue == null) {
            dateQueue = new ConcurrentLinkedQueue<>();
        }
        if (dateQueue.size() >= getFailureThreshold()) {
            dateQueue.poll();
        }
        dateQueue.add(new Date());
        this.throttlerMap.put(requestSignature, dateQueue);
    }

    @Override
    protected void recordThrottle(final HttpServletRequest request) {
        logger.warn("Authentication attempt from {} exceeds the failure threshold {}",
                constructKey(request), getFailureThreshold());
    }

    /**
     * This method is called by an external job which runs periodically to release the throttle.
     */
    public final void decrementCounts() {

        for (final Map.Entry<String, ConcurrentLinkedQueue<Date>> entry : this.throttlerMap.entrySet()) {
            if (entry != null && entry.getValue() != null && !entry.getValue().isEmpty()) {
                logger.info("Login Throttle Map Entry and Queue Size: <{}, {}>", entry.getKey(), entry.getValue().size());
                entry.getValue().poll();
            }
        }
        logger.info("Done decrementing throttler count for all user.");
    }

    /**
     * Construct key to be used by the throttling agent to track requests.
     *
     * @param request the request
     * @return the string
     */
    protected abstract String constructKey(HttpServletRequest request);
}
