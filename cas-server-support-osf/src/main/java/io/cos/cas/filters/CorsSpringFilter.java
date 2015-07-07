/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.cos.cas.filters;

import com.thetransactioncompany.cors.CORSConfiguration;
import com.thetransactioncompany.cors.CORSFilter;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;

/**
 * Allows for spring bean configuration of CORS Filter Library.
 * Source: http://software.dzhuvinov.com/cors-filter-tips.html
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
@Component
public class CorsSpringFilter implements javax.servlet.Filter {
    static CORSFilter corsFilter;

    public CorsSpringFilter(CORSConfiguration corsConfiguration) throws ServletException {
        corsFilter = new CORSFilter(corsConfiguration);
    }

    public CorsSpringFilter() {
        super();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        corsFilter.doFilter(request, response, chain);
    }

    @Override
    public void destroy() {
        corsFilter.destroy();
    }
}
