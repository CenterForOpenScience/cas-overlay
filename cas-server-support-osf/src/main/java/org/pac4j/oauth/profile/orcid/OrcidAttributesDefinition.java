/*
 * Copyright (c) 2018. Center for Open Science
 *
 * Copyright 2012 - 2015 pac4j organization
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
package org.pac4j.oauth.profile.orcid;

import org.pac4j.core.profile.converter.Converters;
import org.pac4j.oauth.profile.OAuthAttributesDefinition;

/**
 * This class defines the attributes of the {@link OrcidProfile}.
 *
 * @author Jens Tinglev
 * @author Michael Haselton
 * @author Longze Chen
 * @since 1.6.0
 */
public class OrcidAttributesDefinition extends OAuthAttributesDefinition {

    /** The XML tag name for the attribute: path. */
    public static final String ORCID = "common:path";
    /** The XML tag name for the attribute: given-names. */
    public static final String GIVEN_NAME = "personal-details:given-names";
    /** The XML tag name for the attribute: family-name. */
    public static final String FAMILY_NAME = "personal-details:family-name";
    /** The XML tag name for the attribute: uri. */
    public static final String URI = "common:uri";
    /** The XML tag name for the attribute: creation-method. */
    public static final String CREATION_METHOD = "history:creation-method";
    /** The XML tag name for the attribute: claimed. */
    public static final String CLAIMED = "history:claimed";
    /** The XML tag name for the attribute: locale. */
    public static final String LOCALE = "preferences:locale";

    /** The normalized name for the attribute: given-names. */
    public static final String NORMALIZED_GIVEN_NAME = "given-names";

    /** The normalized name for the attribute: family-name. */
    public static final String NORMALIZED_FAMILY_NAME = "family-name";


    /** The defualt constructor. */
    public OrcidAttributesDefinition() {

        addAttribute(ORCID, Converters.stringConverter);
        addAttribute(GIVEN_NAME, Converters.stringConverter);
        addAttribute(FAMILY_NAME, Converters.stringConverter);
        addAttribute(URI, Converters.stringConverter);
        addAttribute(CREATION_METHOD, Converters.stringConverter);
        addAttribute(CLAIMED, Converters.booleanConverter);
        addAttribute(LOCALE, Converters.localeConverter);

        addAttribute(NORMALIZED_GIVEN_NAME, Converters.stringConverter);
        addAttribute(NORMALIZED_FAMILY_NAME, Converters.stringConverter);
    }
}
