package io.cos.cas.account.flow;

import io.cos.cas.account.model.ResetPasswordFormBean;
import io.cos.cas.account.util.AbstractAccountFlowUtils;
import io.cos.cas.api.handler.ApiEndpointHandler;
import io.cos.cas.api.type.ApiEndpoint;

import org.apache.http.HttpStatus;

import org.json.JSONObject;

import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Web Flow Action to Reset Password.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class ResetPasswordAction {

    /** The Name of the Action. */
    public static final String NAME = "RESET_PASSWORD";

    /** The API Endpoint Handler. */
    private ApiEndpointHandler apiEndpointHandler;

    /**
     * Constructor.
     *
     * @param apiEndpointHandler the API Endpoint Handler
     */
    public ResetPasswordAction(final ApiEndpointHandler apiEndpointHandler) {
        this.apiEndpointHandler = apiEndpointHandler;
    }

    /**
     * Handle Reset Password From Submission.
     *
     * @param requestContext the request context
     * @param resetPasswordForm the reset password form
     * @param messageContext the message context
     * @return the event
     */
    public Event submitForm(
            final RequestContext requestContext,
            final ResetPasswordFormBean resetPasswordForm,
            final MessageContext messageContext
    ) {
        final AccountManager accountManager = AbstractAccountFlowUtils.getAccountManagerFromRequestContext(requestContext);
        String errorMessage = AbstractAccountFlowUtils.DEFAULT_SERVER_ERROR_MESSAGE;

        if (accountManager != null && accountManager.getUsername() != null) {

            resetPasswordForm.setUsername(accountManager.getUsername());

            final JSONObject user = new JSONObject();
            final JSONObject data = new JSONObject();

            user.put("email", resetPasswordForm.getUsername());
            user.put("verificationCode", resetPasswordForm.getVerificationCode());
            user.put("password", resetPasswordForm.getNewPassword());
            data.put("type", NAME);
            data.put("user", user);

            final JSONObject response = apiEndpointHandler.handle(
                    ApiEndpoint.AUTH_RESET_PASSWORD,
                    apiEndpointHandler.encryptPayload("data", data.toString())
            );

            if (response != null) {
                final int status = response.getInt("status");
                if (status == HttpStatus.SC_OK) {
                    if (AbstractAccountFlowUtils.verifyResponseAndPutLoginRedirectUrlToRequestContext(
                            requestContext,
                            response.getJSONObject("body"),
                            apiEndpointHandler.getCasLoginUrl(),
                            accountManager.getUsername()
                    )) {
                        return new Event(this, "redirect");
                    }
                } else if (status == HttpStatus.SC_FORBIDDEN || status == HttpStatus.SC_UNAUTHORIZED) {
                    errorMessage = apiEndpointHandler.getErrorMessageFromResponseBody(response.getJSONObject("body"));
                }
            }
        }

        messageContext.addMessage(new MessageBuilder().error().source("action").defaultText(errorMessage).build());
        return new Event(this, "error");
    }
}
