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

package io.cos.cas.types;

/**
 * Open Science Framework Login Action Types.
 *
 * @author Longze
 * @since 4.1.5
 */
public enum OsfLoginAction {

    /** Default Login. */
    LOGIN("login"),

    /** Register. */
    REGISTER("register"),

    /** Forgot Password. */
    FORGOT_PASSWORD("forgotPassword"),

    /** Resend Confirmation Email. */
    RESEND_CONFIRMATION("resendConfirmation"),

    /** Reset Password. */
    RESET_PASSWORD("resetPassword"),

    /** Resend Confirmation Email. */
    CONFIRM_EMAIL("confirmEmail");

    private final String id;

    /**
     * Instantiate an OSF Login Action.
     *
     * @param id the id of the OSF Login Challenge
     */
    OsfLoginAction(final String id) {
        this.id = id;
    }

    /**
     * Matches and returns the OSF Login Action enumeration type of the id specified.
     *
     * @param id the id of the action
     * @return the specific action or null
     */
    public static OsfLoginAction getType(final String id) {
        if (id != null) {
            for (final OsfLoginAction type : OsfLoginAction.values()) {
                if (id.equals(type.getId())) {
                    return type;
                }
            }
        }
        return null;
    }

    /**
     * OSF Login Help.
     *
     * @param action the action
     * @return true if action is help
     */
    public static boolean isHelp(final String action) {
        return FORGOT_PASSWORD.getId().equalsIgnoreCase(action) || RESEND_CONFIRMATION.getId().equalsIgnoreCase(action);
    }

    /**
     * OSF Login Challenge.
     *
     * @param action the action
     * @return true if action is challenge
     */
    public static boolean isChallenge(final String action) {
        return REGISTER.getId().equalsIgnoreCase(action)
                || RESET_PASSWORD.getId().equalsIgnoreCase(action)
                || CONFIRM_EMAIL.getId().equalsIgnoreCase(action);
    }

    /**
     * OSF Default Login.
     *
     * @param action the action
     * @return true if action is login
     */
    public static boolean isLogin(final String action) {
        return LOGIN.getId().equalsIgnoreCase(action);
    }

    /**
     * OSF Register.
     *
     * @param action the action
     * @return true if action is register
     */
    public static boolean isRegister(final String action) {
        return REGISTER.getId().equalsIgnoreCase(action);
    }

    public final String getId() {
        return id;
    }
}
