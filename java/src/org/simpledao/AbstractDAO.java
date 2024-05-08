package org.simpledao;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public abstract class AbstractDAO<T>
{

    protected SimpleDAO<T> dao = new SimpleDAO<T>();

    protected abstract Connection getConnection() throws SQLException;

    protected void closeConnection( Connection con)
    {
        try
        {
            if (con != null )
            {
                log.debug("close the database connection");
                con.close();
            }
        }
        catch ( SQLException ex)
        {
            log.error(ex.getMessage());
        }
    }

    public List<T> getList(T criteria)  throws SQLException
    {
        log.debug("GetList('{}' - begin", criteria.getClass().getName() );
        Connection con = null;
        try
        {
            con = getConnection();
            return dao.simpleSelectList(con, criteria);
        }
        catch ( SQLException ex)
        {
            log.error(ex.getMessage(),ex);
            throw ex;
        }
        finally
        {
            closeConnection(con);
            log.debug("GetList('{}') - end", criteria.getClass().getName());
        }
    }

    public T get(T criteria) throws SQLException
    {
        log.debug("Get('{}') - begin", criteria.getClass().getName());
        Connection con = null;
        try
        {
            con = getConnection();
            return dao.simpleSelect(con, criteria);
        }
        catch (SQLException ex)
        {
            log.error(ex.getMessage(),ex);
            throw ex;
        }
        finally
        {
            closeConnection(con);
            log.debug("Get('{}') - end", criteria.getClass().getName());
        }
    }

    public void insert(T criteria) throws SQLException
    {
        log.debug("Insert('{}') - begin", criteria.getClass().getName());

        Connection con = null;
        try
        {
            con = getConnection();
            dao.simpleInsert(con, criteria);
        }
        catch ( SQLException ex)
        {
            log.error(ex.getMessage(),ex);
            throw ex;
        }
        finally
        {
            closeConnection(con);
            log.debug("Insert('{}') - end", criteria.getClass().getName());
        }

    }

    public void update(T criteria) throws SQLException
    {
        log.debug("Update('{}') - begin", criteria.getClass().getName());
        Connection con = null;
        try
        {
            con = getConnection();
            dao.simpleUpdate(con, criteria);
        }
        catch ( SQLException ex)
        {
            log.error(ex.getMessage(),ex);
            throw ex;
        }
        finally
        {
            closeConnection(con);
            log.debug("Update('{}') - end", criteria.getClass().getName());
        }
    }

    public void delete(T criteria) throws SQLException
    {
        log.debug("GetList('{}') - begin", criteria.getClass().getName());
        Connection con = null;
        try
        {
            con = getConnection();
            dao.simpleDelete(con, criteria);
        }
        catch ( SQLException ex)
        {
            log.error(ex.getMessage(),ex);
            throw ex;
        }
        finally
        {
            closeConnection(con);
            log.debug("Delete('{}') - end", criteria.getClass().getName());
        }
    }

}
