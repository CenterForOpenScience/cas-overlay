package io.cos.cas.adaptors.postgres.models;

import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "osf_models_apioauth2application")
public class OpenScienceFrameworkApiOauth2Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkApiOauth2PersonalAccessToken.class);

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "client_id", nullable = false, unique = true)
    @Type(type="org.hibernate.type.PostgresUUIDType")
    private UUID clientId;

    @Column(name = "client_secret", nullable = false)
    private String clientSecret;

    @Column(name = "callback_url", nullable = false)
    private String callbackUrl;

    @Column(name = "home_url", nullable = false)
    private String homeUrl;

    // TODO: handle postgres `timestamp with time zone`
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_created", nullable = false)
    private Date dateDreated;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @OneToOne
    @JoinColumn(name = "owner_id")
    private OpenScienceFrameworkUser owner;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // convert uuid to string
    public String getClientId() {
        return clientId.toString();
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getHomeUrl() {
        return homeUrl;
    }

    public Date getDateDreated() {
        return dateDreated;
    }

    public Boolean isActive() {
        return isActive;
    }

    public OpenScienceFrameworkUser getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return String.format("OAuth [id=%s, name=%s]", id, name);
    }
}
