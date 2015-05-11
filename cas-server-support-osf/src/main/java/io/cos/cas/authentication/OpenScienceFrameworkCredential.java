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
package io.cos.cas.authentication;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jasig.cas.authentication.RememberMeUsernamePasswordCredential;


/**
 * Credential for authenticating with a username and password.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class OpenScienceFrameworkCredential extends RememberMeUsernamePasswordCredential {

    /** Unique ID for serialization. */
    private static final long serialVersionUID = -3006234230814410939L;

    /** Time-based One Time Password suffix appended to username in string representation. */
    private static final String ONETIMEPASSWORD_SUFFIX = "+otp";

    /** The One Time Password. */
    private String oneTimePassword;

    /** Default constructor. */
    public OpenScienceFrameworkCredential() {
    }

    /**
     * Creates a new instance with the given username and password.
     *
     * @param username Non-null user name.
     * @param password Non-null password.
     * @param rememberMe Non-null remember me.
     */
    public OpenScienceFrameworkCredential(final String username, final String password, final Boolean rememberMe) {
        this(username, password, rememberMe, null);
    }

    /**
     * Creates a new instance with the given username and password.
     *
     * @param username Non-null user name.
     * @param password Non-null password.
     * @param rememberMe Non-null remember me.
     * @param oneTimePassword Non-null one time password.
     */
    public OpenScienceFrameworkCredential(final String username, final String password, Boolean rememberMe, final String oneTimePassword) {
        this.setUsername(username);
        this.setPassword(password);
        this.setRememberMe(rememberMe);
        this.oneTimePassword = oneTimePassword;
    }

    /**
     * @return Returns the One Time Password.
     */
    public final String getOneTimePassword() {
        return this.oneTimePassword;
    }

    /**
     * @param oneTimePassword The One Time Password to set.
     */
    public final void setOneTimePassword(final String oneTimePassword) {
        this.oneTimePassword = oneTimePassword;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return this.getUsername();
    }

    @Override
    public String toString() {
        if (this.oneTimePassword != null) {
          return super.toString() + ONETIMEPASSWORD_SUFFIX;
        }
        return super.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OpenScienceFrameworkCredential other = (OpenScienceFrameworkCredential) obj;
        if (this.oneTimePassword != other.oneTimePassword) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(oneTimePassword)
                .toHashCode();
    }

}
