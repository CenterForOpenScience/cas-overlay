package io.cos.cas.adaptors.api;

/**
 * Open Science Framework API Status and Error Messages.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
public final class OpenScienceFrameworkApiStatus {

    // api authentication status
    public static final String AUTHENTICATION_SUCCESS = "AUTHENTICATION_SUCCESS";
    public static final String AUTHENTICATION_FAILURE = "AUTHENTICATION_FAILURE";
    public static final String REGISTRATION_SUCCESS = "REGISTRATION_SUCCESS";

    // api authentication error messages
    public static final String MISSING_CREDENTIALS = "MISSING_CREDENTIALS";
    public static final String ALREADY_REGISTERED = "ALREADY_REGISTERED";
    public static final String USER_NOT_CONFIRMED = "USER_NOT_CONFIRMED";
    public static final String USER_NOT_CLAIMED = "USER_NOT_CLAIMED";
    public static final String USER_DISABLED = "USER_DISABLED";
    public static final String USER_STATUS_INVALID = "USER_STATUS_INVALID";
    public static final String ACCOUNT_NOT_FOUND = "ACCOUNT_NOT_FOUND";
    public static final String INVALID_PASSWORD = "INVALID_PASSWORD";
    public static final String INVALID_VERIFICATION_KEY = "INVALID_VERIFICATION_KEY";
    public static final String INVALID_ONE_TIME_PASSWORD = "INVALID_ONE_TIME_PASSWORD";
    public static final String TWO_FACTOR_AUTHENTICATION_REQUIRED = "TWO_FACTOR_AUTHENTICATION_REQUIRED";

    /**
     * The Default Constructor.
     */
    private OpenScienceFrameworkApiStatus() {}
}
