package io.cos.cas.adaptors.postgres.types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.*;
import java.util.*;

public class StringList implements UserType {

    @Override
    public int[] sqlTypes() {
        return new int[] {Types.ARRAY};
    }

    @Override
    public Class returnedClass() {
        return List.class;
    }

    @Override
    public boolean equals(Object o1, Object o2) throws HibernateException {
        return o1.equals(o2);
    }

    @Override
    public int hashCode(Object o) throws HibernateException {
        if (o != null) {
            return o.hashCode();
        } else {
            return 0;
        }
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor sessionImplementor, Object owner)
            throws HibernateException, SQLException {
        Array array = resultSet.getArray(names[0]);
        List<String> arrayList = new ArrayList<>();
        if (!resultSet.wasNull() && array != null) {
            arrayList = new ArrayList<>(Arrays.asList((String[]) array.getArray()));
        }
        return arrayList;
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, Object value, int index, SessionImplementor sessionImplementor)
            throws HibernateException, SQLException {}

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}
