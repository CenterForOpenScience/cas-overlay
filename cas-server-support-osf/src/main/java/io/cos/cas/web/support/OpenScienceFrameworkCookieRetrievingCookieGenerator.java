/*
 * Copyright (c) 2016. Center for Open Science
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
package io.cos.cas.web.support;

import org.jasig.cas.web.support.CookieValueManager;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;

import javax.servlet.http.Cookie;

/**
 * Add HttpOnly Support for CookieRetrievingCookieGenerator. This is a temporary fix.
 *
 * @author Longze Chen
 * @since 4.1.0
 */
public class OpenScienceFrameworkCookieRetrievingCookieGenerator extends CookieRetrievingCookieGenerator {

    /**
     * Constructor with CookieValueManager.
     *
     * @param casCookieValueManager CAS Cookie Value Manager
     */
    public OpenScienceFrameworkCookieRetrievingCookieGenerator(final CookieValueManager casCookieValueManager) {
        super(casCookieValueManager);
    }

    /**
     * Override CookieGenerator.createCookie(String cookieValue) to add HttpOnly.
     *
     * @param cookieValue Cookie Value
     * @return return the cookie
     */
    @Override
    protected Cookie createCookie(final String cookieValue) {
        final Cookie cookie = super.createCookie(cookieValue);
        cookie.setHttpOnly(super.isCookieHttpOnly());
        return cookie;
    }
}
