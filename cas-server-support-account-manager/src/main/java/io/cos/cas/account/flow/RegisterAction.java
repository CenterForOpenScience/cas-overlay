package io.cos.cas.account.flow;

import io.cos.cas.account.model.RegisterFormBean;
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

    /**
     * Constructor.
     *
     * @param apiEndpointHandler the API Endpoint Handler
     */
    public RegisterAction(final ApiEndpointHandler apiEndpointHandler) {
        this.apiEndpointHandler = apiEndpointHandler;
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

        user.put("fullname", registerForm.getFullname());
        user.put("email", registerForm.getEmail());
        user.put("password", registerForm.getPassword());
        user.put("campaign", registerForm.getCampaign());
        data.put("type", NAME.toUpperCase());
        data.put("user", user);

        final JSONObject response = apiEndpointHandler.handle(
                ApiEndpoint.AUTH_REGISTER,
                apiEndpointHandler.encryptPayload("data", data.toString())
        );

        String errorMessage = AbstractAccountFlowUtils.DEFAULT_CLIENT_ERROR_MESSAGE;

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

        messageContext.addMessage(new MessageBuilder().error().source("action").defaultText(errorMessage).build());
        return new Event(this, "error");
    }
}
