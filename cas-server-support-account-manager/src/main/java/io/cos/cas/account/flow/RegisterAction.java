package io.cos.cas.account.flow;

import io.cos.cas.account.model.RegisterFormBean;
import io.cos.cas.account.util.AbstractAccountFlowUtils;
import io.cos.cas.account.util.RecaptchaUtils;
import io.cos.cas.api.handler.ApiEndpointHandler;
import io.cos.cas.api.type.ApiEndpoint;

import org.apache.http.HttpStatus;

import org.json.JSONObject;

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

    /** The API Endpoint Handler. */
    private ApiEndpointHandler apiEndpointHandler;

    /** The Recaptcha Utility. */
    private RecaptchaUtils recaptchaUtils;

    /**
     * Constructor.
     *
     * @param apiEndpointHandler the API Endpoint Handler
     * @param recaptchaUtils the reCAPTCHA Utility
     */
    public RegisterAction(final ApiEndpointHandler apiEndpointHandler, final RecaptchaUtils recaptchaUtils) {
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

        String errorMessage = AbstractAccountFlowUtils.DEFAULT_CLIENT_ERROR_MESSAGE;

        if (!recaptchaUtils.verifyRecaptcha(requestContext)) {
            errorMessage = "Invalid Captcha";
        } else {
            user.put("fullname", registerForm.getFullname())
                    .put("email", registerForm.getEmail())
                    .put("password", registerForm.getPassword())
                    .put("campaign", registerForm.getCampaign());
            data.put("accountAction", "REGISTER_OSF").put("user", user);
            final JSONObject response = apiEndpointHandler.handle(
                    ApiEndpoint.ACCOUNT_REGISTER_OSF,
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
                } else if (status == HttpStatus.SC_UNAUTHORIZED || status == HttpStatus.SC_FORBIDDEN) {
                    errorMessage = apiEndpointHandler.getErrorMessageFromResponseBody(response.getJSONObject("body"));
                }
            }
        }

        messageContext.addMessage(new MessageBuilder().error().source("action").defaultText(errorMessage).build());
        return new Event(this, "error");
    }
}
