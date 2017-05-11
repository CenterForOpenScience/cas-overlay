package io.cos.cas.account.validator;

import io.cos.cas.account.flow.RegisterAction;
import io.cos.cas.account.model.RegisterFormBean;

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;

import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

/**
 * The Validator Class for {@link RegisterFormBean}.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class RegisterFormBeanValidator {

    /**
     * The Validator for View State: viewRegisterPage.
     *
     * @param registerForm the register form
     * @param context the validation context
     */
    public void validateViewRegisterPage(final RegisterFormBean registerForm, final ValidationContext context) {

        final MessageContext messageContext = context.getMessageContext();
        final EmailValidator emailValidator = new EmailValidator();

        if (registerForm.getAction() == null || !registerForm.getAction().equalsIgnoreCase(RegisterAction.NAME)) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("action").defaultText("Invalid Client State.").build()
            );
        }

        if (registerForm.getFullname() == null || registerForm.getFullname().isEmpty()) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("fullname").defaultText("Please enter your name.").build()
            );
        }

        if (registerForm.getEmail() == null || registerForm.getEmail().isEmpty()) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("email").defaultText("Please enter your email.").build()
            );
        } else if (!emailValidator.isValid(registerForm.getEmail(), null)) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("email").defaultText("Please enter a valid email.").build()
            );
        }

        if (registerForm.getConfirmEmail() == null || registerForm.getConfirmEmail().isEmpty()) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("confirmEmail").defaultText("Please confirm your email.").build()
            );
        } else if (!emailValidator.isValid(registerForm.getConfirmEmail(), null)) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("confirmEmail").defaultText("Please enter a valid email.").build()
            );
        } else if (!registerForm.getConfirmEmail().equals(registerForm.getEmail())) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("confirmEmail").defaultText("Email does not match.").build()
            );
        }

        if (registerForm.getPassword() == null || registerForm.getPassword().isEmpty()) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("password").defaultText("Please enter your password.").build()
            );
        }
    }
}
