package io.cos.cas.adaptors.api;

/**
 * Open Science Framework API Status and Error Messages.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
final class OpenScienceFrameworkApiStatus {

    // api authentication status
    static final String AUTHENTICATION_SUCCESS = "AUTHENTICATION_SUCCESS";
    static final String AUTHENTICATION_FAILURE = "AUTHENTICATION_FAILURE";
    static final String REGISTRATION_SUCCESS = "REGISTRATION_SUCCESS";

    // api authentication error messages
    static final String MISSING_CREDENTIALS = "MISSING_CREDENTIALS";
    static final String ALREADY_REGISTERED = "ALREADY_REGISTERED";
    static final String USER_NOT_CONFIRMED = "USER_NOT_CONFIRMED";
    static final String USER_NOT_CLAIMED = "USER_NOT_CLAIMED";
    static final String USER_DISABLED = "USER_DISABLED";
    static final String USER_STATUS_INVALID = "USER_STATUS_INVALID";
    static final String ACCOUNT_NOT_FOUND = "ACCOUNT_NOT_FOUND";
    static final String INVALID_PASSWORD = "INVALID_PASSWORD";
    static final String INVALID_VERIFICATION_KEY = "INVALID_VERIFICATION_KEY";
    static final String INVALID_ONE_TIME_PASSWORD = "INVALID_ONE_TIME_PASSWORD";
    static final String TWO_FACTOR_AUTHENTICATION_REQUIRED = "TWO_FACTOR_AUTHENTICATION_REQUIRED";

    /**
     * The Default Constructor.
     */
    private OpenScienceFrameworkApiStatus() {}
}
