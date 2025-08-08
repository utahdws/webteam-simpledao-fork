package org.simpledao;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

@Repository
public class SimpleDAORepository extends SimpleDAOBase
{
    private DataSource dataSource;

    public SimpleDAORepository(DataSource dataSource) {
        // Default constructor for Spring
        this.dataSource = dataSource;
    }

    public <T> void simpleInsert( T bean ) throws SQLException
    {
        try (datasource.getConnection)
        {
            simpleInsert( con, bean, getBeanDescriptor(bean) );
        }
    }

    public <T> void simpleUpdate( T bean ) throws SQLException
    {
        try (datasource.getConnection)
        {
            simpleUpdate( con, bean, getBeanDescriptor(bean) );
        }
    }

    public <T> T simpleSelect(T criteria) throws SQLException
    {
        try (datasource.getConnection)
        {
            return simpleSelect(con, criteria, getBeanDescriptor(criteria));
        }
    }

    public <T> ArrayList<T> simpleSelectList(T criteria ) throws SQLException
    {
        SimpleDBConnection dbc = new SimpleDBConnection();
        Connection con = null;
        try
        {
            con = dbc.getDBConnection();
            return simpleSelectList( con, criteria);
        }
        finally
        {
            dbc.closeDBConnection(con);
        }
    }
}