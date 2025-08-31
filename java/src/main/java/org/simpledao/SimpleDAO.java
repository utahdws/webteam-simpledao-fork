package org.simpledao;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SimpleDAO<T> extends SimpleDAOBase
{
    @Override
    public <B> void simpleInsert(Connection con, B bean ) throws SQLException
    {
        super.simpleInsert( con, bean );
    }

    @Override
    public <B> void simpleInsert(Connection con, B bean, BeanDescriptor descriptor ) throws SQLException
    {
        super.simpleInsert( con, bean, descriptor );
    }

    public void simpleInsert( T bean ) throws SQLException
    {
        try (Connection con = new SimpleDBConnection().getDBConnection())
        {
            simpleInsert( con, bean, getBeanDescriptor(bean) );
        }
    }

    @Override
    public <B> void simpleUpdate(Connection con, B bean, BeanDescriptor descriptor ) throws SQLException
    {
        super.simpleUpdate( con, bean, descriptor );
    }

    public void simpleUpdate( T bean ) throws SQLException
    {
        try (Connection con = new SimpleDBConnection().getDBConnection())
        {
            simpleUpdate( con, bean, getBeanDescriptor(bean) );
        }
    }

    @Override
    public <C> void simpleUpdate(Connection con, C criteria) throws SQLException {
        super.simpleUpdate(con, criteria);
    }

    public T simpleSelect(T criteria) throws SQLException
    {
        try (Connection con = new SimpleDBConnection().getDBConnection())
        {
            return simpleSelect(con, criteria, getBeanDescriptor(criteria));
        }
    }

    @Override
    public <C> C simpleSelect(Connection con, C criteria) throws SQLException
    {
        log.debug("Get the beans properties");
        return simpleSelect(con, criteria, getBeanDescriptor(criteria));
    }

    @Override
    public <B> B simpleSelect(Connection con, B bean, BeanDescriptor descriptor) throws SQLException
    {
        return super.simpleSelect(con, bean, descriptor);
    }

    @Override
    public <C> ArrayList<C> simpleSelectList(Connection con, C criteria, BeanDescriptor descriptor ) throws SQLException
    {
        return super.simpleSelectList( con, criteria, descriptor );
    }

    public List<T> simpleSelectList(T criteria ) throws SQLException
    {
        try (Connection con = new SimpleDBConnection().getDBConnection())
        {

            return simpleSelectList( con, criteria);
        }
    }

    @Override
    public <B> ArrayList<B> simpleSelectList(Connection con, B criteria ) throws SQLException
    {
        return super.simpleSelectList(con, criteria);
    }

    @Override
    public <B> void simpleDelete(Connection con, B bean, BeanDescriptor descriptor ) throws SQLException
    {
        super.simpleDelete(con, bean, descriptor);
    }

    @Override
    public <B> void simpleDelete(Connection con, B bean ) throws SQLException
    {
        super.simpleDelete(con, bean);
    }


}
