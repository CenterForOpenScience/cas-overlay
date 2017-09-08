package io.cos.cas.account.validator;

import io.cos.cas.account.flow.CreateOrLinkAction;
import io.cos.cas.account.model.CreateOrLinkFormBean;
import io.cos.cas.account.util.AbstractAccountFlowUtils;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

/**
 * The Validator Class for {@link CreateOrLinkAction}.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class CreateOrLinkFormBeanValidator {

    /**
     * The Validator for View State: viewExternalAuthRegisterPage.
     *
     * @param findAccountForm the find account form
     * @param context the validation context
     */
    public void validateViewExtvalidateViewExternalAuthRegisterPageernalAuthRegisterPage(
            final CreateOrLinkFormBean findAccountForm,
            final ValidationContext context
    ) {
        final MessageContext messageContext = context.getMessageContext();
        final EmailValidator emailValidator = new EmailValidator();

        if (findAccountForm.getAction() == null || !findAccountForm.getAction().equalsIgnoreCase(CreateOrLinkAction.NAME)) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("action").defaultText(AbstractAccountFlowUtils.DEFAULT_CLIENT_ERROR_MESSAGE).build()
            );
        }

        if (findAccountForm.getEmail() == null || findAccountForm.getEmail().isEmpty()) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("email").defaultText("Please enter your email.").build()
            );
        } else if (!emailValidator.isValid(findAccountForm.getEmail(), null)) {
            messageContext.addMessage(
                    new MessageBuilder().error().source("email").defaultText("Please enter a valid email.").build()
            );
        }
    }
}
