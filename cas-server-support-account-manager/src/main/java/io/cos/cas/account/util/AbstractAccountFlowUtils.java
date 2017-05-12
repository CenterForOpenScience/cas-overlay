package io.cos.cas.account.util;

import io.cos.cas.account.flow.AccountManager;

import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredServiceProperty;

import org.springframework.webflow.execution.RequestContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Abstract Utility Class for the Account Flow.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public abstract class AbstractAccountFlowUtils {

    /** The Default Server-side Error Message. */
    public static final String DEFAULT_SERVER_ERROR_MESSAGE = "Internal server error. Please try again later.";

    /** The Default Client-side Error Message. */
    public static final String DEFAULT_CLIENT_ERROR_MESSAGE = "Invalid client state. Please try again later.";

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
     * Get the OSF Campaign Name from the Registered Service .
     *
     * @param context the request context
     * @return the campaign name
     */
    public static String getCampaignFromRegisteredService(final RequestContext context) {
        final RegexRegisteredService registeredService = (RegexRegisteredService) context.getFlowScope().get("registeredService");
        if (registeredService != null) {
            final RegisteredServiceProperty campaign = registeredService.getProperties().get("campaign");
            if (campaign != null && !campaign.getValue().isEmpty()) {
                return campaign.getValue();
            }
        }
        return null;
    }

    /**
     * Get and Encode the Service URL from Request Context.
     *
     * @param context The request context

     * @return the encoded service url
     * @throws AssertionError if fails to encode the URL
     */
    public static String getEncodedServiceUrl(final RequestContext context) throws AssertionError {

        final String serviceUrl = context.getRequestParameters().get("service");
        if (serviceUrl == null || serviceUrl.isEmpty()) {
            return null;
        }
        try {
            return URLEncoder.encode(serviceUrl, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unknown");
        }
    }

    /**
     * Get the Target Action from the Request Context.
     *
     * @param context the request context
     * @return the target action
     */
    public static String getTargetFromRequestContext(final RequestContext context) {
        return context.getRequestParameters().get("target");
    }
}
