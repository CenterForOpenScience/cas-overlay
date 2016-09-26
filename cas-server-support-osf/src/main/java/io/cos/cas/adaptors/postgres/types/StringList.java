package io.cos.cas.adaptors.postgres.types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class StringList implements UserType {

    @Override
    public int[] sqlTypes() {
        return new int[] {Types.VARCHAR};
    }

    @Override
    public Class returnedClass() {
        return List.class;
    }

    @Override
    public boolean equals(Object o1, Object o2) throws HibernateException {
        // TO-DO: remove deprecated apache method
        // return ObjectUtils.equals(var1, var2);
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
        List<String> list = null;
        String nameVal = resultSet.getString(names[0]);
        if (nameVal != null ) {
            nameVal = nameVal.substring(1, nameVal.length()-1);
            list = new ArrayList<>();
            StringTokenizer tokenizer = new StringTokenizer(nameVal, ",");
            while(tokenizer.hasMoreElements()) {
                String val = (String) tokenizer.nextElement();
                list.add(val);
            }
        }
        return list;
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, Object value, int index, SessionImplementor sessionImplementor)
            throws HibernateException, SQLException {
        if (value == null) {
            preparedStatement.setNull(index, Types.VARCHAR);
        } else {
            preparedStatement.setString(index, serialize((List<String>) value));
        }
    }

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

    private String serialize(List<String> list) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iter = list.iterator();
        builder.append("{");
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (iter.hasNext()) {
                builder.append(",");
            }
        }
        builder.append("}");
        return builder.toString();
    }
}
