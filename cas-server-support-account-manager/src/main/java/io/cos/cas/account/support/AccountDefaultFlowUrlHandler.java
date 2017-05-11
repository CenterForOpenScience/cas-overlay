package io.cos.cas.account.support;

import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Extend the Default Spring Flow URL Handler to Add the Service Parameter when Building the Execution URL.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public final class AccountDefaultFlowUrlHandler extends DefaultFlowUrlHandler {

    private static final String DEFAULT_FLOW_EXECUTION_KEY_PARAMETER = "execution";
    private static final String DEFAULT_FLOW_SERVICE_PARAMETER = "service";

    /**
     * Get the Service Parameter from Request.
     *
     * @param request the request
     * @return the service url
     */
    private String getFlowService(final HttpServletRequest request) {
        return request.getParameter(DEFAULT_FLOW_SERVICE_PARAMETER);
    }

    @Override
    public String createFlowExecutionUrl(final String flowId, final String flowExecutionKey, final HttpServletRequest request) {
        final StringBuilder builder = new StringBuilder();
        builder.append(request.getRequestURI());
        builder.append('?');
        final Map<String, Object> flowParameters = new HashMap<>();
        flowParameters.put(DEFAULT_FLOW_EXECUTION_KEY_PARAMETER, flowExecutionKey);
        flowParameters.put(DEFAULT_FLOW_SERVICE_PARAMETER, getFlowService(request));
        appendQueryParameters(builder, flowParameters, getEncodingScheme(request));
        return builder.toString();
    }
}
