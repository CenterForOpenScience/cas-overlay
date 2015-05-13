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

import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.authentication.support.CasAttributeEncoder;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.web.view.Cas30ResponseView;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;


/**
 * Appends the OAuth Access Token to the attributes if found.
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public class OAuthCas30ResponseView extends Cas30ResponseView {
    /**
     * Instantiates a new Abstract cas response view.
     *
     * @param view the view
     */
    protected OAuthCas30ResponseView(final AbstractUrlBasedView view) {
        super(view);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void prepareMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request,
                                            final HttpServletResponse response) throws Exception {
        super.prepareMergedOutputModel(model, request, response);

        HashMap attributes = (HashMap) model.get(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_ATTRIBUTES);
        final String accessToken = (String) model.get(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN);
        if (accessToken != null) {
            attributes.put(OAuthConstants.CAS_PROTOCOL_ACCESS_TOKEN, accessToken);
        }
    }

    /**
     * Sets services manager.
     *
     * @param servicesManager the services manager
     * @since 4.1
     */
    public void setServicesManager(@NotNull final ServicesManager servicesManager) {
        super.setServicesManager(servicesManager);
    }

    /**
     * Sets cas attribute encoder.
     *
     * @param casAttributeEncoder the cas attribute encoder
     * @since 4.1
     */
    public void setCasAttributeEncoder(@NotNull final CasAttributeEncoder casAttributeEncoder) {
        super.setCasAttributeEncoder(casAttributeEncoder);
    }
}
