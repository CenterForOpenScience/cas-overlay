package io.cos.cas.adaptors.postgres.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The OpenScience Framework API OAuth2 Application.
 *
 * @author Micael Haselton
 * @author Longze Chen
 * @since 4.1.0
 */
@Entity
@Table(name = "osf_models_apioauth2application")
public class OpenScienceFrameworkApiOauth2Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkApiOauth2PersonalAccessToken.class);

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name ="_id", nullable = false)
    private String objectId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "client_id", nullable = false, unique = true)
    private String clientId;

    @Column(name = "client_secret", nullable = false)
    private String clientSecret;

    @Column(name = "callback_url", nullable = false)
    private String callbackUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public String getId() {
        return objectId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    @Override
    public String toString() {
        return String.format("OpenScienceFrameworkApiOauth2Application [_id=%s, name=%s]", objectId, name);
    }
}
