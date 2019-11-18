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
package io.cos.cas.web.flow;

import io.cos.cas.adaptors.postgres.handlers.OpenScienceFrameworkInstitutionHandler;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Open Science Framework Institution Login Handler.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
public class OpenScienceFrameworkInstitutionLoginHandler {

    /** The Institution Authentication Handler. */
    private final OpenScienceFrameworkInstitutionHandler institutionHandler;

    /**
     * Creates a new instance with the given parameters.
     * @param institutionHandler The institution handler
     */
    public OpenScienceFrameworkInstitutionLoginHandler(
            final OpenScienceFrameworkInstitutionHandler institutionHandler) {
        this.institutionHandler = institutionHandler;
    }

    /**
     * The Get Institutions Action. Return the "success" event.
     * @param context The request context
     * @return Event
     */
    public Event getInstitutions(final RequestContext context) {

        String target;
        String service = context.getRequestParameters().get("service");
        try {
            if (service != null) {
                service = URLEncoder.encode(service, "UTF-8");
                target = URLEncoder.encode(String.format("/login?service=%s", service), "UTF-8");
            } else {
                target = URLEncoder.encode("/login", "UTF-8");
            }
        } catch (final UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unknown");
        }

        final Map<String, String> institutions = this.institutionHandler.getInstitutionLoginUrls(target);
        institutions.put("", " -- select an institution -- ");
        final Map<String, String> sortedInstitutions = sortByValue(institutions);
        context.getFlowScope().put("institutions", sortedInstitutions);
        return new Event(this, "success");
    }

    /**
     * Sort a Map by value. Return the sorted Map.
     * References: http://www.programcreek.com/2013/03/java-sort-map-by-value/
     *             https://www.mkyong.com/java/how-to-sort-a-map-in-java/
     *
     * @param map The Map to be sorted
     * @param <K> The Type of the Key
     * @param <V> The Type of the Value
     * @return Map&lt;K,V&gt;
     */
    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(final Map<K, V> map) {
        final List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(final Map.Entry<K, V> e1, final Map.Entry<K, V> e2) {
                return (e1.getValue()).compareTo(e2.getValue());
            }
        });
        final Map<K, V> result = new LinkedHashMap<>();
        for (final Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
