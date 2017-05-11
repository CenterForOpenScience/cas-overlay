package io.cos.cas.account.flow;

import io.cos.cas.account.model.RegisterFormBean;
import io.cos.cas.account.util.AbstractAccountFlowUtils;
import io.cos.cas.api.handler.ApiEndpointHandler;
import io.cos.cas.api.type.ApiEndpoint;
import io.cos.cas.api.util.AbstractApiEndpointUtils;

import org.json.JSONObject;

import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;

/**
 * Web Flow Action to Register a New OSF Account.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class RegisterAction {

    /** The Name of the Action. */
    public static final String NAME = "register";

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
        final AccountManager accountPageContext = new AccountManager(serviceUrl, NAME, campaign);
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

        final Map<String, Object> response = apiEndpointHandler.apiCasAuthentication(
                ApiEndpoint.AUTH_REGISTER,
                registerForm.getEmail(),
                apiEndpointHandler.encryptPayload("data", data.toString())
        );

        String errorMessage = "Internal server error. Please try again later.";

        if (response != null && response.containsKey("status")) {
            final String status = (String) response.get("status");
            if (AbstractApiEndpointUtils.REGISTER_SUCCESS.equals(status)) {
                final AccountManager accountManager
                        = AbstractAccountFlowUtils.getAccountManagerFromRequestContext(requestContext);
                if (accountManager != null) {
                    accountManager.setAction(VerifyEmailAction.NAME);
                    accountManager.setEmailToVerify(registerForm.getEmail());
                    AbstractAccountFlowUtils.putAccountManagerToRequestContext(requestContext, accountManager);
                    return new Event(this, "success");
                }
            }
            if (AbstractApiEndpointUtils.AUTH_FAILURE.equals(status) && response.containsKey("detail")) {
                final String detail = (String) response.get("detail");
                if (AbstractApiEndpointUtils.ALREADY_REGISTERED.equals(detail)) {
                    errorMessage = String.format("The email %s has already been registered.", registerForm.getEmail());
                }
            }
        }

        messageContext.addMessage(new MessageBuilder().error().source("action").defaultText(errorMessage).build());
        return new Event(this, "error");
    }
}
