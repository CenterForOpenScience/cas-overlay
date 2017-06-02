package io.cos.cas.adaptors.postgres.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * The Open Science Framework Email.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
@Entity
@Table(name = "osf_email")
public class OpenScienceFrameworkEmail {

    /** The Primary Key. */
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    /** The Email Address. */
    @Column(name = "address", nullable = false)
    private String address;

    /** The Owner of The Email Address. */
    @OneToOne
    @JoinColumn(name = "user_id")
    private OpenScienceFrameworkUser user;

    /** Default Constructor. */
    public OpenScienceFrameworkEmail() {}

    public Integer getId() {
        return id;
    }

    public String getName() {
        return address;
    }

    public OpenScienceFrameworkUser getUser() {
        return user;
    }

    @Override
    public String toString() {
        return String.format("%s [id=%d, email=%s, user=%s]", this.getClass().getSimpleName(), id, address, user.getUsername());
    }
}
