package org.simpledao;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

@Slf4j
public class SimpleDAO<T> extends SimpleDAOBase
{
    public void simpleInsert( T bean ) throws SQLException
    {
        try (Connection con = new SimpleDBConnection().getDBConnection())
        {
            simpleInsert( con, bean, getBeanDescriptor(bean) );
        }
    }

    public void simpleUpdate( T bean ) throws SQLException
    {
        try (Connection con = new SimpleDBConnection().getDBConnection())
        {
            simpleUpdate( con, bean, getBeanDescriptor(bean) );
        }
    }

    public T simpleSelect(T criteria) throws SQLException
    {
        try (Connection con = new SimpleDBConnection().getDBConnection())
        {
            return simpleSelect(con, criteria, getBeanDescriptor(criteria));
        }
    }

    public ArrayList<T> simpleSelectList(T criteria ) throws SQLException
    {
        try (Connection con = new SimpleDBConnection().getDBConnection())
        {

            return simpleSelectList( con, criteria);
        }
    }
}
