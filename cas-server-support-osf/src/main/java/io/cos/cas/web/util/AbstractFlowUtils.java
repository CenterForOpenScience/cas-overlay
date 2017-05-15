package io.cos.cas.web.util;

import io.cos.cas.web.flow.LoginManager;

import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredServiceProperty;

import org.springframework.webflow.execution.RequestContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Abstract Utility Class for the Login Web Flow.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public abstract class AbstractFlowUtils {

    /** The Default Server-side Error Message. */
    public static final String DEFAULT_SERVER_ERROR_MESSAGE = "Internal server error. Please try again later.";

    /** The Default Client-side Error Message. */
    public static final String DEFAULT_CLIENT_ERROR_MESSAGE = "Invalid client state. Please try again later.";

    /** The Request Parameter Name for Institution Login Flag in Login Web Flow. */
    private static final String PARAM_INSTITUTION = "institution";

    /**
     * Get the Login Manager from Flow Scope in Request Context.
     *
     * @param context the request context
     * @return the login manager
     */
    public static LoginManager getLoginManagerFromRequestContext(final RequestContext context) {
        return LoginManager.fromJson((String) context.getFlowScope().get(LoginManager.ATTRIBUTE_NAME));
    }

    /**
     * Put the Login Manager to Flow Scope in Request Context.
     *
     * @param context the request context
     * @param manager the login manager
     */
    public static void putLoginManagerToRequestContext(final RequestContext context, final LoginManager manager) {
        if (manager != null) {
            context.getFlowScope().put(LoginManager.ATTRIBUTE_NAME, manager.toJson());
        }
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
     * @param context the request context
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
     * Check and verify request parameter "institution".
     *
     * @param context the request context
     * @return <code>true</code> if the request has param "institution=true", false otherwise
     */
    public static boolean isInstitutionLogin(final RequestContext context) {
        final String institution = context.getRequestParameters().get(PARAM_INSTITUTION);
        return Boolean.TRUE.toString().equalsIgnoreCase(institution);
    }
}
