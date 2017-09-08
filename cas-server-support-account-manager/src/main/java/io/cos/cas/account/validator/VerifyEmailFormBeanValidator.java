package io.cos.cas.account.validator;

import io.cos.cas.account.flow.VerifyEmailAction;
import io.cos.cas.account.model.VerifyEmailFormBean;

import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

/**
 * The Validator Class for {@link VerifyEmailFormBean}.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class VerifyEmailFormBeanValidator {

    /**
     * The Validator for View State: viewVerifyEmailPage.
     *
     * @param verifyEmailForm the verify email form
     * @param context the validation context
     */
    public void validateViewVerifyEmailPage(
            final VerifyEmailFormBean verifyEmailForm,
            final ValidationContext context
    ) {
        final MessageContext messageContext = context.getMessageContext();

        if (verifyEmailForm.getAction() == null || !verifyEmailForm.getAction().equalsIgnoreCase(VerifyEmailAction.NAME)) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("action").defaultText("Invalid Client State.").build()
            );
        }

        if (verifyEmailForm.getVerificationCode() == null || verifyEmailForm.getVerificationCode().isEmpty()) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("verificationCode").defaultText("Please enter the verification code.").build()
            );
        }
    }
}
