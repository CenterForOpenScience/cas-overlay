package io.cos.cas.account.flow;

import io.cos.cas.api.handler.ApiEndpointHandler;

public class ResetPasswordAction {

    /** The Name of the Action. */
    public static final String NAME = "resetPassword";

    /** The API Endpoint Handler. */
    private ApiEndpointHandler apiEndpointHandler;

    /**
     * Constructor.
     *
     * @param apiEndpointHandler the API Endpoint Handler
     */
    public ResetPasswordAction(final ApiEndpointHandler apiEndpointHandler) {
        this.apiEndpointHandler = apiEndpointHandler;
    }
}
