package io.cos.cas.account.flow;

import io.cos.cas.account.model.RegisterFormBean;
import io.cos.cas.account.util.AbstractAccountFlowUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Web Flow Action to register a new OSF user.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class RegisterAction {

    /** The Name of the Action. */
    public static final String NAME = "register";

    /**
     * Prepare the Register Page.
     *
     * @param requestContext the request context
     * @return the event
     */
    public Event preparePage(final RequestContext requestContext) {
        final String serviceUrl = AbstractAccountFlowUtils.getEncodedServiceUrl(requestContext);
        final String campaign = AbstractAccountFlowUtils.getCampaignFromRegisteredService(requestContext);
        final AccountManagerPageContext accountPageContext = new AccountManagerPageContext(serviceUrl, NAME, campaign);
        requestContext.getFlowScope().put("jsonAccountPageContext", accountPageContext.toJson());
        return new Event(this, "success");
    }

    /**
     * Handle Register From Submission.
     *
     * @param requestContext the request context
     * @param registerFrom the register from bean
     * @param messageContext the message context
     * @return the event
     */
    public Event submitForm(final RequestContext requestContext, final RegisterFormBean registerFrom, final MessageContext messageContext) {
        messageContext.addMessage(
                new MessageBuilder().error().source("action").defaultText("Registration Successful").build()
        );
        return new Event(this, "error");
    }
}
