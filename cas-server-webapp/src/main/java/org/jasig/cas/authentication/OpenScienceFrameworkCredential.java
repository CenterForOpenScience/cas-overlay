/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.authentication;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Credential for authenticating with a username and password.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class OpenScienceFrameworkCredential implements Credential, Serializable {

    /** Authentication attribute name for password. **/
    public static final String AUTHENTICATION_ATTRIBUTE_PASSWORD = "credential";

    /** Unique ID for serialization. */
    private static final long serialVersionUID = -3006234230814410939L;

    /** Password suffix appended to username in string representation. */
    private static final String PASSWORD_SUFFIX = "+password";

    /** Time-based One Time Password suffix appended to username in string representation. */
    private static final String ONETIMEPASSWORD_SUFFIX = "+otp";

    /** The username. */
    @NotNull
    @Size(min=1, message = "required.username")
    private String username;

    /** The password. */
    @NotNull
    @Size(min=1, message = "required.password")
    private String password;

    /** The One Time Password. */
    private String oneTimePassword;

    /** Default constructor. */
    public OpenScienceFrameworkCredential() {}

    /**
     * Creates a new instance with the given username and password.
     *
     * @param userName Non-null user name.
     * @param password Non-null password.
     */
    public OpenScienceFrameworkCredential(final String username, final String password) {
        this(username, password, null);
    }

    /**
     * Creates a new instance with the given username and password.
     *
     * @param userName Non-null user name.
     * @param password Non-null password.
     */
    public OpenScienceFrameworkCredential(final String username, final String password, final String oneTimePassword) {
        this.username = username;
        this.password = password;
        this.oneTimePassword = oneTimePassword;
    }

    /**
     * @return Returns the username.
     */
    public final String getUsername() {
        return this.username;
    }

    /**
     * @param userName The userName to set.
     */
    public final void setUsername(final String username) {
        this.username = username;
    }

    /**
     * @return Returns the password.
     */
    public final String getPassword() {
        return this.password;
    }

    /**
     * @param password The password to set.
     */
    public final void setPassword(final String password) {
        this.password = password;
    }

    /**
     * @return Returns the One Time Password.
     */
    public final String getOneTimePassword() {
        return this.oneTimePassword;
    }

    /**
     * @param password The One Time Password to set.
     */
    public final void setOneTimePassword(final String oneTimePassword) {
        this.oneTimePassword = oneTimePassword;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return this.username;
    }

    @Override
    public String toString() {
        if (this.oneTimePassword != null) {
          return this.username + PASSWORD_SUFFIX + ONETIMEPASSWORD_SUFFIX;
        }
        return this.username + PASSWORD_SUFFIX;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final OpenScienceFrameworkCredential that = (OpenScienceFrameworkCredential) o;

        if (username != null ? !username.equals(that.username) : that.username != null) {
            return false;
        }

        if (password != null ? !password.equals(that.password) : that.password != null) {
            return false;
        }

        if (oneTimePassword != null ? !oneTimePassword.equals(that.oneTimePassword) : that.oneTimePassword != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(username)
                .append(password)
                // .append(oneTimePassword)
                .toHashCode();
    }

}
