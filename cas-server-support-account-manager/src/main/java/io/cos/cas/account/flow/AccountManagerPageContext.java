package io.cos.cas.account.flow;

import com.google.gson.Gson;

/**
 * Account Manager Page Context.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class AccountManagerPageContext {

    private String serviceUrl;
    private String action;
    private String username;
    private String campaign;

    /**
     * Instantiate an Account Manager Page Context with Service URL, Action and Campaign.
     *
     * @param serviceUrl the service url
     * @param action the action
     * @param campaign the campaign
     */
    public AccountManagerPageContext(final String serviceUrl, final String action, final String campaign) {
        this.serviceUrl = serviceUrl;
        this.action = action;
        this.username = null;
        this.campaign = campaign;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(final String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(final String campaign) {
        this.campaign = campaign;
    }

    /**
     * @return <code>true</code> if service is not empty, <code>false</code> otherwise.
     */
    public boolean checkService() {
        return serviceUrl != null && !serviceUrl.isEmpty();
    }

    /**
     * @return  <code>true</code> if action is {@link RegisterAction}, <code>false</code> otherwise.
     */
    public boolean isRegister() {
        return RegisterAction.NAME.equalsIgnoreCase(action);
    }

    /**
     * Serialize the Instance to a JSON String.
     *
     * @return JSON string
     */
    public String toJson() {
        final Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     * Restore the Instance from a JSON string.
     *
     * @param jsonString The json String
     * @return an instance of {@link AccountManagerPageContext}
     */
    public static AccountManagerPageContext fromJson(final String jsonString) {
        final Gson gson = new Gson();
        return gson.fromJson(jsonString, AccountManagerPageContext.class);
    }
}
