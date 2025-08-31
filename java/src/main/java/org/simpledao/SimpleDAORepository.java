package org.simpledao;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SimpleDAORepository extends SimpleDAOBase
{
    private final DataSource dataSource;

    public SimpleDAORepository(DataSource dataSource) {
        // Constructor for Spring dependency injection
        this.dataSource = dataSource;
    }

    public <T>  void simpleInsert(T bean, BeanDescriptor descriptor) throws SQLException {
        try (Connection con = dataSource.getConnection())
        {
            simpleInsert(con, bean, descriptor);
        }
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

    public <T> T simpleSelect(T criteria, BeanDescriptor descriptor) throws SQLException {
        try (Connection con = dataSource.getConnection())
        {
            return simpleSelect(con, criteria, descriptor);
        }
    }

    public <T> T simpleSelect(T criteria) throws SQLException
    {
        try (Connection con = dataSource.getConnection())
        {
            return simpleSelect(con, criteria, getBeanDescriptor(criteria));
        }
    }

    public <T> List<T> simpleSelectList(T criteria ) throws SQLException
    {
        try (Connection con = dataSource.getConnection())
        {
            return simpleSelectList(con, criteria);
        }
    }

    public <T> List<T> simpleSelectList(T criteria, BeanDescriptor descriptor) throws SQLException {
        try (Connection con = dataSource.getConnection())
        {
            return simpleSelectList(con, criteria, descriptor);
        }
    }

    @Override
    public <T> void simpleDelete(T bean) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            simpleDelete(con, bean);
        }
    }

    public <T> void simpleDelete(T bean, BeanDescriptor descriptor) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            simpleDelete(con, bean, descriptor);
        }
    }

}