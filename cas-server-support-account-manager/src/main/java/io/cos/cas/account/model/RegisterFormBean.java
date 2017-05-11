package io.cos.cas.account.model;

import java.io.Serializable;

/**
 * The Model for Register Form.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class RegisterFormBean implements Serializable {

    private static final long serialVersionUID = -5918867586220928623L;

    private String action;

    private String campaign;

    private String fullname;

    private String email;

    private String confirmEmail;

    private String password;

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(final String campaign) {
        this.campaign = campaign;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(final String fullname) {
        this.fullname = fullname.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email.trim().toLowerCase();
    }

    public String getConfirmEmail() {
        return confirmEmail;
    }

    public void setConfirmEmail(final String confirmEmail) {
        this.confirmEmail = confirmEmail.trim().toLowerCase();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password.trim();
    }
}
