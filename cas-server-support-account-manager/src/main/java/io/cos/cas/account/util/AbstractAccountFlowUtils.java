package io.cos.cas.account.util;

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
     * Get and Encode the Service URL from request.
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
}
