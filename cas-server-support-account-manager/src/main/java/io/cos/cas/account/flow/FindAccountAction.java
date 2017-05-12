package io.cos.cas.account.flow;

import io.cos.cas.account.model.FindAccountFormBean;
import io.cos.cas.account.util.AbstractAccountFlowUtils;
import io.cos.cas.api.handler.ApiEndpointHandler;

import org.json.JSONObject;

import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Web Flow Action to Find the OSF Account by Email.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class FindAccountAction {

    /** The Name of the Action. */
    public static final String NAME = "findAccount";

    /** The API Endpoint Handler. */
    private ApiEndpointHandler apiEndpointHandler;

    /**
     * Constructor.
     *
     * @param apiEndpointHandler the API Endpoint Handler
     */
    public FindAccountAction(final ApiEndpointHandler apiEndpointHandler) {
        this.apiEndpointHandler = apiEndpointHandler;
    }

    /**
     * Prepare the Find Account Page.
     *
     * @param requestContext the request context
     * @return the event
     */
    public Event preparePage(final RequestContext requestContext) {
        final String serviceUrl = AbstractAccountFlowUtils.getEncodedServiceUrl(requestContext);
        final String target = AbstractAccountFlowUtils.getTargetFromRequestContext(requestContext);
        final String campaign = AbstractAccountFlowUtils.getCampaignFromRegisteredService(requestContext);

        if (verifyTargetAction(target)) {
            final AccountManager accountPageContext = new AccountManager(serviceUrl, NAME, target, campaign);
            requestContext.getFlowScope().put(AccountManager.ATTRIBUTE_NAME, accountPageContext.toJson());
            return new Event(this, "success");
        }
        return new Event(this, "error");
    }

    /**
     * Handle Find Account Form Submission.
     *
     * @param requestContext the request context
     * @param findAccountForm the find account form bean
     * @param messageContext the message context
     * @return the event
     */
    public Event submitForm(
            final RequestContext requestContext,
            final FindAccountFormBean findAccountForm,
            final MessageContext messageContext
    ) {
        final AccountManager accountManager = AbstractAccountFlowUtils.getAccountManagerFromRequestContext(requestContext);
        final String errorMessage = AbstractAccountFlowUtils.DEFAULT_SERVER_ERROR_MESSAGE;

        if (accountManager != null && verifyTargetAction(accountManager.getTarget())) {

            final JSONObject user = new JSONObject();
            final JSONObject data = new JSONObject();

            user.put("email", findAccountForm.getEmail());
            data.put("type", NAME);
            data.put("user", user);
            apiEndpointHandler.encryptPayload("data", data.toString());

            if (VerifyEmailAction.NAME.equalsIgnoreCase(accountManager.getTarget())) {
                accountManager.setAction(VerifyEmailAction.NAME);
                accountManager.setEmailToVerify(findAccountForm.getEmail());
            } else if (ResetPasswordAction.NAME.equalsIgnoreCase(accountManager.getTarget())) {
                accountManager.setAction(ResetPasswordAction.NAME);
                accountManager.setUsername(findAccountForm.getEmail());
            }
            AbstractAccountFlowUtils.putAccountManagerToRequestContext(requestContext, accountManager);
            return new Event(this, accountManager.getAction());
        }

        messageContext.addMessage(new MessageBuilder().error().source("action").defaultText(errorMessage).build());
        return new Event(this, "error");
    }

    /**
     * Verify the Target Action.
     *
     * @param target the target action
     * @return <code>true</code> if target is valid, <code>false</code> otherwise
     */
    private boolean verifyTargetAction(final String target) {
        return ResetPasswordAction.NAME.equalsIgnoreCase(target) || VerifyEmailAction.NAME.equalsIgnoreCase(target);
    }
}
