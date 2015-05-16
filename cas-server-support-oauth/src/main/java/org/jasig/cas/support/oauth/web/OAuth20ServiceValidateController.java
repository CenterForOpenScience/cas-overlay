/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.support.oauth.web;


import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthTokenUtils;
import org.jasig.cas.ticket.*;
import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.util.CipherExecutor;
import org.jasig.cas.web.DelegateController;
import org.jasig.cas.web.ServiceValidateController;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * This controller allows injection of an oauth access ticket into the CAS protocol.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public class OAuth20ServiceValidateController extends DelegateController {
    /** Wrapped Service Validate Controller. */
    private ServiceValidateController wrapped = new ServiceValidateController();

    /** The CORE which we will delegate all requests to. */
    @NotNull
    private CentralAuthenticationService centralAuthenticationService;

    /** The view to redirect to on a successful validation. */
    @NotNull
    private String successView;

    /** Extracts parameters from Request object. */
    @NotNull
    private ArgumentExtractor argumentExtractor;

    /** Instance of CipherExecutor. */
    @NotNull
    private CipherExecutor cipherExecutor;

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final WebApplicationService service = this.argumentExtractor.extractService(request);
        final String serviceTicketId = service != null ? service.getArtifactId() : null;
        final ServiceTicket serviceTicket = this.centralAuthenticationService.getTicket(serviceTicketId, Ticket.class);

        ModelAndView modelAndView = wrapped.handleRequest(request, response);

        if (service != null && modelAndView.getViewName().equals(this.successView)) {
            TicketGrantingTicket accessTicket = serviceTicket.getGrantingTicket();
            modelAndView.addObject(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN, OAuthTokenUtils.getJsonWebToken(cipherExecutor, accessTicket, service));
        }

        return modelAndView;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandle(final HttpServletRequest request, final HttpServletResponse response) {
        return wrapped.canHandle(request, response);
    }

    /**
     * @param centralAuthenticationService The centralAuthenticationService to
     * set.
     */
    public final void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
        wrapped.setCentralAuthenticationService(this.centralAuthenticationService);
    }


    public final void setArgumentExtractor(final ArgumentExtractor argumentExtractor) {
        this.argumentExtractor = argumentExtractor;
        wrapped.setArgumentExtractor(this.argumentExtractor);
    }

    /**
     * @param validationSpecificationClass The authenticationSpecificationClass
     * to set.
     */
    public final void setValidationSpecificationClass(final Class<?> validationSpecificationClass) {
        wrapped.setValidationSpecificationClass(validationSpecificationClass);
    }

    /**
     * @param failureView The failureView to set.
     */
    public final void setFailureView(final String failureView) {
        wrapped.setFailureView(failureView);
    }

    /**
     * @param successView The successView to set.
     */
    public final void setSuccessView(final String successView) {
        this.successView = successView;
        wrapped.setSuccessView(this.successView);
    }

    /**
     * @param proxyHandler The proxyHandler to set.
     */
    public final void setProxyHandler(final ProxyHandler proxyHandler) {
        wrapped.setProxyHandler(proxyHandler);
    }

    /**
     * Sets the services manager.
     *
     * @param servicesManager the new services manager
     */
    public final void setServicesManager(final ServicesManager servicesManager) {
        wrapped.setServicesManager(servicesManager);
    }

    public void setCipherExecutor(final CipherExecutor cipherExecutor) {
        this.cipherExecutor = cipherExecutor;
    }
}
