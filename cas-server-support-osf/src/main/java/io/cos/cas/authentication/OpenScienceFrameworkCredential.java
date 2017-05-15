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

package io.cos.cas.authentication;

import io.cos.cas.types.DelegationProtocol;
import io.cos.cas.types.OsfLoginAction;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.jasig.cas.authentication.RememberMeUsernamePasswordCredential;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Open Science Framework Credential.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 4.1.5
 */
public class OpenScienceFrameworkCredential extends RememberMeUsernamePasswordCredential {

    /** Authentication attribute name for Institution ID. */
    public static final String INSTITUTION_ID = "institutionId";

    /** Authentication attribute name for Remote Principal. */
    public static final String REMOTE_PRINCIPAL = "remotePrincipal";

    /** Unique ID for serialization. */
    private static final long serialVersionUID = -3006234230814410939L;

    /** Remote Principal appended to username in string representation. */
    private static final String REMOTE_PRINCIPAL_SUFFIX = "+rp";

    /** Verification Key appended to username in string representation. */
    private static final String VERIFICATION_KEY_SUFFIX = "+vk";

    /** Time-based One Time Password suffix appended to username in string representation. */
    private static final String ONE_TIME_PASSWORD_SUFFIX = "+otp";

    private String verificationKey;

    private String oneTimePassword;

    private Boolean remotePrincipal = Boolean.FALSE;

    private String institutionId;

    private DelegationProtocol delegationProtocol;

    private Map<String, String> delegationAttributes = new HashMap<>();

    private String fullname;

    private String email;

    private String confirmEmail;

    private String campaign;

    private String verificationCode;

    private String newPassword;

    private String confirmPassword;

    private String loginAction;

    /** Default Constructor. */
    public OpenScienceFrameworkCredential() {}

    public String getVerificationKey() {
        return this.verificationKey;
    }

    public void setVerificationKey(final String verificationKey) {
        this.verificationKey = verificationKey;
    }

    public String getOneTimePassword() {
        return this.oneTimePassword;
    }

    public void setOneTimePassword(final String oneTimePassword) {
        this.oneTimePassword = oneTimePassword;
    }

    public final Boolean isRemotePrincipal() {
        return this.remotePrincipal;
    }

    public final void setRemotePrincipal(final Boolean remotePrincipal) {
        this.remotePrincipal = remotePrincipal;
    }

    public final String getInstitutionId() {
        return this.institutionId;
    }

    public final void setInstitutionId(final String institutionId) {
        this.institutionId = institutionId;
    }

    public final DelegationProtocol getDelegationProtocol() {
        return delegationProtocol;
    }

    public void setDelegationProtocol(final DelegationProtocol delegationProtocol) {
        this.delegationProtocol = delegationProtocol;
    }

    public final Map<String, String> getDelegationAttributes() {
        return delegationAttributes;
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

    /**
     * Set email and update username.
     *
     * @param email the email
     */
    public void setEmail(final String email) {
        this.email = email.trim();
        setUsername(this.email);
    }

    public String getConfirmEmail() {
        return confirmEmail;
    }

    public void setConfirmEmail(final String confirmEmail) {
        this.confirmEmail = confirmEmail.trim();
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(final String campaign) {
        this.campaign = campaign;
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
        this.newPassword = newPassword.trim();
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(final String confirmPassword) {
        this.confirmPassword = confirmPassword.trim();
    }

    public String getLoginAction() {
        return loginAction;
    }

    public void setLoginAction(final String loginAction) {
        this.loginAction = loginAction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return this.getUsername();
    }

    @Override
    public String toString() {
        String representation = super.toString();

        if (this.remotePrincipal) {
            representation += REMOTE_PRINCIPAL_SUFFIX;
        }
        if (this.verificationKey != null) {
            representation += VERIFICATION_KEY_SUFFIX;
        }
        if (this.oneTimePassword != null) {
            representation += ONE_TIME_PASSWORD_SUFFIX;
        }
        return representation;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final OpenScienceFrameworkCredential other = (OpenScienceFrameworkCredential) obj;

        if (!this.verificationKey.equals(other.verificationKey)) {
            return false;
        }
        if (!this.oneTimePassword.equals(other.oneTimePassword)) {
            return false;
        }
        if (!this.remotePrincipal.equals(other.remotePrincipal)) {
            return false;
        }
        if (!this.institutionId.equals(other.institutionId)) {
            return false;
        }
        if (!this.delegationProtocol.equals(other.delegationProtocol)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(verificationKey)
                .append(oneTimePassword)
                .append(remotePrincipal)
                .append(institutionId)
                .append(delegationProtocol)
                .toHashCode();
    }

    /**
     * Form Submission Validation for View State: viewRegisterForm.
     *
     * Inherited property "password" is validated by super class "UsernamePasswordCredential".
     * Hidden form property "loginAction" is checked against client side modification
     *
     * @param context the validation context
     */
    public void validateViewRegisterForm(final ValidationContext context) {

        final MessageContext messageContext = context.getMessageContext();
        final EmailValidator emailValidator = new EmailValidator();

        if (loginAction == null || !OsfLoginAction.isRegister(loginAction)) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("loginAction").defaultText("Invalid Client State.").build()
            );
        }

        if (fullname == null || fullname.trim().isEmpty()) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("fullname").defaultText("Please enter your name.").build()
            );
        }

        if (email == null || email.trim().isEmpty()) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("email").defaultText("Please enter your email.").build()
            );
        } else if (!emailValidator.isValid(email, null)) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("email").defaultText("Please enter a valid email.").build()
            );
        }

        if (confirmEmail == null || confirmEmail.trim().isEmpty()) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("confirmEmail").defaultText("Please confirm your email.").build()
            );
        } else if (!emailValidator.isValid(confirmEmail, null)) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("confirmEmail").defaultText("Please enter a valid email.").build()
            );
        } else if (!confirmEmail.equals(email)) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("confirmEmail").defaultText("Email does not match.").build()
            );
        }
    }

    /**
     * Form Submission Validation for View State: viewLoginHelpForm.
     *
     * Hidden form property "loginAction" is checked against client side modification
     *
     * @param context the validation context
     */
    public void validateViewLoginHelpForm(final ValidationContext context) {

        final MessageContext messageContext = context.getMessageContext();
        final EmailValidator emailValidator = new EmailValidator();

        if (loginAction == null || !OsfLoginAction.isHelp(loginAction)) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("loginAction").defaultText("Invalid Client State.").build()
            );
        }

        if (email == null || email.trim().isEmpty()) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("email").defaultText("Please enter your email.").build()
            );
        } else if (!emailValidator.isValid(email, null)) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("email").defaultText("Please enter a valid email.").build()
            );
        }
    }

    /**
     * Form Submission Validation for View State: viewLoginChallenge.
     *
     * Hidden form property "loginAction" is checked against client side modification
     *
     * @param context the validation context
     */
    public void validateViewLoginChallenge(final ValidationContext context) {

        final MessageContext messageContext = context.getMessageContext();
        final EmailValidator emailValidator = new EmailValidator();

        if (loginAction == null || !OsfLoginAction.isChallenge(loginAction)) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("loginAction").defaultText("Invalid Client State.").build()
            );
        }

        if (email == null || email.trim().isEmpty()) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("email").defaultText("Please enter your email.").build()
            );
        } else if (!emailValidator.isValid(email, null)) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("email").defaultText("Please enter a valid email.").build()
            );
        }

        if (verificationCode == null || verificationCode.trim().isEmpty()) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("verificationCode").defaultText("Please enter the verification code.").build()
            );
        }

        if (loginAction.equals(OsfLoginAction.RESET_PASSWORD.getId())) {
            if (newPassword == null || newPassword.trim().isEmpty()) {
                messageContext.addMessage(
                        new MessageBuilder().error().source("newPassword").defaultText("Please enter a new password.").build()
                );
            }

            if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
                messageContext.addMessage(
                        new MessageBuilder().error().source("confirmPassword").defaultText("Please confirm the new password.").build()
                );
            } else if (!confirmPassword.equals(newPassword)) {
                messageContext.addMessage(
                        new MessageBuilder().error().source("confirmPassword").defaultText("Password does not match.").build()
                );
            }
        }
    }
}
