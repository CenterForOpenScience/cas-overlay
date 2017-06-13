package io.cos.cas.account.flow;

import io.cos.cas.account.model.FindAccountFormBean;
import io.cos.cas.account.util.AbstractAccountFlowUtils;
import io.cos.cas.api.handler.ApiEndpointHandler;
import io.cos.cas.api.type.ApiEndpoint;

import io.cos.cas.authentication.OpenScienceFrameworkCredential;
import org.apache.http.HttpStatus;

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
    public static final String NAME = "FIND_ACCOUNT";

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
        final OpenScienceFrameworkCredential credential = AbstractAccountFlowUtils.getCredentialFromSessionScope(requestContext);
        final boolean externalIdRegisterEmail = credential != null
                && credential.getNonInstitutionExternalIdProvider() != null
                && credential.getNonInstitutionExternalId() != null
                && !credential.isRemotePrincipal();

        if (externalIdRegisterEmail) {
            final AccountManager accountPageContext = new AccountManager(serviceUrl, NAME, target, campaign);
            requestContext.getFlowScope().put(AccountManager.ATTRIBUTE_NAME, accountPageContext.toJson());
            return new Event(this, "success");
        } else if (verifyTargetAction(target)) {
            final AccountManager accountPageContext = new AccountManager(serviceUrl, NAME, target, campaign);
            requestContext.getFlowScope().put(AccountManager.ATTRIBUTE_NAME, accountPageContext.toJson());
            return new Event(this, "success");
        } else {
            return new Event(this, "error");
        }
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
        final boolean externalIdRegisterEmail = credential != null
                && credential.getNonInstitutionExternalIdProvider() != null
                && credential.getNonInstitutionExternalId() != null
                && !credential.isRemotePrincipal();
        String errorMessage = AbstractAccountFlowUtils.DEFAULT_SERVER_ERROR_MESSAGE;

        if (accountManager != null && (externalIdRegisterEmail || verifyTargetAction(accountManager.getTarget()))) {

            ApiEndpoint endpoint;
            final JSONObject user = new JSONObject();
            final JSONObject data = new JSONObject();
            user.put("email", findAccountForm.getEmail());

            if (externalIdRegisterEmail) {
                endpoint = ApiEndpoint.AUTH_EXTERNAL_CREATE_OR_LINK;
                user.put("externalIdProvider", credential.getNonInstitutionExternalIdProvider());
                user.put("externalId", credential.getNonInstitutionExternalId());
                user.put("campaign", accountManager.getCampaign());
                user.put("attributes", credential.getDelegationAttributes());
                data.put("type", "CREATE_OR_LINK_OSF_ACCOUNT");
                findAccountForm.setExternalIdProvider(credential.getNonInstitutionExternalIdProvider());
                findAccountForm.setExternalId(credential.getNonInstitutionExternalId());
            } else {
                endpoint = ApiEndpoint.SERVICE_FIND_ACCOUNT;
                data.put("type", NAME + "_FOR_" + accountManager.getTarget().toUpperCase());
            }
            data.put("user", user);

            final JSONObject response = apiEndpointHandler.handle(
                    endpoint,
                    apiEndpointHandler.encryptPayload("data", data.toString())
            );

            if (response != null) {
                final int status = response.getInt("status");
                if (status == HttpStatus.SC_NO_CONTENT) {
                    if (externalIdRegisterEmail || VerifyEmailAction.NAME.equalsIgnoreCase(accountManager.getTarget())) {
                        accountManager.setAction(VerifyEmailAction.NAME);
                        accountManager.setEmailToVerify(findAccountForm.getEmail());
                        accountManager.setTarget(null);
                    } else if (ResetPasswordAction.NAME.equalsIgnoreCase(accountManager.getTarget())) {
                        accountManager.setAction(ResetPasswordAction.NAME);
                        accountManager.setUsername(findAccountForm.getEmail());
                        accountManager.setTarget(null);
                    }
                    AbstractAccountFlowUtils.putAccountManagerToRequestContext(requestContext, accountManager);
                    return new Event(this, accountManager.getAction());
                } else if (status == HttpStatus.SC_OK) {
                    final JSONObject responseBody = response.getJSONObject("body");
                    if (responseBody != null && responseBody.has("createOrLink")) {
                       final String createOrLink = responseBody.getString("createOrLink");
                        accountManager.setAction(VerifyEmailAction.NAME);
                        accountManager.setEmailToVerify(findAccountForm.getEmail());
                        accountManager.setTarget(createOrLink);
                    }
                    AbstractAccountFlowUtils.putAccountManagerToRequestContext(requestContext, accountManager);
                    return new Event(this, accountManager.getAction());
                } else if (status == HttpStatus.SC_FORBIDDEN || status == HttpStatus.SC_UNAUTHORIZED) {
                    errorMessage = apiEndpointHandler.getErrorMessageFromResponseBody(response.getJSONObject("body"));
                }
            }
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
