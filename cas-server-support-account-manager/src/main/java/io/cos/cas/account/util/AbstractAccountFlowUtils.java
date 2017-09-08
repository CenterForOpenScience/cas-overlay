package io.cos.cas.account.util;

import io.cos.cas.account.flow.AccountManager;
import io.cos.cas.web.util.AbstractFlowUtils;

import org.apache.commons.lang.StringUtils;

import org.jasig.cas.web.support.WebUtils;

import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.webflow.execution.RequestContext;

/**
 * Abstract Utility Class for the Account Flow, inherits {@link AbstractFlowUtils}.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public abstract class AbstractAccountFlowUtils extends AbstractFlowUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAccountFlowUtils.class);

    /** The Request Parameter Name of the Target Action for Find Account Flow. */
    private static final String PARAM_TARGET = "target";

    /** The Request Parameter Name of the User's GUID for Find Account Flow. */
    private static final String PARAM_USER_ID = "user";

    /** The Request Parameter Name of the OSF4Meeting flag for Find Account Flow. */
    private static final String PARAM_MEETINGS = "osf4Meetings";

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
        return context.getRequestParameters().get(PARAM_TARGET, StringUtils.EMPTY);
    }

    /**
     * Get the user's GUID from the Request Context.
     *
     * @param context the request context
     * @return the user's GUID
     */
    public static String getUserIdFromRequestContext(final RequestContext context) {
        return context.getRequestParameters().get(PARAM_USER_ID, StringUtils.EMPTY);
    }

    /**
     * Get the OSF4Meetings flag from the Request Context.
     *
     * @param context the request context
     * @return the boolean flag
     */
    public static boolean getMeetingsFromRequestContext(final RequestContext context) {
        return context.getRequestParameters().getBoolean(PARAM_MEETINGS, Boolean.FALSE);
    }

    /**
     * Parse API 200 Response, build and set login redirect url with username and verification key.
     *
     * @param requestContext the request context
     * @param responseBody the response body
     * @param casLoginUrl the cas login url
     * @param osfCasActionUrl the osf cas action url
     * @return <code>true</code> if response is valid, <code>false</code> otherwise
     */
    public static boolean verifyResponseAndPutLoginRedirectUrlToRequestContext(
            final RequestContext requestContext,
            final JSONObject responseBody,
            final String casLoginUrl,
            final String osfCasActionUrl
    ) {
        if (responseBody != null) {
            if (responseBody.has("verificationKey")
                    && responseBody.has("userId")
                    && responseBody.has("casAction")
                    && responseBody.has("nextUrl")
                    && responseBody.has("username")
                    ) {
                final boolean nextUrl = responseBody.getBoolean("nextUrl");
                String serviceUrl = String.format(
                        "%s%s/?action=%s",
                        osfCasActionUrl,
                        responseBody.getString("userId"),
                        responseBody.getString("casAction")
                );
                if (nextUrl) {
                    serviceUrl += String.format("&next=%s", getEncodedServiceUrl(requestContext));
                }
                final String redirectUrl = String.format(
                        "%susername=%s&verification_key=%s&service=%s",
                        casLoginUrl,
                        encodeUrlParameter(responseBody.getString("username")),
                        encodeUrlParameter(responseBody.getString("verificationKey")),
                        encodeUrlParameter(serviceUrl)
                );
                requestContext.getFlowScope().put("loginRedirectUrl", redirectUrl);
                return true;
            }
        }
        return false;
    }

    /**
     * Tries to to determine if the login ticket in the request flow scope matches the login ticket provided by the
     * request. The comparison is case-sensitive.
     *
     * @param context the context
     * @return true if valid
     */
    public static boolean checkLoginTicketIfExists(final RequestContext context) {
        final String loginTicketFromFlowScope = WebUtils.getLoginTicketFromFlowScope(context);
        final String loginTicketFromRequest = WebUtils.getLoginTicketFromRequest(context);

        LOGGER.trace(
                "Comparing login ticket in the flow scope [{}] with login ticket in the request [{}]",
                loginTicketFromFlowScope,
                loginTicketFromRequest
        );
        return StringUtils.equals(loginTicketFromFlowScope, loginTicketFromRequest);
    }
}
