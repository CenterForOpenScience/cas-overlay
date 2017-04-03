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

package io.cos.cas.adaptors.postgres.types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The String List User Type.
 *
 * @author Longze Chen
 * @since 4.0.1
 */
public class StringListUserType implements UserType {

    @Override
    public int[] sqlTypes() {
        return new int[] {Types.ARRAY};
    }

    @Override
    public Class returnedClass() {
        return List.class;
    }

    @Override
    public boolean equals(final Object o1, final Object o2) throws HibernateException {
        return o1.equals(o2);
    }

    @Override
    public int hashCode(final Object o) throws HibernateException {
        return o != null ? o.hashCode() : 0;
    }

    @Override
    public Object nullSafeGet(
            final ResultSet resultSet,
            final String[] names,
            final SessionImplementor sessionImplementor,
            final Object owner
        ) throws HibernateException, SQLException {

        final Array array = resultSet.getArray(names[0]);
        if (!resultSet.wasNull() && array != null) {
            return new ArrayList<>(Arrays.asList((String[]) array.getArray()));
        }
        return null;
    }

    @Override
    public void nullSafeSet(
            final PreparedStatement preparedStatement,
            final Object value,
            final int index,
            final SessionImplementor sessionImplementor
        ) throws HibernateException, SQLException {
        // no need to implement this method since CAS is postgres readonly.
    }

    @Override
    public Object deepCopy(final Object value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(final Object value) throws HibernateException {
        return (Serializable) value;
    }

    @Override
    public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
        return original;
    }
}
