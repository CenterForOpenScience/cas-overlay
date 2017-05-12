package io.cos.cas.account.validator;

import io.cos.cas.account.flow.ResetPasswordAction;
import io.cos.cas.account.model.ResetPasswordFormBean;

import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

/**
 * The Validator Class for {@link ResetPasswordFormBean}.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class ResetPasswordFormBeanValidator {
    
    /**
     * The Validator for View State: viewResetPasswordPage.
     *
     * @param resetPasswordForm the reset password form
     * @param context the validation context
     */
    public void validateViewResetPasswordPage(
            final ResetPasswordFormBean resetPasswordForm,
            final ValidationContext context
    ) {
        final MessageContext messageContext = context.getMessageContext();

        if (resetPasswordForm.getAction() == null || !resetPasswordForm.getAction().equalsIgnoreCase(ResetPasswordAction.NAME)) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("action").defaultText("Invalid Client State.").build()
            );
        }

        if (resetPasswordForm.getVerificationCode() == null || resetPasswordForm.getVerificationCode().isEmpty()) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("verificationCode").defaultText("Please enter the verification code.").build()
            );
        }

        if (resetPasswordForm.getNewPassword() == null || resetPasswordForm.getNewPassword().isEmpty()) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("newPassword").defaultText("Please enter your new password.").build()
            );
        }

        if (resetPasswordForm.getConfirmPassword() == null || resetPasswordForm.getConfirmPassword().isEmpty()) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("confirmPassword").defaultText("Please confirm your new email.").build()
            );
        } else if (!resetPasswordForm.getConfirmPassword().equals(resetPasswordForm.getNewPassword())) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("confirmPassword").defaultText("Password does not match.").build()
            );
        }
    }
}
