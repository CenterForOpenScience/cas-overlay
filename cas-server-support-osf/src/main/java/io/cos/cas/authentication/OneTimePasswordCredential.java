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

import org.jasig.cas.authentication.AbstractCredential;
import org.jasig.cas.authentication.CredentialMetaData;
import org.jasig.cas.authentication.UsernamePasswordCredential;

/**
 * Describes a one-time-password credential that contains an optional unique identifier and required password.
 * The primary difference between this component and {@link UsernamePasswordCredential} is that the username/ID is optional
 * in the former and requisite in the latter.
 * <p>
 * This class implements {@link CredentialMetaData} since the one-time-password is safe for long-term storage after
 * authentication. Note that metadata is stored only _after_ authentication, at which time the OTP has already
 * been consumed and by definition is no longer useful for authentication.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class OneTimePasswordCredential extends AbstractCredential {

    /** Serialization version marker. */
    private static final long serialVersionUID = 1892587671827699709L;

    /** One-time password. */
    private String passcode;

    /** Optional unique identifier. */
    private String id;

    /** Default constructor. */
    public OneTimePasswordCredential() {}

    /**
     * Creates a one-time-password with just a password.
     *
     * @param passcode Non-null cleartext one-time password value.
     */
    public OneTimePasswordCredential(final String passcode) {
        if (passcode == null) {
            throw new IllegalArgumentException("One-time password cannot be null.");
        }
        this.passcode = passcode;
    }

    /**
     * Creates a one-time-password with unique ID and passcode.
     *
     * @param id Identifier that is commonly used to look up one-time password in system of record.
     * @param passcode Non-null cleartext one-time password value.
     */
    public OneTimePasswordCredential(final String id, final String passcode) {
        this(passcode);
        this.id = id;
    }

    /**
     * @return Returns the passcode.
     */
    public final String getPasscode() {
        return this.passcode;
    }

    /**
     * @param passcode The passcode to set.
     */
    public final void setPasscode(final String passcode) {
        this.passcode = passcode;
    }

    /**
     * Gets the unique ID commonly used to look up a one-time password in a system of record.
     *
     * @return Possibly null unique ID.
     */
    public String getId() {
        return id;
    }

    public final void setId(final String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return this.id;
    }
}
