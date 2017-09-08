package io.cos.cas.authentication.oauth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

/**
 * TOTP implementation is copied from @{link https://github.com/parkghost/TOTP-authentication-demo}.
 *
 * @author Dmitriy Kopylenko
 * @author Unicon, inc.
 * @author Michael Haselton
 * @since 0.2
 */
public final class Totp {

    private static final int[] DIGITS_POWER = {
            // 0 1 2 3 4 5 6 7 8
            1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000
    };
    private static final int HASH_BUFFER_CAPACITY = 8;

    private static final int HASH_OFFSET_1 = 1;

    private static final int HASH_OFFSET_2 = 2;

    private static final int HASH_OFFSET_3 = 3;

    private static final int BITMASK_15 = 0xf;

    private static final int BITMASK_127 = 0x7f;

    private static final int BITMASK_255 = 0xff;

    private static final int BITWISE_AND_8 = 8;

    private static final int BITWISE_AND_16 = 16;

    private static final int BITWISE_AND_24 = 24;

    /**
     * Constructs a new instance of the time-based one time password class.
     */
    private Totp() {
    }

    /**
     * This method uses the JCE to provide the crypto algorithm. HMAC computes a
     * Hashed Message Authentication Code with the crypto hash algorithm as a
     * parameter.
     *
     * @param crypto the crypto algorithm (HmacSHA1, HmacSHA256, HmacSHA512)
     * @param keyBytes the bytes to use for the HMAC key
     * @param text the message or text to be authenticated
     * @return the HMAC sha
     */
    private static byte[] hmacSha(final String crypto, final byte[] keyBytes, final byte[] text) {
        try {
            final Mac hmac = Mac.getInstance(crypto);
            final SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (final GeneralSecurityException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * This method generates a TOTP value for the given set of parameters.
     *
     * @param key the shared secret
     * @param time a value that reflects a time
     * @param digits number of digits to return
     * @param crypto the crypto function to use
     *
     * @return digits
     */
    public static int generateTotp(final byte[] key, final long time, final int digits, final String crypto) {
        final byte[] msg = ByteBuffer.allocate(HASH_BUFFER_CAPACITY).putLong(time).array();
        final byte[] hash = hmacSha(crypto, key, msg);

        // put selected bytes into result int
        final int offset = hash[hash.length - 1] & BITMASK_15;

        int binary = ((hash[offset] & BITMASK_127) << BITWISE_AND_24);
        binary = binary | ((hash[offset + HASH_OFFSET_1] & BITMASK_255) << BITWISE_AND_16);
        binary = binary | ((hash[offset + HASH_OFFSET_2] & BITMASK_255) << BITWISE_AND_8);
        binary = binary | (hash[offset + HASH_OFFSET_3] & BITMASK_255);

        return binary % DIGITS_POWER[digits];
    }
}
