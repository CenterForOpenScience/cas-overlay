package io.cos.cas.account.util;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Request;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.jasig.cas.web.support.WebUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Google reCAPTCHA Utility Class.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class RecaptchaUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecaptchaUtils.class);

    private String verifyUrl;

    private String siteKey;

    private String secretKey;

    private boolean enabled;

    /**
     * Constructor.
     *
     * @param verifyUrl the recaptcha api verification url
     * @param siteKey the site key
     * @param secretKey the secret key
     * @param enabled whether recaptcha is enabled or not
     */
    public RecaptchaUtils(final String verifyUrl, final String siteKey, final String secretKey, final boolean enabled) {
        this.verifyUrl = verifyUrl;
        this.siteKey = siteKey;
        this.secretKey = secretKey;
        this.enabled = enabled;
    }
    
    public String getSiteKey() {
        return siteKey;
    }

    /**  @return whether recaptcha is enabled. */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Verify the reCAPTCHA response.
     *
     * @param requestContext the request context
     * @return <code>true</code> if verification passed, <code>false</code> otherwise
     */
    public boolean verifyRecaptcha(final RequestContext requestContext) {

        final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);
        final String remoteAddress = request.getRemoteAddr();
        final String responseValue = request.getParameter("g-recaptcha-response");

        final ArrayList<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("secret", secretKey));
        parameters.add(new BasicNameValuePair("response", responseValue));
        if (remoteAddress != null && !remoteAddress.trim().isEmpty()) {
            parameters.add(new BasicNameValuePair("remoteip", remoteAddress));
        }

        try {
            LOGGER.debug("Verifying Recaptcha: remoteAddress={}, responseValue={}", remoteAddress, responseValue);
            final HttpResponse response = Request.Post(verifyUrl)
                    .addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"))
                    .bodyForm(parameters)
                    .execute()
                    .returnResponse();
            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                final JSONObject responseBody = new JSONObject(new BasicResponseHandler().handleEntity(response.getEntity()));
                LOGGER.trace("Response: {}", response.toString());
                if (responseBody.getBoolean("success")) {
                    return true;
                } else {
                    LOGGER.debug("Invalid Captcha! errors={}", responseBody.get("error-codes"));
                }
            }
        } catch (final IOException e) {
            LOGGER.error("An IO exception has occurred during recaptcha verification.");
            LOGGER.debug(e.getMessage());
        }

        return false;
    }
}
