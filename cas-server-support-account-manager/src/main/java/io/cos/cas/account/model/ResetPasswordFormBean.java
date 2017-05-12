package io.cos.cas.account.model;

import java.io.Serializable;

/**
 * The Model for Reset Password From.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class ResetPasswordFormBean implements Serializable {

    private static final long serialVersionUID = 7138617376864260767L;

    private String action;

    private String username;

    private String verificationCode;

    private String newPassword;

    private String confirmPassword;

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

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(final String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(final String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(final String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
