package io.cos.cas.account.flow;

import io.cos.cas.account.model.VerifyEmailFormBean;
import io.cos.cas.account.util.AbstractAccountFlowUtils;
import io.cos.cas.api.handler.ApiEndpointHandler;

import org.json.JSONObject;

import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Web Flow Action to Verify an Email.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class VerifyEmailAction {

    /** The Name of the Action. */
    public static final String NAME = "VERIFY_EMAIL";

    /** The API Endpoint Handler. */
    private ApiEndpointHandler apiEndpointHandler;

    /**
     * Constructor.
     *
     * @param apiEndpointHandler the API Endpoint Handler
     */
    public VerifyEmailAction(final ApiEndpointHandler apiEndpointHandler) {
        this.apiEndpointHandler = apiEndpointHandler;
    }

    /**
     * Handle Verify Email From Submission.
     *
     * @param requestContext the request context
     * @param verifyEmailForm the verify email form
     * @param messageContext the message context
     * @return the event
     */
    public Event submitForm(
            final RequestContext requestContext,
            final VerifyEmailFormBean verifyEmailForm,
            final MessageContext messageContext
    ) {
        final AccountManager accountManager = AbstractAccountFlowUtils.getAccountManagerFromRequestContext(requestContext);
        final String errorMessage = AbstractAccountFlowUtils.DEFAULT_SERVER_ERROR_MESSAGE;

        if (accountManager != null && accountManager.getEmailToVerify() != null) {

            // VerifyEmailFormBean.emailToVerify is disabled in the form and should be null from submission
            verifyEmailForm.setEmailToVerify(accountManager.getEmailToVerify());

            final JSONObject user = new JSONObject();
            final JSONObject data = new JSONObject();

            user.put("email", verifyEmailForm.getEmailToVerify());
            user.put("verificationCode", verifyEmailForm.getVerificationCode());
            data.put("type", NAME);
            data.put("user", user);
            apiEndpointHandler.encryptPayload("data", data.toString());

            return new Event(this, "success");
        }

        messageContext.addMessage(
                new MessageBuilder().error().source("action").defaultText(errorMessage).build()
        );
        return new Event(this, "error");
    }

}




