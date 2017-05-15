package io.cos.cas.account.flow;

import com.google.gson.Gson;

/**
 * The Open Science Framework Account Manager.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class AccountManager {

    /** Flow Scope Attribute Name for Account Manager. */
    public static final String ATTRIBUTE_NAME = "accountManager";

    private String serviceUrl;
    private String action;
    private String target;
    private String username;
    private String emailToVerify;
    private String campaign;

    /**
     * Instantiate an Account Manager with Service URL, Action, Target and Campaign.
     *
     * @param serviceUrl the service url
     * @param action the action
     * @param target the target
     * @param campaign the campaign
     */
    public AccountManager(final String serviceUrl, final String action, final String target, final String campaign) {
        this.serviceUrl = serviceUrl;
        this.action = action;
        this.target = target;
        this.username = null;
        this.emailToVerify = null;
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

    public String getTarget() {
        return target;
    }

    public void setTarget(final String target) {
        this.target = target;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getEmailToVerify() {
        return emailToVerify;
    }

    public void setEmailToVerify(final String emailToVerify) {
        this.emailToVerify = emailToVerify;
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
     * @return a JSON string
     */
    public String toJson() {
        final Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     * Restore the Instance from a JSON String.
     *
     * @param jsonString The json String
     * @return an instance of {@link AccountManager}
     */
    public static AccountManager fromJson(final String jsonString) {
        if (jsonString != null && !jsonString.isEmpty()) {
            final Gson gson = new Gson();
            return gson.fromJson(jsonString, AccountManager.class);
        }
        return null;
    }
}
