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
package org.jasig.cas.services;

import org.jasig.cas.authentication.principal.Service;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Mutable registered service that uses String equality check for
 * service matching. Matching is case insensitive.
 *
 * @author Longze Chen
 * @since 4.1.5
 */
@Entity
@DiscriminatorValue("osf")
public class OSFRegisteredService extends AbstractRegisteredService {

    private static final long serialVersionUID = 2028857446020394771L;

    private transient String servicePattern;

    @Override
    public void setServiceId(final String id) {
        serviceId = id;
        servicePattern = null;
    }

    @Override
    public boolean matches(final Service service) {
        if (servicePattern == null) {
            servicePattern = serviceId;
        }
        return service != null && servicePattern.equalsIgnoreCase(service.getId());
    }

    @Override
    protected AbstractRegisteredService newInstance() {
        return new OSFRegisteredService();
    }

}
