package io.cos.cas.api.type;

/**
 * API Errors.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class APIErrors {

    private int status;
    private int code;
    private String detail;

    /** Constructor. */
    public APIErrors(int code, String detail) {
        this.status = code / 100;
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
