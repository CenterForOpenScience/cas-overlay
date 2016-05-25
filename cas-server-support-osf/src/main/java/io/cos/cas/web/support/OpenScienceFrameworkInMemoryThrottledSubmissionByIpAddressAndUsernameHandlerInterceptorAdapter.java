package io.cos.cas.web.support;

import org.jasig.inspektr.common.web.ClientInfoHolder;
import javax.servlet.http.HttpServletRequest;


public class OpenScienceFrameworkInMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter
        extends OpenScienceFrameworkAbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter{

    @Override
    protected String constructKey(final HttpServletRequest request) {
        final String username = request.getParameter(getUsernameParameter());

        if (username == null) {
            return request.getRemoteAddr();
        }

        return ClientInfoHolder.getClientInfo().getClientIpAddress() + ';' + username.toLowerCase();
    }
}
