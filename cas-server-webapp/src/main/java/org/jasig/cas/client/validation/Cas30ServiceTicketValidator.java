/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
package org.jasig.cas.client.validation;

/**
 * Service tickets validation service for the CAS protocol v3.
 *
 * @author Jerome Leleu
 * @author Longze Chen
 * @since 4.1.0
 */
public class Cas30ServiceTicketValidator extends Cas20ServiceTicketValidator {

    /**
     * Instantiate an Service Ticket Validator using CAS 3.0 protocol.
     *
     * @param casServerUrlPrefix the CAS server URL prefix
     */
    public Cas30ServiceTicketValidator(final String casServerUrlPrefix) {
        super(casServerUrlPrefix);
    }

    @Override
    protected String getUrlSuffix() {
        return "p3/serviceValidate";
    }
}