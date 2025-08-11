package org.simpledao;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

@Repository
public class SimpleDAORepository extends SimpleDAOBase
{
    private final DataSource dataSource;

    public SimpleDAORepository(DataSource dataSource) {
        // Constructor for Spring dependency injection
        this.dataSource = dataSource;
    }

    public <T> void simpleInsert( T bean ) throws SQLException
    {
        try (Connection con = dataSource.getConnection())
        {
            simpleInsert(con, bean, getBeanDescriptor(bean));
        }
    }

    public <T> void simpleUpdate( T bean ) throws SQLException
    {
        try (Connection con = dataSource.getConnection())
        {
            simpleUpdate(con, bean, getBeanDescriptor(bean));
        }
    }

    public <T> T simpleSelect(T criteria) throws SQLException
    {
        try (Connection con = dataSource.getConnection())
        {
            return simpleSelect(con, criteria, getBeanDescriptor(criteria));
        }
    }

    public <T> ArrayList<T> simpleSelectList(T criteria ) throws SQLException
    {
        try (Connection con = dataSource.getConnection())
        {
            return simpleSelectList(con, criteria);
        }
    }
}