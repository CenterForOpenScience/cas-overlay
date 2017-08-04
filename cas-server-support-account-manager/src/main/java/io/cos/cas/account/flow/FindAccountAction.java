package io.cos.cas.account.flow;

import io.cos.cas.account.model.FindAccountFormBean;
import io.cos.cas.account.util.AbstractAccountFlowUtils;
import io.cos.cas.account.util.RecaptchaUtils;
import io.cos.cas.api.handler.ApiEndpointHandler;
import io.cos.cas.api.type.APIErrors;
import io.cos.cas.api.type.ApiEndpoint;

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
    private ApiEndpointHandler apiEndpointHandler;

    /** The Recaptcha Utility. */
    private RecaptchaUtils recaptchaUtils;

    /**
     * Constructor.
     *
     * @param apiEndpointHandler the API Endpoint Handler
     * @param recaptchaUtils the reCAPTCHA Utility
     */
    public FindAccountAction(final ApiEndpointHandler apiEndpointHandler, final RecaptchaUtils recaptchaUtils) {
        this.apiEndpointHandler = apiEndpointHandler;
        this.recaptchaUtils = recaptchaUtils;
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
        final String userId = AbstractAccountFlowUtils.getUserIdFromRequestContext(requestContext);
        final Boolean osf4Meetings = AbstractAccountFlowUtils.getOsf4MeetingsFromRequestContext(requestContext);

        final OpenScienceFrameworkCredential credential = AbstractAccountFlowUtils.getCredentialFromSessionScope(requestContext);
        final boolean externalIdRegisterEmail = credential != null
                && credential.getNonInstitutionExternalIdProvider() != null
                && credential.getNonInstitutionExternalId() != null
                && !credential.isRemotePrincipal();

        if (externalIdRegisterEmail) {
            final AccountManager accountPageContext = new AccountManager(serviceUrl, NAME, target, campaign);
            accountPageContext.setRecaptchaSiteKey(recaptchaUtils.getSiteKey());
            requestContext.getFlowScope().put(AccountManager.ATTRIBUTE_NAME, accountPageContext.toJson());

            return new Event(this, "success");
        } else if (verifyTargetAction(target)) {
            final AccountManager accountPageContext = new AccountManager(serviceUrl, NAME, target, campaign);
            if (ResetPasswordAction.NAME.equalsIgnoreCase(target) && !userId.isEmpty()) {
                accountPageContext.setUserId(userId);
                if (osf4Meetings) {
                    accountPageContext.setOsf4Meetings(Boolean.TRUE);
                }
                accountPageContext.setAction(ResetPasswordAction.NAME);
                accountPageContext.setTarget(null);
                requestContext.getFlowScope().put(AccountManager.ATTRIBUTE_NAME, accountPageContext.toJson());
                return new Event(this, "reset");
            }
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
            final String targetAction = accountManager.getTarget();
            final JSONObject user = new JSONObject();
            final JSONObject data = new JSONObject();
            user.put("email", findAccountForm.getEmail());

            if (externalIdRegisterEmail) {
                if (!recaptchaUtils.verifyRecaptcha(requestContext)) {
                    errorMessage = "Invalid Captcha";
                    messageContext.addMessage(new MessageBuilder().error().source("action").defaultText(errorMessage).build());
                    return new Event(this, "error");
                }
                endpoint = ApiEndpoint.ACCOUNT_REGISTER_EXTERNAL;
                user.put("externalIdProvider", credential.getNonInstitutionExternalIdProvider())
                        .put("externalId", credential.getNonInstitutionExternalId())
                        .put("campaign", accountManager.getCampaign())
                        .put("attributes", credential.getDelegationAttributes());
                data.put("accountAction", "REGISTER_EXTERNAL").put("user", user);
                findAccountForm.setExternalIdProvider(credential.getNonInstitutionExternalIdProvider());
                findAccountForm.setExternalId(credential.getNonInstitutionExternalId());
            } else if (ResetPasswordAction.NAME.equalsIgnoreCase(targetAction)){
                endpoint = ApiEndpoint.ACCOUNT_PASSWORD_FORGOT;
                data.put("accountAction", "PASSWORD_FORGOT").put("user", user);
            } else if (VerifyEmailAction.NAME.equalsIgnoreCase(targetAction)) {
                endpoint = ApiEndpoint.ACCOUNT_VERIFY_OSF_RESEND;
                data.put("accountAction", "VERIFY_OSF_RESEND").put("user", user);
            } else {
                messageContext.addMessage(new MessageBuilder().error().source("action").defaultText(errorMessage).build());
                return new Event(this, "error");
            }

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
                    } else {
                        messageContext.addMessage(new MessageBuilder().error().source("action").defaultText(errorMessage).build());
                        return new Event(this, "error");
                    }
                    AbstractAccountFlowUtils.putAccountManagerToRequestContext(requestContext, accountManager);
                    return new Event(this, accountManager.getAction());
                } else if (status == HttpStatus.SC_OK) {
                    final JSONObject responseBody = response.getJSONObject("body");
                    if (responseBody != null && responseBody.has("username") && responseBody.has("createOrLink")) {
                        final String username = responseBody.getString("username");
                        final String createOrLink = responseBody.getString("createOrLink");
                        accountManager.setUsername(username);
                        accountManager.setAction(VerifyEmailAction.NAME);
                        accountManager.setEmailToVerify(findAccountForm.getEmail());
                        accountManager.setTarget(createOrLink);
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
