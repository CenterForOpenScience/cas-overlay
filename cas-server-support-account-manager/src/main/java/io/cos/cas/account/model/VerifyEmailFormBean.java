package io.cos.cas.account.model;

import java.io.Serializable;

/**
 * The Model for Verify Email Form.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class VerifyEmailFormBean implements Serializable {

    private static final long serialVersionUID = -2296030509948569137L;

    private String action;

    private String emailToVerify;

    private String verificationCode;

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    public String getEmailToVerify() {
        return emailToVerify;
    }

    public void setEmailToVerify(final String emailToVerify) {
        this.emailToVerify = emailToVerify;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(final String verificationCode) {
        this.verificationCode = verificationCode;
    }
}
