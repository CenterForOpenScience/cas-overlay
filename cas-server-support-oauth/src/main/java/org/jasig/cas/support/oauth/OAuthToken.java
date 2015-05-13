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

package org.jasig.cas.support.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;


/**
 * Simple JSON OAuth Access Token
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class OAuthToken {
    @JsonProperty("st")
    public String serviceTicketId;
    @JsonProperty("tgt")
    public String ticketGrantingTicketId;
    @JsonProperty("svc")
    public String serviceId;

    public OAuthToken() {
    }

    public OAuthToken(String serviceTicketId) {
        this.serviceTicketId = serviceTicketId;
    }

    public OAuthToken(String ticketGrantingTicketId, String serviceId) {
        this.ticketGrantingTicketId = ticketGrantingTicketId;
        this.serviceId = serviceId;
    }

    public static OAuthToken read(String content)
        throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(content, OAuthToken.class);
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
