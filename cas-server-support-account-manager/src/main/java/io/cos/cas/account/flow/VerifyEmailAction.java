package io.cos.cas.account.flow;

import io.cos.cas.account.model.VerifyEmailFormBean;
import io.cos.cas.account.util.AbstractAccountFlowUtils;
import io.cos.cas.api.handler.APIEndpointHandler;

import io.cos.cas.api.type.APIErrors;
import io.cos.cas.api.type.APIEndpoint;
import org.apache.http.HttpStatus;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyEmailAction.class);

    /** The API Endpoint Handler. */
    private APIEndpointHandler apiEndpointHandler;

    /**
     * Constructor.
     *
     * @param apiEndpointHandler the API Endpoint Handler
     */
    public VerifyEmailAction(final APIEndpointHandler apiEndpointHandler) {
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
        String errorMessage = AbstractAccountFlowUtils.DEFAULT_SERVER_ERROR_MESSAGE;

        if (accountManager != null) {

            final JSONObject user = new JSONObject();
            final JSONObject data = new JSONObject();
            APIEndpoint endpoint;

            if (accountManager.getTarget() != null) {
                endpoint = APIEndpoint.ACCOUNT_VERIFY_EXTERNAL;
                data.put("accountAction", "VERIFY_EXTERNAL");
            } else {
                endpoint = APIEndpoint.ACCOUNT_VERIFY_OSF;
                data.put("accountAction", "VERIFY_OSF");
            }

            if (accountManager.getUserId() != null) {
                verifyEmailForm.setUserId(accountManager.getUserId());
                user.put("userId", verifyEmailForm.getUserId());
            }

            if (accountManager.getEmailToVerify() != null) {
                verifyEmailForm.setEmailToVerify(accountManager.getEmailToVerify());
                user.put("email", verifyEmailForm.getEmailToVerify());
            }

            user.put("verificationCode", verifyEmailForm.getVerificationCode());
            data.put("user", user);

            final JSONObject response = apiEndpointHandler.handle(
                    endpoint,
                    apiEndpointHandler.encryptPayload("data", data.toString())
            );

            if (response != null) {
                final int status = response.getInt("status");
                if (status == HttpStatus.SC_OK) {
                    if (AbstractAccountFlowUtils.verifyResponseAndPutLoginRedirectUrlToRequestContext(
                            requestContext,
                            response.getJSONObject("body"),
                            apiEndpointHandler.getCasLoginUrl(),
                            apiEndpointHandler.getOsfCasActionUrl()
                    )) {
                        return new Event(this, "redirect");
                    }
                }  else if (status == HttpStatus.SC_BAD_REQUEST) {
                    final APIErrors error = apiEndpointHandler.getAPIErrorFromResponse(response.getJSONObject("body"));
                    if (error != null) {
                        errorMessage = error.getDetail();
                        LOGGER.error("API Request Failed: status={}, code={}, detail='{}'", status, error.getCode(), error.getDetail());
                    }
                } else {
                    LOGGER.error("API Request Failed: unexpected HTTP status {}", status);
                }
            }
        }

        messageContext.addMessage(
                new MessageBuilder().error().source("action").defaultText(errorMessage).build()
        );
        return new Event(this, "error");
    }

}




