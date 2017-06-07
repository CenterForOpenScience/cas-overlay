package io.cos.cas.account.model;

import java.io.Serializable;

/**
 * The Model for Find Account Form.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class FindAccountFormBean implements Serializable {

    private static final long serialVersionUID = -2314704141363230320L;

    private String action;

    private String email;

    private String externalIdProvider;

    private String externalId;

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email.trim().toLowerCase();
    }

    public String getExternalIdProvider() {
        return externalIdProvider;
    }

    public void setExternalIdProvider(final String externalIdProvider) {
        this.externalIdProvider = externalIdProvider;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(final String externalId) {
        this.externalId = externalId;
    }
}
