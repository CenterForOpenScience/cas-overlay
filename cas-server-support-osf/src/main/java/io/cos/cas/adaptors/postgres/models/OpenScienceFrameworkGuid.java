package io.cos.cas.adaptors.postgres.models;

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

/**
 * The Open Science Framework GUID.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
@Entity
@Table(name = "osf_guid")
public class OpenScienceFrameworkGuid {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenScienceFrameworkGuid.class);

    /** The Primary Key. */
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    /** The GUID of the Object. */
    @Column(name = "_id", nullable = false)
    private String guid;

    /** The Primary Key of the Object. */
    @Column(name = "object_id", nullable = false)
    private Integer objectId;

    /** The Content Type of the Object. */
    @OneToOne
    @JoinColumn(name = "content_type_id")
    private OpenScienceFrameworkDjangoContentTypeId djangoContentType;

    /** The Date Created. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created", nullable = false)
    private Date created;

    /** The Default Constructor. */
    public OpenScienceFrameworkGuid() {}

    public Integer getId() {
        return id;
    }

    public String getGuid() {
        return guid;
    }

    public Integer getObjectId() {
        return objectId;
    }

    public OpenScienceFrameworkDjangoContentTypeId getDjangoContentType() {
        return djangoContentType;
    }

    @Override
    public String toString() {
        return String.format(
            "OpenScienceFrameworkGuid [guid=%s, objectId=%d, djangoContentTypeId=%s]",
            guid,
            objectId,
            djangoContentType.getId()
        );
    }
}
