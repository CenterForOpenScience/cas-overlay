package io.cos.cas.account.flow;

import io.cos.cas.account.model.FindAccountFormBean;
import io.cos.cas.account.util.AbstractAccountFlowUtils;
import io.cos.cas.api.handler.APIEndpointHandler;
import io.cos.cas.api.type.APIErrors;
import io.cos.cas.api.type.APIEndpoint;

import io.cos.cas.authentication.OpenScienceFrameworkCredential;
import org.apache.http.HttpStatus;

import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public static final String NAME = "FIND_ACCOUNT";

    private static final Logger LOGGER = LoggerFactory.getLogger(FindAccountAction.class);

    /** The API Endpoint Handler. */
    private APIEndpointHandler apiEndpointHandler;

    /**
     * Constructor.
     *
     * @param apiEndpointHandler the API Endpoint Handler
     */
    public FindAccountAction(final APIEndpointHandler apiEndpointHandler) {
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
        final AccountManager accountPageContext = new AccountManager(serviceUrl, NAME, target, campaign);

        final String userId = AbstractAccountFlowUtils.getUserIdFromRequestContext(requestContext);
        final Boolean meetings = AbstractAccountFlowUtils.getMeetingsFromRequestContext(requestContext);

        String event;
        if (ResetPasswordAction.NAME.equalsIgnoreCase(target)) {
            if (!userId.isEmpty()) {
                // skip find account action and go to reset action directly
                accountPageContext.setUserId(userId);
                if (meetings) {
                    accountPageContext.setMeetings(Boolean.TRUE);
                }
                accountPageContext.setAction(ResetPasswordAction.NAME);
                accountPageContext.setTarget(null);
                event = "reset";
            } else {
                // land on find account page for sending password reset email
                event = "success";
            }
        } else if (VerifyEmailAction.NAME.equalsIgnoreCase(target)) {
            if (!userId.isEmpty()) {
                // skip find account action and go to verify email direction directly
                accountPageContext.setUserId(userId);
                accountPageContext.setAction(VerifyEmailAction.NAME);
                accountPageContext.setTarget(null);
                event = "verify";
            } else {
                // land on find account page for resending confirmation email
                event = "success";
            }
        } else {
            LOGGER.error("Find Account Action Failed: target={} is invalid", target);
            event = "error";
        }

        requestContext.getFlowScope().put(AccountManager.ATTRIBUTE_NAME, accountPageContext.toJson());
        return new Event(this, event);
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
        final OpenScienceFrameworkCredential credential = AbstractAccountFlowUtils.getCredentialFromSessionScope(requestContext);
        String errorMessage = AbstractAccountFlowUtils.DEFAULT_SERVER_ERROR_MESSAGE;

        if (accountManager != null) {

            final String targetAction = accountManager.getTarget();
            final APIEndpoint endpoint = getAPIEndpointByTargetAction(targetAction);

            if (endpoint != null) {

                final JSONObject user = new JSONObject();
                final JSONObject data = new JSONObject();
                user.put("email", findAccountForm.getEmail());
                data.put("user", user);
                final JSONObject response = apiEndpointHandler.handle(
                        endpoint,
                        apiEndpointHandler.encryptPayload("data", data.toString())
                );

                if (response != null) {
                    final int status = response.getInt("status");
                    if (status == HttpStatus.SC_NO_CONTENT) {
                        if (VerifyEmailAction.NAME.equalsIgnoreCase(targetAction)) {
                            accountManager.setAction(VerifyEmailAction.NAME);
                            accountManager.setEmailToVerify(findAccountForm.getEmail());
                            accountManager.setTarget(null);
                        } else if (ResetPasswordAction.NAME.equalsIgnoreCase(targetAction)) {
                            accountManager.setAction(ResetPasswordAction.NAME);
                            accountManager.setUsername(findAccountForm.getEmail());
                            accountManager.setTarget(null);
                        }
                        AbstractAccountFlowUtils.putAccountManagerToRequestContext(requestContext, accountManager);
                        return new Event(this, accountManager.getAction());
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
        }

        messageContext.addMessage(new MessageBuilder().error().source("action").defaultText(errorMessage).build());
        return new Event(this, "error");
    }

    /**
     * Verify the Target Action and Return the API Endpoint.
     *
     * @param target the target action
     * @return the API Endpoint if valid
     */
    private APIEndpoint getAPIEndpointByTargetAction(final String target) {

        if (ResetPasswordAction.NAME.equalsIgnoreCase(target)){
            return APIEndpoint.ACCOUNT_PASSWORD_FORGOT;
        } else if (VerifyEmailAction.NAME.equalsIgnoreCase(target)) {
            return APIEndpoint.ACCOUNT_VERIFY_OSF_RESEND;
        } else {
            LOGGER.error("Find Account Action Failed: target={} is invalid", target);
            return null;
        }
    }
}
