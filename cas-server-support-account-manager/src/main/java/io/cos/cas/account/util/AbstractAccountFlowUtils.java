package io.cos.cas.account.util;

import io.cos.cas.account.flow.AccountManager;

import io.cos.cas.web.util.AbstractFlowUtils;

import org.springframework.webflow.execution.RequestContext;

/**
 * Abstract Utility Class for the Account Flow, inherits {@link AbstractFlowUtils}.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public abstract class AbstractAccountFlowUtils extends AbstractFlowUtils {

    /** The Request Parameter Name of the Target Action for Find Account Flow. */
    private static final String PARAM_TARGET = "target";

    /**
     * Get the Account Manager from Flow Scope in Request Context.
     *
     * @param context the request context
     * @return the account manager
     */
    public static AccountManager getAccountManagerFromRequestContext(final RequestContext context) {
        return AccountManager.fromJson((String) context.getFlowScope().get(AccountManager.ATTRIBUTE_NAME));
    }

    /**
     * Put the Account Manager to Flow Scope in Request Context.
     *
     * @param context the request context
     * @param manager the account manager
     */
    public static void putAccountManagerToRequestContext(final RequestContext context, final AccountManager manager) {
        context.getFlowScope().put(AccountManager.ATTRIBUTE_NAME, manager.toJson());
    }

    /**
     * Get the Target Action from the Request Context.
     *
     * @param context the request context
     * @return the target action
     */
    public static String getTargetFromRequestContext(final RequestContext context) {
        return context.getRequestParameters().get(PARAM_TARGET);
    }
}
