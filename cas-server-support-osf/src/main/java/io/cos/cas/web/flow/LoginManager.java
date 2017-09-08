/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

package io.cos.cas.web.flow;

import com.google.gson.Gson;

/**
 * The Open Science Framework Login Manager.
 *
 * @author Longze chen
 * @since  4.1.5
 */
public final class LoginManager {

    /** Flow Scope Attribute Name for Login Manager. */
    public static final String ATTRIBUTE_NAME = "loginManager";

    private String serviceUrl;
    private String username;
    private String campaign;
    private boolean institutionLogin;
    private String handleErrorName;

    /**
     * Instantiate an Open Science Framework Login Context with Service URL, OSF Campaign.
     *
     * @param serviceUrl the service url
     * @param campaign the OSF campaign
     * @param institutionLogin login through institutions
     */
    public LoginManager(
            final String serviceUrl,
            final String campaign,
            final boolean institutionLogin
    ) {
        this.serviceUrl = serviceUrl;
        this.username = null;
        this.campaign = campaign;
        this.handleErrorName = null;
        this.institutionLogin = institutionLogin;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getCampaign() {
        return campaign;
    }

    public String getHandleErrorName() {
        return handleErrorName;
    }

    public void setServiceUrl(final String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setCampaign(final String campaign) {
        this.campaign = campaign;
    }

    public void setInstitutionLogin(final Boolean institutionLogin) {
        this.institutionLogin = institutionLogin;
    }

    public void setHandleErrorName(final String handleErrorName) {
        this.handleErrorName = handleErrorName;
    }

    /**
     * @return <code>institutionLogin</code>.
     */
    public Boolean isInstitutionLogin() {
        return institutionLogin;
    }

    /**
     * @return <code>true</code> if service is not empty, <code>false</code> otherwise.
     */
    public boolean checkService() {
        return serviceUrl != null && !serviceUrl.isEmpty();
    }

    /**
     * Serialize the Instance to a JSON String.
     *
     * @return a JSON string
     */
    public String toJson() {
        final Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     * Restore the Instance from a JSON String.
     *
     * @param jsonString the json string
     * @return an instance of {@link LoginManager}
     */
    public static LoginManager fromJson(final String jsonString) {
        if (jsonString != null && !jsonString.isEmpty()) {
            final Gson gson = new Gson();
            return gson.fromJson(jsonString, LoginManager.class);
        }
        return null;
    }
}
