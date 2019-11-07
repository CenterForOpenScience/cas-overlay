/*
 * Copyright (c) 2015. Center for Open Science
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jasig.cas.support.oauth.web;

import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.CentralOAuthService;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.token.AccessToken;
import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.web.DelegateController;
import org.jasig.cas.web.ServiceValidateController;
import org.jasig.cas.web.support.ArgumentExtractor;

import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * The OAuth 2.0 Service Validation Controller.
 *
 * This controller allows injection of an OAuth access token into the CAS protocol. With current CAS settings, this
 * is the base / parent controller class for the service validation process in both CAS 2.0 and 3.0 protocol, which
 * replaces the default {@link ServiceValidateController} by wrapping it into the class as a private property.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public class OAuth20ServiceValidateController extends DelegateController {

    /** The wrapped service validate controller. */
    private ServiceValidateController wrapped = new ServiceValidateController();

    /** The CAS OAuth authorization service. */
    @NotNull
    private CentralOAuthService centralOAuthService;

    /** The primary CAS authentication service. */
    @NotNull
    private CentralAuthenticationService centralAuthenticationService;

    /** The view to redirect to on a successful validation. */
    @NotNull
    private String successView;

    /**
     * Extracts parameters from a given {@link HttpServletRequest} object.
     */
    @NotNull
    private ArgumentExtractor argumentExtractor;

    /**
     * Initialize the application context.
     *
     * It calls the super {@link #initApplicationContext} first to initialize the application context. Then it sets the
     * context for the wrapped service validated controller {@link #wrapped}.
     *
     * Note: {@link #initApplicationContext} calls {@link #initServletContext} if the given {@link ApplicationContext}
     * object is an instance of {@link org.springframework.web.context.WebApplicationContext}.
     */
    @Override
    protected void initApplicationContext(final ApplicationContext context) {
        super.initApplicationContext(context);
        wrapped.setApplicationContext(context);
    }

    @Override
    public ModelAndView handleRequest(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final WebApplicationService service = this.argumentExtractor.extractService(request);
        final String serviceTicketId = service != null ? service.getArtifactId() : null;

        ServiceTicket serviceTicket = null;
        try {
            serviceTicket = this.centralAuthenticationService.getTicket(serviceTicketId, Ticket.class);
        } catch (final Exception e) {
            // ignore, wrapped object will handle error appropriately.
        }

        final ModelAndView modelAndView = wrapped.handleRequest(request, response);

        if (service != null && serviceTicket != null && modelAndView.getViewName().equals(this.successView)) {
            final AccessToken accessToken = centralOAuthService
                    .grantCASAccessToken(serviceTicket.getGrantingTicket(), serviceTicket.getService());
            modelAndView.addObject(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN, accessToken.getId());
            modelAndView.addObject(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN_SCOPE, accessToken.getScopes());
        }

        return modelAndView;
    }

    @Override
    protected ModelAndView handleRequestInternal(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse
    ) throws Exception {
        // This class must be implemented to meet the interface requirements. It is expected to be used by the
        // `handleRequest()` method from the `org.springframework.web.servlet.mvc.AbstractController`. However,
        // we have overridden and fully rewritten the that method above. There is no need for this method to do
        // anything at all and thus return `null`.
        return null;
    }

    /**
     * Determine if this class (i.e. a subclass of {@link DelegateController} can handle the current request.
     *
     * @param request the current request
     * @param response the response
     * @return true if the controller can handler the request, false otherwise
     */
    @Override
    public boolean canHandle(final HttpServletRequest request, final HttpServletResponse response) {
        return wrapped.canHandle(request, response);
    }

    /**
     * Set the primary CAS authentication service for this class and the {@link #wrapped} service validate controller.
     *
     * @param centralAuthenticationService the CAS authentication service to set
     */
    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
        wrapped.setCentralAuthenticationService(centralAuthenticationService);
    }

    /**
     * Set the CAS OAuth service.
     *
     * @param centralOAuthService the CAS OAuth service to set
     */
    public void setCentralOAuthService(final CentralOAuthService centralOAuthService) {
        this.centralOAuthService = centralOAuthService;
    }

    /**
     * Set the argument extractor for this class and the {@link #wrapped} service validate controller.
     *
     * @param argumentExtractor argument extractor to set
     */
    public void setArgumentExtractor(final ArgumentExtractor argumentExtractor) {
        this.argumentExtractor = argumentExtractor;
        wrapped.setArgumentExtractor(this.argumentExtractor);
    }

    /**
     * Set the validation specification class for the {@link #wrapped} service validate controller.
     *
     * @param validationSpecificationClass the validation specification class to set
     */
    public void setValidationSpecificationClass(final Class<?> validationSpecificationClass) {
        wrapped.setValidationSpecificationClass(validationSpecificationClass);
    }

    /**
     * Set the failure view for the {@link #wrapped} service validate controller.
     *
     * @param failureView the failure view to set
     */
    public void setFailureView(final String failureView) {
        wrapped.setFailureView(failureView);
    }

    /**
     * Set the success view for this class and the {@link #wrapped} service validate controller.
     *
     * @param successView the success view to set
     */
    public void setSuccessView(final String successView) {
        this.successView = successView;
        wrapped.setSuccessView(this.successView);
    }

    /**
     * Set the proxy handler for the {@link #wrapped} service validate controller.
     *
     * @param proxyHandler the proxy handler to set
     */
    public void setProxyHandler(final ProxyHandler proxyHandler) {
        wrapped.setProxyHandler(proxyHandler);
    }

    /**
     * Set the services manager for the {@link #wrapped} service validate controller.
     *
     * @param servicesManager the services manager to set
     */
    public void setServicesManager(final ServicesManager servicesManager) {
        wrapped.setServicesManager(servicesManager);
    }
}
