package io.cos.cas.account.util;

import io.cos.cas.account.flow.AccountManager;
import io.cos.cas.web.util.AbstractFlowUtils;

import org.json.JSONObject;
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

    /**
     * Parse API 200 Response, build and set login redirect url with username and verification key.
     *
     * @param requestContext the request context
     * @param responseBody the response body
     * @param casLoginUrl the cas login url
     * @param username the username
     * @return <code>true</code> if response is valid, <code>false</code> otherwise
     */
    public static boolean verifyResponseAndPutLoginRedirectUrlToRequestContext(
            final RequestContext requestContext,
            final JSONObject responseBody,
            final String casLoginUrl,
            final String username
    ) {
        if (responseBody != null && responseBody.has("verificationKey") && responseBody.has("serviceUrl")) {
            final String redirectUrl = String.format(
                    "%sservice=%s&username=%s&verification_key=%s",
                    casLoginUrl,
                    encodeUrlParameter(responseBody.getString("serviceUrl")),
                    encodeUrlParameter(username),
                    encodeUrlParameter(responseBody.getString("verificationKey"))
            );
            requestContext.getFlowScope().put("loginRedirectUrl", redirectUrl);
            return true;
        }
        return false;
    }
}
