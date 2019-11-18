/*
 * Copyright (c) 2015. Center for Open Science
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cos.cas.authentication.oath;

import org.apache.commons.codec.binary.Base32;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Copied from @{link https://github.com/parkghost/TOTP-authentication-demo}.
 *
 * <strong>NOTE: </strong> in order to use this class in CAS maven war overlays, exclude the following jar in the
 * maven-war-plugin->configuration->overlays->excludes section of local maven overlay pom.xml: WEB-INF/lib/commons-codec-1.4.jar
 *
 * @author Dmitriy Kopylenko
 * @author Unicon, inc.
 * @author Michael Haselton
 * @since 0.5
 */
public final class TotpUtils {

    private static final int PASS_CODE_LENGTH = 6;

    private static final String CRYPTO = "HmacSHA1";

    /**
     * A private constructor for the utility class.
     */
    private TotpUtils() {
    }

    /**
     * Checks a Time-based One Time Password Code.
     *
     * @param secret the totp secret
     * @param code the code to verify
     * @param interval the totp interval
     * @param window the totp window
     * @return a boolean value indicating if the check passes or fails
     *
     * @throws NoSuchAlgorithmException On no such algorithm found to perform the check.
     * @throws InvalidKeyException On the case when the key is invalid.
     */
    public static boolean checkCode(final String secret, final long code, final int interval, final int window)
            throws NoSuchAlgorithmException, InvalidKeyException {
        final Base32 codec = new Base32();
        final byte[] decodedKey = codec.decode(secret);

        final long currentTimeSeconds = System.currentTimeMillis() / 1000;
        final long currentInterval = currentTimeSeconds / interval;

        for (int i = -window; i <= window; ++i) {
            final long hash = Totp.generateTotp(decodedKey, currentInterval + i, PASS_CODE_LENGTH, CRYPTO);

            if (hash == code) {
                return true;
            }
        }

        // The validation code is invalid.
        return false;
    }
}
