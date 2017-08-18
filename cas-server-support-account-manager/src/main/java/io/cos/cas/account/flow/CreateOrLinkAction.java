package io.cos.cas.account.flow;

import io.cos.cas.account.model.CreateOrLinkFormBean;
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
 * Web Flow Action to Create or Link OSF Account with External Identity.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class CreateOrLinkAction {

    /** The Name of the Action. */
    public static final String NAME = "CREATE_OR_LINK";

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateOrLinkAction.class);

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
    public CreateOrLinkAction(final ApiEndpointHandler apiEndpointHandler, final RecaptchaUtils recaptchaUtils) {
        this.apiEndpointHandler = apiEndpointHandler;
        this.recaptchaUtils = recaptchaUtils;
    }

    /**
     * Prepare the Create Or Link OSF Account Page.
     *
     * @param requestContext the request context
     * @return the event
     */
    public Event preparePage(final RequestContext requestContext) {

        final String serviceUrl = AbstractAccountFlowUtils.getEncodedServiceUrl(requestContext);
        final String target = AbstractAccountFlowUtils.getTargetFromRequestContext(requestContext);
        final String campaign = AbstractAccountFlowUtils.getCampaignFromRegisteredService(requestContext);

        final OpenScienceFrameworkCredential credential = AbstractAccountFlowUtils.getCredentialFromSessionScope(requestContext);

        if (verifyExternalCredential(credential)) {
            LOGGER.info("target = {}, campaign = {}, serviceUrl = {}", target, campaign, serviceUrl);
            final AccountManager accountPageContext = new AccountManager(serviceUrl, NAME, target, campaign);
            accountPageContext.setRecaptchaSiteKey(recaptchaUtils.getSiteKey());
            requestContext.getFlowScope().put(AccountManager.ATTRIBUTE_NAME, accountPageContext.toJson());
            return new Event(this, "success");
        }

        return new Event(this, "error");
    }

    /**
     * Handle Form Submission on Create or Link OSF Account Page.
     *
     * @param requestContext the request context
     * @param createOrLinkFormBean the create or link form bean
     * @param messageContext the message context
     * @return the event
     */
    public Event submitForm(
            final RequestContext requestContext,
            final CreateOrLinkFormBean createOrLinkFormBean,
            final MessageContext messageContext
    ) {
        final AccountManager accountManager = AbstractAccountFlowUtils.getAccountManagerFromRequestContext(requestContext);
        final OpenScienceFrameworkCredential credential = AbstractAccountFlowUtils.getCredentialFromSessionScope(requestContext);
        String errorMessage = AbstractAccountFlowUtils.DEFAULT_SERVER_ERROR_MESSAGE;

        if (accountManager != null && credential != null && verifyExternalCredential(credential)) {

            if (!recaptchaUtils.verifyRecaptcha(requestContext)) {
                errorMessage = "Invalid Captcha";
                messageContext.addMessage(new MessageBuilder().error().source("action").defaultText(errorMessage).build());
                return new Event(this, "error");
            }

            final ApiEndpoint endpoint = ApiEndpoint.ACCOUNT_REGISTER_EXTERNAL;
            final JSONObject user = new JSONObject();
            final JSONObject data = new JSONObject();
            user.put("email", createOrLinkFormBean.getEmail())
                    .put("externalIdProvider", credential.getNonInstitutionExternalIdProvider())
                    .put("externalId", credential.getNonInstitutionExternalId())
                    .put("campaign", accountManager.getCampaign())
                    .put("attributes", credential.getDelegationAttributes());
            data.put("accountAction", "REGISTER_EXTERNAL").put("user", user);
            createOrLinkFormBean.setExternalIdProvider(credential.getNonInstitutionExternalIdProvider());
            createOrLinkFormBean.setExternalId(credential.getNonInstitutionExternalId());

            final JSONObject response = apiEndpointHandler.handle(
                    endpoint,
                    apiEndpointHandler.encryptPayload("data", data.toString())
            );

            if (response != null) {
                final int status = response.getInt("status");
                if (status == HttpStatus.SC_OK) {
                    final JSONObject responseBody = response.getJSONObject("body");
                    if (responseBody != null && responseBody.has("username") && responseBody.has("createOrLink")) {
                        final String username = responseBody.getString("username");
                        final String createOrLink = responseBody.getString("createOrLink");
                        accountManager.setUsername(username);
                        accountManager.setAction(VerifyEmailAction.NAME);
                        accountManager.setEmailToVerify(createOrLinkFormBean.getEmail());
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


    /**
     * Verify that the External Credential.
     *
     * @param credential the open science framework credential in the session scope
     * @return true if credential contains users' external identity
     */
    private boolean verifyExternalCredential(final OpenScienceFrameworkCredential credential) {

        if (credential.getNonInstitutionExternalIdProvider() == null || credential.getNonInstitutionExternalId() == null) {
            LOGGER.error("External Authentication Failed: Missing external id or idp in credential.");
            return false;
        }

        if (credential.isRemotePrincipal()) {
            LOGGER.error("External Authentication Failed: Remote principal is true.");
            return false;
        }

        LOGGER.info(
                "External Authentication Succeeds: idp={}, id={}",
                credential.getNonInstitutionExternalIdProvider(),
                credential.getNonInstitutionExternalId()
        );
        return true;
    }
}
