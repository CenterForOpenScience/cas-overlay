/*
 * Copyright (c) 2020. Center for Open Science
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
package io.cos.cas.adaptors.postgres.hibernate;

import org.hibernate.dialect.PostgreSQL9Dialect;

import java.sql.Types;

/**
 * Customized Postgres dialect that supports {@literal jsonb}.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
public class OSFPostgreSQLDialect extends PostgreSQL9Dialect {

    /** The default constructor. */
    public OSFPostgreSQLDialect() {
        this.registerColumnType(Types.JAVA_OBJECT, "jsonb");
    }
}
