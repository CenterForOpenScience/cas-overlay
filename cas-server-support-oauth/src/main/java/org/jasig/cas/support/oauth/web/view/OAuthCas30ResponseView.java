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
package org.jasig.cas.support.oauth.web.view;

import org.apache.commons.lang3.StringUtils;

import org.jasig.cas.authentication.support.CasAttributeEncoder;
import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.web.view.Cas30ResponseView;

import org.springframework.web.servlet.view.AbstractUrlBasedView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * The OAuth CAS 3.0 (protocol version as opposed to the implementation level) view.
 *
 * This class expand the {@link Cas30ResponseView} by appending the CAS access token to the attributes if found. With
 * current CAS settings, this class replaces {@link Cas30ResponseView} and serves as the success view for the protocol
 * 3.0 service validation controller.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public class OAuthCas30ResponseView extends Cas30ResponseView {

    /**
     * Instantiates a new {@link OAuthCas30ResponseView}.
     *
     * @param view the view
     */
    protected OAuthCas30ResponseView(final AbstractUrlBasedView view) {
        super(view);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void prepareMergedOutputModel(
            final Map<String, Object> model,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        super.prepareMergedOutputModel(model, request, response);
        final HashMap attributes
                = (HashMap) model.get(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_ATTRIBUTES);
        final String accessToken = (String) model.get(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN);
        final Set<String> accessTokenScope = (Set<String>) model.get(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN_SCOPE);
        final String scopes = StringUtils.join(accessTokenScope, " ");
        if (accessToken != null) {
            attributes.put(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN, accessToken);
            attributes.put(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN_SCOPE, scopes);
        }
    }

    /**
     * Set the services manager.
     *
     * @param servicesManager the services manager to set
     */
    public void setServicesManager(@NotNull final ServicesManager servicesManager) {
        super.setServicesManager(servicesManager);
    }

    /**
     * Set the CAS attribute encoder.
     *
     * @param casAttributeEncoder the CAS attribute encoder to set
     */
    public void setCasAttributeEncoder(@NotNull final CasAttributeEncoder casAttributeEncoder) {
        super.setCasAttributeEncoder(casAttributeEncoder);
    }
}
