package io.cos.cas.web.support;


import org.jasig.cas.web.support.AbstractThrottledSubmissionHandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.Iterator;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public abstract class OpenScienceFrameworkAbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter
        extends AbstractThrottledSubmissionHandlerInterceptorAdapter {

    private final ConcurrentMap<String, ConcurrentLinkedQueue<Date>> ThrottlingQueueMap = new ConcurrentHashMap<>();

    @Override
    protected final boolean exceedsThreshold(final HttpServletRequest request) {
        final ConcurrentLinkedQueue<Date> queue = this.ThrottlingQueueMap.get(constructKey(request));
        if (queue == null) {
            return false;
        }
        return queue.size() >= getFailureThreshold();
    }

    @Override
    protected final void recordSubmissionFailure(final HttpServletRequest request) {
        String requestSignature = constructKey(request);
        ConcurrentLinkedQueue<Date> dateQueue = this.ThrottlingQueueMap.get(requestSignature);
        if (dateQueue == null) {
            dateQueue = new ConcurrentLinkedQueue<>();
        }
        else if (dateQueue.size() >= getFailureThreshold()) {
            dateQueue.poll();
        }
        dateQueue.add(new Date());
        this.ThrottlingQueueMap.put(requestSignature, dateQueue);
    }

    /**
     * Construct key to be used by the throttling agent to track requests.
     *
     * @param request the request
     * @return the string
     */
    protected abstract String constructKey(HttpServletRequest request);

    /**
     * This class relies on an external configuration to clean it up. It ignores the threshold data in the parent class.
     */
    public final void decrementCounts() {

        for (Map.Entry<String, ConcurrentLinkedQueue<Date>> entry : this.ThrottlingQueueMap.entrySet()) {
            if (entry != null && entry.getValue() != null && !entry.getValue().isEmpty()) {
                entry.getValue().poll();
                logger.info("{}\t{}", entry.getKey(), entry.getValue().size());
            }
        }
        logger.info("Done decrementing count for throttler.");
    }
}
