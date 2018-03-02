/*
  Copyright 2012 - 2015 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.oauth.profile.orcid;

import org.pac4j.core.profile.converter.Converters;
import org.pac4j.oauth.profile.OAuthAttributesDefinition;

/**
 * This class defines the attributes of the {@link OrcidProfile}.
 *
 * @author Jens Tinglev
 * @author Longze Chen
 * @since 1.7.1
 */
public class OrcidAttributesDefinition extends OAuthAttributesDefinition {

    public static final String ORCID = "common:path";
    public static final String FIRST_NAME = "personal-details:given-names";
    public static final String FAMILY_NAME = "personal-details:family-name";
    public static final String URI = "common:uri";
    public static final String CREATION_METHOD = "history:creation-method";
    public static final String CLAIMED = "history:claimed";
    public static final String LOCALE = "preferences:locale";


    public OrcidAttributesDefinition() {
        addAttribute(ORCID, Converters.stringConverter);
        addAttribute(FIRST_NAME, Converters.stringConverter);
        addAttribute(FAMILY_NAME, Converters.stringConverter);
        addAttribute(URI, Converters.stringConverter);
        addAttribute(CREATION_METHOD, Converters.stringConverter);
        addAttribute(CLAIMED, Converters.booleanConverter);
        addAttribute(LOCALE, Converters.localeConverter);
    }
}
