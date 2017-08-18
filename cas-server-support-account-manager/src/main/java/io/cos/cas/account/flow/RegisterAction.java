package io.cos.cas.account.flow;

import io.cos.cas.account.model.RegisterFormBean;
import io.cos.cas.account.util.AbstractAccountFlowUtils;
import io.cos.cas.account.util.RecaptchaUtils;
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
 * Web Flow Action to Register a New OSF Account.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class RegisterAction {

    /** The Name of the Action. */
    public static final String NAME = "REGISTER";

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterAction.class);

    /** The API Endpoint Handler. */
    private APIEndpointHandler apiEndpointHandler;

    /** The Recaptcha Utility. */
    private RecaptchaUtils recaptchaUtils;

    /**
     * Constructor.
     *
     * @param apiEndpointHandler the API Endpoint Handler
     * @param recaptchaUtils the reCAPTCHA Utility
     */
    public RegisterAction(final APIEndpointHandler apiEndpointHandler, final RecaptchaUtils recaptchaUtils) {
        this.apiEndpointHandler = apiEndpointHandler;
        this.recaptchaUtils = recaptchaUtils;
    }

    /**
     * Prepare the Register Page.
     *
     * @param requestContext the request context
     * @return the event
     */
    public Event preparePage(final RequestContext requestContext) {
        final String serviceUrl = AbstractAccountFlowUtils.getEncodedServiceUrl(requestContext);
        final String campaign = AbstractAccountFlowUtils.getCampaignFromRegisteredService(requestContext);
        final AccountManager accountPageContext = new AccountManager(serviceUrl, NAME, null, campaign);
        accountPageContext.setRecaptchaSiteKey(recaptchaUtils.getSiteKey());
        requestContext.getFlowScope().put(AccountManager.ATTRIBUTE_NAME, accountPageContext.toJson());
        return new Event(this, "success");
    }

    /**
     * Handle Register From Submission.
     *
     * @param requestContext the request context
     * @param registerForm the register form bean
     * @param messageContext the message context
     * @return the event
     */
    public Event submitForm(final RequestContext requestContext, final RegisterFormBean registerForm, final MessageContext messageContext) {

        final JSONObject user = new JSONObject();
        final JSONObject data = new JSONObject();

        String errorMessage = AbstractAccountFlowUtils.DEFAULT_SERVER_ERROR_MESSAGE;

        if (!recaptchaUtils.verifyRecaptcha(requestContext)) {
            errorMessage = "Invalid Captcha";
        } else {
            user.put("fullname", registerForm.getFullname())
                    .put("email", registerForm.getEmail())
                    .put("password", registerForm.getPassword())
                    .put("campaign", registerForm.getCampaign());
            data.put("accountAction", "REGISTER_OSF").put("user", user);
            final JSONObject response = apiEndpointHandler.handle(
                    APIEndpoint.ACCOUNT_REGISTER_OSF,
                    apiEndpointHandler.encryptPayload("data", data.toString())
            );
            if (response != null) {
                final int status = response.getInt("status");
                if (status == HttpStatus.SC_NO_CONTENT) {
                    final AccountManager accountManager
                            = AbstractAccountFlowUtils.getAccountManagerFromRequestContext(requestContext);
                    if (accountManager != null) {
                        accountManager.setAction(VerifyEmailAction.NAME);
                        accountManager.setEmailToVerify(registerForm.getEmail());
                        AbstractAccountFlowUtils.putAccountManagerToRequestContext(requestContext, accountManager);
                        return new Event(this, "success");
                    }
                } else if (status == HttpStatus.SC_BAD_REQUEST) {
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

        messageContext.addMessage(new MessageBuilder().error().source("action").defaultText(errorMessage).build());
        return new Event(this, "error");
    }
}
