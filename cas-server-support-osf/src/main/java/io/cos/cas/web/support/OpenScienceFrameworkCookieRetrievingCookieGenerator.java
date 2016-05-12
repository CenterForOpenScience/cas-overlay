package io.cos.cas.web.support;

import org.jasig.cas.web.support.CookieValueManager;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;

import javax.servlet.http.Cookie;


public class OpenScienceFrameworkCookieRetrievingCookieGenerator extends CookieRetrievingCookieGenerator {

    public OpenScienceFrameworkCookieRetrievingCookieGenerator(CookieValueManager casCookieValueManager) {
        super(casCookieValueManager);
    }

    @Override
    protected Cookie createCookie(String cookieValue) {
        Cookie cookie = super.createCookie(cookieValue);
        cookie.setHttpOnly(super.isCookieHttpOnly());
        return cookie;
    }
}
