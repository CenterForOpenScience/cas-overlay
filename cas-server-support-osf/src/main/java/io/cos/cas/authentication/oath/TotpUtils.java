package io.cos.cas.authentication.oath;

import org.apache.commons.codec.binary.Base32;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

/**
 * Copied from @{link https://github.com/parkghost/TOTP-authentication-demo}
 *
 * <strong>NOTE: </strong> in order to use this class in CAS maven war overlays, exclude the following jar in the
 * maven-war-plugin->configuration->overlays->excludes section of local maven overlay pom.xml: WEB-INF/lib/commons-codec-1.4.jar
 *
 * @since 0.5
 */
public class TotpUtils {

    private static final int SECRET_SIZE = 10;

    private static final int PASS_CODE_LENGTH = 6;

    private static final String CRYPTO = "HmacSHA1";

    private static final Random rand = new Random();

    public static String generateSecret() {

        // Allocating the buffer
        byte[] buffer = new byte[SECRET_SIZE];

        // Filling the buffer with random numbers.
        rand.nextBytes(buffer);

        // Getting the key and converting it to Base32
        Base32 codec = new Base32();
        byte[] secretKey = Arrays.copyOf(buffer, SECRET_SIZE);
        byte[] encodedKey = codec.encode(secretKey);
        return new String(encodedKey);
    }

    public static boolean checkCode(String secret, long code, int interval, int window) throws NoSuchAlgorithmException, InvalidKeyException {
        Base32 codec = new Base32();
        byte[] decodedKey = codec.decode(secret);

        // Window is used to check codes generated in the near past.
        // You can use this value to tune how far you're willing to go.
        //int window = WINDOW;
        long currentInterval = getCurrentInterval(interval);

        for (int i = -window; i <= window; ++i) {
            long hash = Totp.generateTotp(decodedKey, currentInterval + i, PASS_CODE_LENGTH, CRYPTO);

            if (hash == code) {
                return true;
            }
        }

        // The validation code is invalid.
        return false;
    }

    private static long getCurrentInterval(int interval) {
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        return currentTimeSeconds / interval;
    }
}
