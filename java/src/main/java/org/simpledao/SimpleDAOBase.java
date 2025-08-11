package org.simpledao;

import lombok.extern.slf4j.Slf4j;
import org.simpledao.exceptions.SimpleDAOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.simpledao.exceptions.SimpleDAOBaseRuntimeException;

//todo: insert byte array blob
//todo: handle clob in insert

@Slf4j()
public class SimpleDAOBase {
    // This Class should never be instantiated directly, but rather use SimpleDAO or SimpleDAORepository
    // which are the concrete implementations of this class.

    private static final Logger sqlLog = LoggerFactory.getLogger("SQL");


    /**
     * Insert data into the database based on columns introspected from the bean
     * @param con Connection object used to communicate with the database (JDBC)
     * @param bean SimpleBean derived class that has the approprate getters/setters
     * @throws SQLException catch-all
     * @see SimpleBean
     */
    public <T> void simpleInsert( Connection con, T bean ) throws SQLException
    {
        simpleInsert(con, bean, getBeanDescriptor(bean));
    }

    /**
     * Creates and executes a SQL INSERT statement against the passed in Connection object.
     * The columns to be inserted along with their values are ascertained from the passed in
     * SimpleBean derived class and the associated Map of bean proeprties.
     * @param con Connection object used to communicate with the database (JDBC)
     * @param bean SimpleBean derived class that has the approprate getters/setters
     * @param description A description of the bean
     * @throws SQLException catch-all
     * @see SimpleBean
     */
    public <T> void simpleInsert( Connection con, T bean, BeanDescriptor description ) throws SQLException
    {
        PreparedStatement ps = buildInsertStatement(bean, description, con);
        ps.executeUpdate();
        ps.close();

    }

    public <T> T simpleSelect( Connection con, T criteria) throws SQLException
    {
        log.debug("Get the beans properties");
        return simpleSelect(con, criteria, getBeanDescriptor(criteria));
    }


    public <T> T simpleSelect( Connection con, T criteria, BeanDescriptor descriptor ) throws SQLException
    {
        log.debug("call simpleSelectList and get the first bean");

        ArrayList<T> beanList = simpleSelectList(con, criteria, descriptor);
        if (!beanList.isEmpty())
        {
            return beanList.getFirst();
        }
        else
        {
            return null;
        }
    }

    public <T> ArrayList<T> simpleSelectList( Connection con, T criteria) throws SQLException
    {
        return simpleSelectList( con, criteria, getBeanDescriptor(criteria));
    }

    public <T> ArrayList<T> simpleSelectList( Connection con, T bean, BeanDescriptor descriptor ) throws SQLException
    {
        ArrayList<T> beanList = new ArrayList<>();
        Map<String,String> columnPropertyMap = Utils.getColumnPropertyMap( descriptor.getPropertyMap());

        try (PreparedStatement ps = buildSelectStatement( bean, descriptor, con );
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while ( rs.next() )
            {
                HashMap<String,Object> props = new HashMap<>();
                for ( int i = 1; i <= columnCount ; i++)
                {
                    if ( columnPropertyMap.containsKey(metaData.getColumnName((i)).toUpperCase()))
                    {
                        if ( metaData.getColumnType(i) == Types.BLOB || metaData.getColumnTypeName(i).equalsIgnoreCase("bytea") )
                        {
                            log.debug("simpleSelectList - column # '{}' is a BLOB", i);

                            Blob blob = rs.getBlob( metaData.getColumnName(i).toUpperCase() );

                            if ( blob != null )
                            {
                                log.debug("simpleSelectList - column # '{}' BLOB is not null, write it to bean", i);

                                ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                                BufferedInputStream bis = new BufferedInputStream(blob.getBinaryStream());

                                byte[] buffer = new byte[1024];
                                int curByte;
                                try
                                {
                                    while ((curByte = bis.read(buffer, 0, buffer.length)) != -1)
                                    {
                                        baos.write(buffer, 0, curByte);
                                    }
                                } catch (IOException e)
                                {
                                    log.error("Unable to write BLOB", e);
                                    throw new SimpleDAOBaseRuntimeException("Unable to read the blob from the database", e);
                                }
                                props.put( columnPropertyMap.get(metaData.getColumnName(i).toUpperCase()), baos.toByteArray() );
                            }
                        }
                        else if  ( metaData.getColumnType(i) == Types.CLOB || metaData.getColumnTypeName(i).equalsIgnoreCase("text") )
                        {
                            log.debug("simpleSelectList - write CLOB to bean'");
                            props.put(columnPropertyMap.get(metaData.getColumnName(i).toUpperCase()), rs.getString(i));

                        }
                        else if ( metaData.getColumnType(i) == Types.DATE || metaData.getColumnTypeName(i).equalsIgnoreCase("date") )
                        {
                            log.debug("simpleSelectList - column # '{}' is a DATE", i);
                            props.put( columnPropertyMap.get( metaData.getColumnName(i).toUpperCase()), rs.getTimestamp(i) );
                        }
                        else if ( metaData.getColumnType(i) == Types.TIME || metaData.getColumnTypeName(i).equalsIgnoreCase("time") )
                        {
                            log.debug("simpleSelectList - column # '{}' is a TIME", i);
                            props.put( columnPropertyMap.get( metaData.getColumnName(i).toUpperCase()), rs.getTime(i) );
                        }
                        else if ( metaData.getColumnType(i) == Types.TIMESTAMP || metaData.getColumnTypeName(i).equalsIgnoreCase("timestamp"))
                        {
                            log.debug("simpleSelectList - column # '{}' is a TIMESTAMP", i);
                            props.put( columnPropertyMap.get( metaData.getColumnName(i).toUpperCase()), rs.getTimestamp(i) );
                        }
                        else
                        {
                            log.debug("simpleSelectList - column # '{}' is not special", i);
                            props.put( columnPropertyMap.get( metaData.getColumnName(i).toUpperCase()), rs.getString(i) );
                        }
                    }
                }

                // create the return bean
                T newBean;
                try
                {
                    newBean = (T)bean.getClass().newInstance();
                }
                catch (Exception e)
                {
                    log.error("Unable to create new bean", e);
                    throw new SimpleDAOBaseRuntimeException("Unable to instantiate the new Object",e);
                }

                ReflectionUtils.populateBean(newBean, props);
                beanList.add( newBean );
            }
        }

        return beanList;
    }


    /**
     * Introspects the passed SimpleBean for columns that need to be updated.  The
     * resulting Map of columns is then passed to the simpleUpdate method that
     * accepts a Map parameter.
     *
     * @param con Connection object used to communicate with the database (JDBC) and run the UPDATE
     * @param bean  SimpleBean derived class with getters and setters that holds the values to update.
     * This method introspects the passed SimpleBean for columns to update
     * @throws SQLException catch-all
     * @see SimpleBean
     */
    public <T> void simpleUpdate( Connection con, T bean ) throws SQLException
    {
        simpleUpdate(con, bean, getBeanDescriptor(bean));
    }

    /**
     * Creates and runs a SQL UPDATE statement against the passed database connection object.
     * The columns to be updated along with their values are ascertained from the passed in
     * SimpleBean derived class and Map of columns.
     *
     * @param con Connection object used to communicate with the database (JDBC) and run the UPDATE
     * @param bean  SimpleBean derived class with getters and setters that holds the values to update
     * @param description A Map of SimpleBean properties that will be used as columns to update
     * @throws SQLException catch-all
     * @see SimpleBean
     */
    public <T> void simpleUpdate( Connection con, T bean,BeanDescriptor description ) throws SQLException
    {
        PreparedStatement ps = buildUpdateStatement(bean, description, con);
        ps.executeUpdate();
        ps.close();
    }

    public <T> void simpleDelete( T bean ) throws SQLException
    {
        try (Connection con = new SimpleDBConnection().getDBConnection())
        {
            simpleDelete( con, bean, getBeanDescriptor(bean) );
        }
    }

    /**
     *
     * @param con Connection object used to run the DELETE statement against
     * @param bean SimpleBean derived class used to determine the table name and WHERE clause
     * @throws SQLException catch-all
     * @see SimpleBean
     */
    public <T> void simpleDelete( Connection con, T bean ) throws SQLException
    {
        simpleDelete(con, bean, getBeanDescriptor(bean));
    }


    /**
     * Creates and executes a SQL DELETE statement against the Connection parameter.
     * The table name is determined from the SimpleBean derived class and the WHERE clause
     * is ascertained from the Map of SimpleBean properties.
     *
     * @param con Connection object used to run the DELETE statement against
     * @param bean SimpleBean derived class used to determine the table name and WHERE clause
     * @param description Map of SimpleBean properties used to determine WHERE clause
     * @throws SQLException catch-all
     */
    public <T> void simpleDelete( Connection con, T bean, BeanDescriptor description ) throws SQLException
    {
        PreparedStatement ps = buildDeleteStatement(bean, description, con);
        ps.executeUpdate();
        ps.close();
    }


    //todo: handle BeanDescriptor SQL Statement
    private <T> PreparedStatement buildInsertStatement(T bean, BeanDescriptor description, Connection con ) throws SQLException
    {
        ArrayList<BoundVariable> bindVariables = new ArrayList<>();
        StringBuilder sql = new StringBuilder("INSERT INTO " );
        StringBuilder valuesSQL = new StringBuilder(" ) VALUES ( ");
        int propCount = 0;

        sql.append( description.getTable() );
        sql.append( " ( " );

        for (String property : description.getPropertyMap().keySet())
        {
            String column = description.getPropertyMap().get(property).getName();
            // if the database column is not already specified (unlikely), then determine it
            if (column == null || column.isEmpty())
            {
                column = Utils.getPropertyDBName(property);
            }

            PropertyDescriptor pd;
            Object value;
            try
            {
                pd = BeanUtils.getPropertyDescriptor( bean.getClass(), property);
                value = pd.getReadMethod().invoke(bean);
            }
            catch (Exception e)
            {
                log.error("Unable to find bean property named '{}'", property, e);
                throw new SimpleDAOBaseRuntimeException("Unable to get the bean property named '" + property + "'",e);
            }

            Class<?> type = pd.getPropertyType();
            boolean isInt = type == Integer.class || type == int.class;
            boolean isDouble = type == Double.class || type == double.class;
            if (value == null || (isInt && (Integer) value < 0) || (isDouble && (Double) value < 0.0d))
            {
                continue;
            }

            if (propCount > 0)
            {
                sql.append(", ");
                valuesSQL.append(", ");
            }
            sql.append(column);
            valuesSQL.append("?");

            propCount++;
            bindVariables.add(new BoundVariable(propCount, column, type, value));
        }
        sql.append( valuesSQL );
        sql.append( " )");

        sqlLog.debug("buildInsertStatement SQL:{}", sql);

        return Utils.prepareStatement(con, sql.toString(), bindVariables);
    }

    private <T> PreparedStatement buildSelectStatement( T bean, BeanDescriptor descriptor, Connection con) throws SQLException
    {
        String sql;

        ArrayList<BoundVariable> bindVariables = new ArrayList<>();

        if ( descriptor.getTable().toUpperCase().contains("SELECT ") &&
                descriptor.getTable().toUpperCase().contains("FROM "))
        {
            sql = bindStaticSQLStatement(bean, descriptor, bindVariables);
        }
        else
        {

            StringBuilder selectSQL = new StringBuilder( "SELECT ");
            StringBuilder whereSQL = new StringBuilder(" FROM " );
            StringBuilder orderSQL = new StringBuilder();

            whereSQL.append( descriptor.getTable() );

            int colCount = 0;
            int whereCount = 0;

            for (Map.Entry<String,ColumnDefinition> ent: descriptor.getPropertyMap().entrySet())
            {
                String property = ent.getKey();
                String column = ent.getValue().getName();

                log.debug("buildSelectStatement - get property '{}' for column '{}'", property, column);

                PropertyDescriptor pd;
                Object value;
                try
                {
                    pd = BeanUtils.getPropertyDescriptor( bean.getClass(), property);
                    assert pd != null;
                    value = pd.getReadMethod().invoke(bean);
                }
                catch (Exception e)
                {
                    throw new SimpleDAOBaseRuntimeException("Unable to get the property '" + property + "'",e);
                }
                Class<?> type = pd.getPropertyType();

                if ( colCount > 0 )
                {
                    selectSQL.append(", " );
                }
                selectSQL.append( column );
                colCount++;


                if ( ent.getValue().isNullable()  &&  Utils.isPropertyNull(type, value, ent.getValue().getNullValue()))
                {
                    whereSQL.append(whereCount > 0 || whereSQL.toString().contains("WHERE") ? " AND " : " WHERE " )
                            .append(column).append( " IS NULL ");
                }
                else if ( !Utils.isPropertyNull( type, value ) )
                {
                    whereSQL.append(whereCount > 0 || whereSQL.toString().contains("WHERE") ? " AND " : " WHERE " )
                            .append(column).append( value.toString().contains("%") ? " LIKE ? " : " = ? ");
                    whereCount ++;
                    bindVariables.add( new BoundVariable( whereCount, column, type, value));
                }

            }

            Map<Integer, SortedColumn> sorts = descriptor.getOrderedColumns();
            if ( sorts != null && !sorts.isEmpty())
            {
                //todo: this might need to look at the property<->column map to make sure this column is included
                for ( int i = 1; i <= sorts.size(); i ++ )
                {
                    SortedColumn sc = sorts.get(i);
                    if ( i == 1 )
                        orderSQL.append(" ORDER BY ");
                    else
                        orderSQL.append(", ");
                    orderSQL.append( sc.getName() );
                    if ( sc.getSortOrder() == SortOrder.DESCENDING)
                        orderSQL.append(" DESC");
                }
            }
            selectSQL.append ( whereSQL );
            selectSQL.append( orderSQL );
            sql = selectSQL.toString();
        }
        sqlLog.debug("buildSelectStatement SQL:{}", sql);

        return Utils.prepareStatement(con, sql, bindVariables);
    }

    private <T> PreparedStatement buildUpdateStatement( T bean, BeanDescriptor descriptor, Connection con) throws SQLException
    {
        String sql;
        ArrayList<BoundVariable> bindVariables = new ArrayList<>() ;

        if ( descriptor.getTable().toUpperCase().contains("UPDATE "))
        {
            sql = bindStaticSQLStatement(bean, descriptor, bindVariables);
        }
        else
        {
            bindVariables = new ArrayList<>();
            StringBuilder updateSQL = new StringBuilder("UPDATE ");
            StringBuilder whereSQL = new StringBuilder(" WHERE ");
            ArrayList<BoundVariable> keyBindVariables = new ArrayList<>();

            int columnCount = 0;
            int keyCount = 0;

            updateSQL.append(descriptor.getTable());

            updateSQL.append(" SET ");

            String[] keys = descriptor.getUpdateKeys();

            for (String property : descriptor.getPropertyMap().keySet())
            {
                ColumnDefinition def = descriptor.getPropertyMap().get(property);
                String column = def.getName();
                if (column == null || column.isEmpty())
                {
                    column = Utils.getPropertyDBName(property);
                }

                PropertyDescriptor pd;
                Object value;
                try
                {
                    pd = BeanUtils.getPropertyDescriptor( bean.getClass(), property);
                    value = pd.getReadMethod().invoke(bean);
                } catch (Exception e)
                {
                    throw new SimpleDAOBaseRuntimeException("Unable to get the property '" + property + "'", e);
                }

                if (isColumnAKey(keys, column))
                {
                    if (Utils.isPropertyNull(pd.getPropertyType(), value))
                    {
                        throw new SimpleDAOException("Key may not have a null/0 value;");
                    }
                    if (keyCount > 0)
                    {
                        whereSQL.append(" AND ");
                    }
                    whereSQL.append(column);
                    whereSQL.append(" = ?");

                    //todo: this count can go away, just use the size of the key BB list
                    keyCount++;
                    keyBindVariables.add(new BoundVariable(keyCount, column, pd.getPropertyType(), value));
                }
                else
                {
                    Class<?> type = pd.getPropertyType();
                    StringBuilder col = new StringBuilder();
                    if (columnCount > 0)
                    {
                        col.append(", ");
                    }
                    col.append(column);
                    col.append(" = ");

                    if (Utils.isPropertyNull(type, value))
                    {
                        if (def.isNullable())
                        {
                            log.debug("set {} to NULL", column);
                            col.append("?");
                            columnCount++;
                            bindVariables.add(new BoundVariable(columnCount, column, type, null));
                        }
                        else
                        {
                            continue;
                        }
                    }
                    else
                    {
                        col.append("?");
                        columnCount++;
                        bindVariables.add(new BoundVariable(columnCount, column, type, value));
                    }
                    updateSQL.append(col);
                }
            }

            // add the keys to the bind variable list
            for (BoundVariable bv : keyBindVariables)
            {
                bindVariables.add(new BoundVariable(columnCount + bv.getPosition(), bv.getName(), bv.getType(), bv.getValue()));
            }

            updateSQL.append(whereSQL);
            sql = updateSQL.toString();
        }

        sqlLog.debug("buildUpdateStatement SQL:{}", sql);
        return Utils.prepareStatement(con, sql, bindVariables);
    }

    private <T> PreparedStatement buildDeleteStatement( T bean, BeanDescriptor description,Connection con ) throws SQLException
    {
        ArrayList<BoundVariable> bindVariables = new ArrayList<>();
        StringBuilder sql = new StringBuilder( "DELETE FROM ");

        sql.append( description.getTable() );
        sql.append( " WHERE " );

        int colCount = 0;
        for (String property : description.getPropertyMap().keySet())
        {
            String column = description.getPropertyMap().get( property ).getName();

            PropertyDescriptor pd;
            Object value;
            try
            {
                pd = BeanUtils.getPropertyDescriptor( bean.getClass(), property);
                assert pd != null;
                value = pd.getReadMethod().invoke(bean);
            }
            catch (Exception e)
            {
                throw new SimpleDAOBaseRuntimeException("Unable to get the property '" + property + "'",e);
            }
            Class<?> type = pd.getPropertyType();

            if ( !Utils.isPropertyNull( type, value ) )
            {

                if ( colCount > 0 )
                {
                    sql.append(" AND ");
                }
                sql.append( column );
                sql.append( " = ? " );
                colCount ++;
                bindVariables.add( new BoundVariable( colCount, column, type, value ));
            }
        }
        sqlLog.debug("buildDeleteStatement SQL:{}", sql);
        return Utils.prepareStatement(con, sql.toString(), bindVariables);
    }

    protected <T> BeanDescriptor getBeanDescriptor(T bean)
    {
        if ( bean instanceof SimpleBean)
            return ((SimpleBean)bean).describe();
        else
            return ReflectionUtils.describeBean(bean);
    }

    private boolean isColumnAKey( String[] keys, String column)
    {
        for ( String key : keys )
        {
            if ( key.equalsIgnoreCase( column ))
            {
                log.debug("Key column found: {}", column);
                return true;
            }
        }
        return false;
    }

    private <T> String bindStaticSQLStatement(T bean, BeanDescriptor descriptor, ArrayList<BoundVariable>  bindVariables)
    {
        String sql = descriptor.getTable();
        String loopsql = sql;
        int paramCount = 0;
        while (loopsql.contains("@"))
        {
            String param;
            int paramStart = loopsql.indexOf("@") + 1;
            int paramEnd = loopsql.indexOf(" ", paramStart);
            if (paramEnd > paramStart)
            {
                param = loopsql.substring(paramStart, paramEnd);
            }
            else
            {
                param = loopsql.substring(paramStart);
            }
            PropertyDescriptor pd;
            Object value;
            try
            {
                pd = BeanUtils.getPropertyDescriptor( bean.getClass(), param);
                value = pd.getReadMethod().invoke(bean);
            } catch (Exception e)
            {
                log.error("Unable to get bean property named '{}'", param, e);
                throw new SimpleDAOBaseRuntimeException("Unable to get the bean property named '" + param + "'", e);
            }
            paramCount++;
            bindVariables.add(new BoundVariable(paramCount, descriptor.getPropertyMap().get(param).getName(), pd.getPropertyType(), value));
            sql = sql.replace("@" + param, "?");
            loopsql = loopsql.substring(loopsql.indexOf("@" + param) + param.length());
        }
        return sql;
    }
}
