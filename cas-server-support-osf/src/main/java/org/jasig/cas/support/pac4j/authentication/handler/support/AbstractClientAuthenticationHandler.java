/*
 * Copyright (c) 2019. Center for Open Science
 *
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
package org.jasig.cas.support.pac4j.authentication.handler.support;

import io.cos.cas.authentication.exceptions.DelegatedLoginException;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.jasig.cas.support.pac4j.authentication.principal.ClientCredential;

import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.UserProfile;

import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;

import java.security.GeneralSecurityException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * The Abstract Client Authentication Handler.
 *
 * This class is a generic handler for authentication delegated to an auth client, implementing the authentication
 * method {@link AbstractPreAndPostProcessingAuthenticationHandler#doAuthentication(Credential)}. It uses the auth
 * client and provides it with the client credentials in order to get the user profile returned by the provider for
 * an authenticated user.
 *
 * @author Jerome Leleu
 * @author Longze Chen
 * @since 4.1.0
 */
@SuppressWarnings("unchecked")
public abstract class AbstractClientAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    /** The factory to create the principal type. **/
    @NotNull
    protected PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    /** The clients for authentication. */
    @NotNull
    private final Clients clients;

    /**
     * Instantiate a new {@link AbstractClientAuthenticationHandler} and define the clients.
     *
     * @param theClients the clients for authentication
     */
    public AbstractClientAuthenticationHandler(final Clients theClients) {
        this.clients = theClients;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public final boolean supports(final Credential credential) {
        return credential != null && ClientCredential.class.isAssignableFrom(credential.getClass());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException {

        // Construct a specific client credential from a general credential
        final ClientCredential clientCredentials = (ClientCredential) credential;
        logger.debug("clientCredentials : {}", clientCredentials);

        // Retrieve the client name found in the client credential
        final Credentials credentials = clientCredentials.getCredentials();
        final String clientName = credentials.getClientName();
        logger.debug("clientName : {}", clientName);

        // Attempt to load the client from defined clients
        final Client<Credentials, UserProfile> client;
        try {
            client = this.clients.findClient(clientName);
            logger.debug("client : {}", client);
        } catch (final TechnicalException e) {
            logger.error("Invalid client: {}", clientName);
            throw new DelegatedLoginException("Invalid client: " + clientName);
        }

        // Create the web context
        final ServletExternalContext servletExternalContext
                = (ServletExternalContext) ExternalContextHolder.getExternalContext();
        final HttpServletRequest request = (HttpServletRequest) servletExternalContext.getNativeRequest();
        final HttpServletResponse response = (HttpServletResponse) servletExternalContext.getNativeResponse();
        final WebContext webContext = new J2EContext(request, response);

        // Attempt to retrieve the user profile
        final UserProfile userProfile;
        try {
            userProfile = client.getUserProfile(credentials, webContext);
            logger.debug("userProfile : {}", userProfile);
        } catch (final TechnicalException e) {
            logger.error("Failed to retrieve user profile: client = {}, error = {}", clientName, e.getMessage());
            throw new DelegatedLoginException(e.getMessage());
        }

        // If successful, create and return a handler result; otherwise, throw a delegated login exception.
        if (userProfile != null) {
            return createResult(clientCredentials, userProfile);
        }
        logger.error("Provider did not produce a user profile: client = {}", clientName);
        throw new DelegatedLoginException("Provider did not produce a user profile for: " + clientCredentials);
    }

    /**
     * Build the handler result.
     *
     * @param credentials the provided credentials
     * @param profile the retrieved user profile
     * @return the built handler result
     * @throws GeneralSecurityException on authentication failure
     */
    protected abstract HandlerResult createResult(
            final ClientCredential credentials,
            final UserProfile profile
    ) throws GeneralSecurityException;

    /**
     * Set the principal factory.
     *
     * @param principalFactory the principal factory
     */
    public void setPrincipalFactory(final PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }
}
