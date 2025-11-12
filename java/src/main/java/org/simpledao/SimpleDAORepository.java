package org.simpledao;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Repository
public class SimpleDAORepository extends SimpleDAOBase
{
    private final DataSource dataSource;

    public SimpleDAORepository(DataSource dataSource) {
        // Constructor for Spring dependency injection
        this.dataSource = dataSource;
    }

    public <T> void simpleInsert(T bean, BeanDescriptor descriptor) throws SQLException {
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

    // Collection-based insert using implicit descriptor
    public <T> void simpleInsert(Collection<T> beans) throws SQLException {
        if (beans == null || beans.isEmpty()) {
            return;
        }
        try (Connection con = dataSource.getConnection()) {
            boolean originalAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                simpleInsert(con, beans); // delegate to base protected method
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(originalAutoCommit);
            }
        }
    }

    // Collection-based insert using provided descriptor
    public <T> void simpleInsert(Collection<T> beans, BeanDescriptor descriptor) throws SQLException {
        if (beans == null || beans.isEmpty()) {
            return;
        }
        try (Connection con = dataSource.getConnection()) {
            boolean originalAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                simpleInsert(con, beans, descriptor); // delegate to base protected method
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(originalAutoCommit);
            }
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

    // Collection-based delete using implicit descriptor
    public <T> void simpleDelete(Collection<T> beans) throws SQLException {
        if (beans == null || beans.isEmpty()) {
            return;
        }
        try (Connection con = dataSource.getConnection()) {
            boolean originalAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                simpleDelete(con, beans); // delegate to base protected method
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(originalAutoCommit);
            }
        }
    }

    // Collection-based delete using provided descriptor
    public <T> void simpleDelete(Collection<T> beans, BeanDescriptor descriptor) throws SQLException {
        if (beans == null || beans.isEmpty()) {
            return;
        }
        try (Connection con = dataSource.getConnection()) {
            boolean originalAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                simpleDelete(con, beans, descriptor); // delegate to base protected method
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(originalAutoCommit);
            }
        }
    }

}