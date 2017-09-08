package io.cos.cas.api.type;

/**
 * API Errors.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class APIErrors {

    /** Malformed Request. */
    public static final int INVALID_REQUEST = 40001;

    /** Account Not Found. */
    public static final int ACCOUNT_NOT_FOUND = 40009;

    /** Account Not Confirmed. */
    public static final int ACCOUNT_NOT_CONFIRMED = 40010;

    /** Account Not Claimed. */
    public static final int ACCOUNT_NOT_CLAIMED = 40011;

    /** Account Disabled. */
    public static final int ACCOUNT_DISABLED = 40012;

    /** Account Merged. */
    public static final int ACCOUNT_MERGED = 40013;

    /** Account Invalid or Erroneous. */
    public static final int ACCOUNT_INVALID = 40014;

    /** Invalid Password. */
    public static final int INVALID_PASSWORD = 40102;

    /** Invalid Verification Key. */
    public static final int INVALID_VERIFICATION_KEY = 40103;

    /** External Login Failed. */
    public static final int EXTERNAL_LOGIN_FAILED_ERROR = 40104;

    /** Two Factor Required. */
    public static final int TWO_FACTOR_REQUIRED = 40105;

    /** Two Factor Failed (Invalid Time-based One Time Password). */
    public static final int TWO_FACTOR_FAILED = 40106;

    /** Institution Login Failed. */
    public static final int INSTITUTION_LOGIN_FAILED_ERROR = 40107;

    /** API Request Failed. */
    public static final int REQUEST_FAILED = 50001;

    private static final int DENOMINATOR = 100;

    private int status;
    private int code;
    private String detail;

    /**
     * Constructor.
     *
     * @param code the error code
     * @param detail the error detail
     */
    public APIErrors(final int code, final String detail) {
        this.status = code / DENOMINATOR;
        this.code = code;
        this.detail = detail;
    }

    public int getStatus() {
        return status;
    }

    public int getCode() {
        return code;
    }

    public String getDetail() {
        return detail;
    }
}
