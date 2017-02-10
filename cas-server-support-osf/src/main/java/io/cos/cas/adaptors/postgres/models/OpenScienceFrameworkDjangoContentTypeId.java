package io.cos.cas.adaptors.postgres.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The Open Science Framework GUID.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
@Entity
@Table(name = "django_content_type")
public class OpenScienceFrameworkDjangoContentTypeId {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkDjangoContentTypeId.class);

    /** The Primary Key. */
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    /** The Application Label. */
    @Column(name = "app_label", nullable = false)
    private String appLabel;

    /** The Model Name. */
    @Column(name = "model", nullable = false)
    private String model;

    /** Default Constructor. */
    public OpenScienceFrameworkDjangoContentTypeId() {}

    public Integer getId() {
        return id;
    }

    public String getAppLabel() {
        return appLabel;
    }

    public String getModel() {
        return model;
    }

    @Override
    public String toString() {
        return String.format("%s_%s", appLabel, model);
    }
}

