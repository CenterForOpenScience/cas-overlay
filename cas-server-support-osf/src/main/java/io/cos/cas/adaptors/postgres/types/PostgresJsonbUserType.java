/*
 * Licensed to the Center For Open Science (COS) under one or more
 * contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright
 * ownership. COS licenses this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a copy of
 * the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.  See the License for the specific language
 * governing permissions and limitations under the License.
 */

package io.cos.cas.adaptors.postgres.types;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Customized Hibernate data type for Postgres {@literal jsonb}.
 *
 * {@link com.google.gson.JsonObject} is used as the object type / class for Postgres {@literal jsonb}.
 *
 * CAS only has read-access to the OSF DB. Thus, 1) the type is immutable; 2) {@link this#nullSafeGet} is not
 * implemented; 3) {@link this#deepCopy} simply returns the argument. Several methods are implemented with default /
 * minimal behavior by using the {@link this#deepCopy}.
 *
 * @author Longze Chen
 * @since 19.4.0
 */
public class PostgresJsonbUserType implements UserType {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.JAVA_OBJECT};
    }

    @Override
    public Class returnedClass() {
        return JsonObject.class;
    }

    @Override
    public boolean equals(final Object x, final Object y) throws HibernateException {
        if (x == null) {
            return y == null;
        }
        return x.equals(y);
    }

    @Override
    public int hashCode(final Object x) throws HibernateException {
        return x.hashCode();
    }

    @Override
    public Object nullSafeGet(
            final ResultSet rs,
            final String[] names,
            final SessionImplementor session,
            final Object owner
    ) throws HibernateException, SQLException {
        final String jsonString = rs.getString(names[0]);
        if (jsonString == null) {
            return null;
        }
        try {
            final JsonParser jsonParser = new JsonParser();
            return jsonParser.parse(jsonString).getAsJsonObject();
        } catch (final JsonSyntaxException | IllegalStateException e) {
            logger.error("PostgresJsonbUserType.nullSafeGet(): failed to convert Java JSON String to GSON JsonObject:");
            throw new RuntimeException("Failed to convert Java JSON String to GSON JsonObject: " + e.getMessage());
        }
    }

    // There is no need to implement this class since CAS only has read-access to the OSF DB.
    @Override
    public void nullSafeSet(
            final PreparedStatement st,
            final Object value,
            final int index,
            final SessionImplementor session
    ) throws HibernateException {
        throw new NotYetImplementedException();
    }

    // Immutable object: simply return the argument.
    @Override
    public Object deepCopy(final Object value) throws HibernateException {
        return value;
    }

    // Objects of this type is immutable.
    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(final Object value) throws HibernateException {
        return (Serializable) this.deepCopy(value);
    }

    @Override
    public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        return this.deepCopy(cached);
    }

    @Override
    public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
        return this.deepCopy(original);
    }
}
