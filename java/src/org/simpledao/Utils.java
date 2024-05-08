package org.simpledao;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.simpledao.annotations.Column;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@NoArgsConstructor
public class Utils
{
    /**
     *
     * @param bean to use
     * @return a map of properties
     * @deprecated Use ReflectionUtils.getBeanPropertyMap instead
     */
    public static Map<String,String> getBeanPropertyMap(Object bean)
    {
        Map<String,String> props = new HashMap<String,String>();
        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors( bean );
        for (PropertyDescriptor descriptor : descriptors)
        {
            String property = descriptor.getName();
            Column ca = descriptor.getReadMethod().getAnnotation(Column.class);
            if ( ca != null && !"".equals(ca.value()))
            {
                props.put(property, ca.value());
            }
            else
            {
                if (!"class".equals(property) && !property.contains("DB"))
                {
                    props.put(property, Utils.getPropertyDBName(property));
                }
            }
        }

        return props;
    }



    /**
     * return the camel case property name from a database column name
     * e.g. first_name == firstName.
     * @param columnName the database column name
     * @return a string representing the property name
     */
    public static String getCamelCaseColumnName( String columnName )
    {
        StringBuilder camelCaseColumnName = new StringBuilder();

        if ( columnName.indexOf( "_" ) > 0 )
        {
            columnName = columnName.toLowerCase();
            String[] parts = columnName.split( "_"  );

            for ( int i = 0; i < parts.length ; i++ )
            {
                if ( i == 0 )
                {
                    camelCaseColumnName.append( parts[i].toLowerCase() );
                }
                else
                {
                    camelCaseColumnName.append( parts[i].substring( 0, 1 ).toUpperCase() );
                    camelCaseColumnName.append( parts[i].substring( 1 ).toLowerCase() );
                }
            }
        }
        else
        {
            camelCaseColumnName.append( columnName.toLowerCase() ) ;
        }

        return camelCaseColumnName.toString();
    }

    /**
     * get the database column name from a camel case property name
     * @param property the bean property
     * @return the column name
     */
    public static String getPropertyDBName( String property )
    {
        StringBuilder dbColumnName = new StringBuilder();
        char[] chars = property.toCharArray();
        for ( int i = 0; i < property.length(); i++ )
        {
            if ( Character.isUpperCase( chars[i] ) && i > 1 )
            {
                dbColumnName.append( "_" );
                dbColumnName.append( chars[i] );
            }
            else
            {
                dbColumnName.append( chars[i] );
            }
        }
        return dbColumnName.toString().toUpperCase();
    }

    public static boolean isPropertyNull( Class type, Object value )
    {
        if ( value == null )
            return true;

        if  ( ( type == Integer.class || "int".equals( type.getName() ) ) && ((Integer) value < 0) )
            return true;

        if  ( ( type == Long.class || "long".equals( type.getName() ) ) && ((Long) value < 0) )
            return true;

        if ( type == String.class && "".equals( value ) )
            return true;

		if ( ( type == Double.class || "double".equals( type.getName() ) ) && ((Double) value < 0.0d))
			return true;

		if (  "char".equals( type.getName() ) )
        {
            try
            {
                Character x = (Character) value;
                if (x == '\u0000' ) return true;
            }
            catch (ClassCastException e )
            {
                return true;
            }
        }

        return false;
    }

    public static boolean isPropertyNull( Class type, Object value, String nullValue )
    {
        // handle just nulls
        if (value == null && nullValue == null )
        {
            return true;
        }

        try
        {
            if  ( ( type == Long.class || "long".equals( type.getName() ) ) &&
                    (Long)value == Long.parseLong(nullValue) )
            {
                return true;
            }

            if ((type == Double.class || "double".equals(type.getName()))) {
                assert value instanceof Double;
                if ((Double)value == Double.parseDouble(nullValue)) {
                    return true;
                }
            }

            if ((type == Float.class || "float".equals(type.getName()))) {
                assert value instanceof Float;
                if ((Float)value == Float.parseFloat(nullValue )) {
                    return true;
                }
            }

            if ( value instanceof java.util.Date )
            {
                SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
                sdf.setLenient(true);
                return (sdf.parse(nullValue ).equals(value));

            }

            if ( type == String.class && nullValue.equals( value ) )
                return true;

        }

        catch (Exception ex)
        {
            log.warn("Unable to test for null type '{}' against '{}' because of - {}", type.getName(), nullValue, ex.getMessage());
        }
        return false;
    }

    public static boolean isNullablePropertyValid(Class propertyType, String nullValue)
    {
        return true;
    }

    public static void bindStatementVariable ( PreparedStatement stmt, BoundVariable bv) throws SQLException
    {
        if (bv.getType() == String.class)
        {
            if (bv.getName().toLowerCase().indexOf("_date") > 0)
            {
                if ("".equals(bv.getValue().toString()))
                {
                    log.debug("bindStatementVariable - var '{}' is null date (String)", bv.getName());
                    stmt.setNull(bv.getPosition(), Types.DATE);
                }
                else
                {
                    log.debug("bindStatementVariable - var is date (String)");

                    String format = "M/d/yyyy";
                    if ( bv.getName().toLowerCase().indexOf("time") > 0 )
                        format = "M/d/yyyy HH:mm";

                    SimpleDateFormat sdf = new SimpleDateFormat(format);
                    sdf.setLenient(false);
                    try
                    {
                        java.util.Date dt = sdf.parse(bv.getValue().toString());
                        Date newDate = new Date(dt.getTime());
                        stmt.setDate(bv.getPosition(), newDate);
                    }
                    catch (ParseException e)
                    {
                        throw new RuntimeException("unable to parse date: " + bv.getValue(),e);
                    }
                }
            }
            else
            {
                if ("".equals(bv.getValue().toString()))
                {
                    log.debug("bindStatementVariable - var '{}' is null String", bv.getName());
                    stmt.setNull(bv.getPosition(), Types.VARCHAR);
                }
                else
                {
                    log.debug("bindStatementVariable - var '{}' is String", bv.getName());
                    stmt.setString(bv.getPosition(), bv.getValue().toString());
                }
            }
        }
        else if (bv.getType() == Integer.class || "int".equals(bv.getType().getName()))
        {
            log.debug("bindStatementVariable - var '{}' is int", bv.getName());
            stmt.setInt(bv.getPosition(), Integer.parseInt(bv.getValue().toString(), 10));
        }
        else if (bv.getType() == Long.class || "long".equals(bv.getType().getName()))
        {
            log.debug("bindStatementVariable - var '{}' is long", bv.getName());
            stmt.setLong(bv.getPosition(), Long.parseLong(bv.getValue().toString(), 10));
        }
        else if (bv.getType() == Double.class || "double".equals(bv.getType().getName()))
        {
            log.debug("bindStatementVariable - var '{}' is double", bv.getName());
            stmt.setDouble(bv.getPosition(), Double.parseDouble(bv.getValue().toString()));
        }
        else if (bv.getType() == Float.class ||  "float".equals(bv.getType().getName()))
        {
            log.debug("bindStatementVariable - var '{}' is float", bv.getName());
            stmt.setFloat(bv.getPosition(), Float.parseFloat(bv.getValue().toString()));
        }
        else if ("char".equals(bv.getType().getName()))
        {
            log.debug("bindStatementVariable - var '{}' is char", bv.getName());
            stmt.setString(bv.getPosition(), bv.getValue().toString());
        }
        else if ( bv.getValue() instanceof java.util.Date )
        {
            log.debug("bindStatementVariable - var '{}' is Date", bv.getName());
            stmt.setTimestamp(bv.getPosition(), new Timestamp(((java.util.Date)bv.getValue()).getTime()));

        }
        else
        {
            log.debug("bindStatementVariable - var '{}' is not mapped", bv.getName());}
            stmt.setString(bv.getPosition(), bv.getValue().toString());
        }


    public static void bindVariables( PreparedStatement stmt,
            ArrayList<BoundVariable> boundVariables ) throws SQLException
    {
        log.debug("bindVariables - begin");

        for (BoundVariable bv : boundVariables)
        {

            // this takes care of the WHERE clause of an update
            if (bv.getPosition() == 0)
            {
				log.debug("bindVariables - found the key");
                bv.setPosition(boundVariables.size());
            }

            if (bv.getType() == String.class)
            {
                if (bv.getName().toLowerCase().indexOf("_date") > 0)
                {
                    if ("".equals(bv.getValue().toString()))
                    {

                        log.debug("bindVariables - var '{}' is null date (String)", bv.getName());

                        stmt.setNull(bv.getPosition(), Types.DATE);
                    }
                    else
                    {
                        log.debug("bindVariables - var is date (String)");

                        String format = "M/d/yyyy";
                        if ( bv.getName().toLowerCase().indexOf("time") > 0 )
                            format = "M/d/yyyy HH:mm";

                        SimpleDateFormat sdf = new SimpleDateFormat(format);
                        sdf.setLenient(false);
                        try
                        {
                            java.util.Date dt = sdf.parse(bv.getValue().toString());
                            Date newDate = new Date(dt.getTime());
                            stmt.setDate(bv.getPosition(), newDate);
                        }
                        catch (ParseException e)
                        {
                            throw new RuntimeException("unable to parse date: " + bv.getValue(),e);
                        }
                    }
                }
                else
                {
                    if ("".equals(bv.getValue().toString()))
                    {
						log.debug("bindVariables - var '" + bv.getName() + "' is null String");
                        stmt.setNull(bv.getPosition(), Types.VARCHAR);
                    }
                    else
                    {
						log.debug("bindVariables - var '" + bv.getName() + "' is String");
                        stmt.setString(bv.getPosition(), bv.getValue().toString());
                    }
                }
            }
            else if (bv.getType() == Integer.class || "int".equals(bv.getType().getName()))
            {
				log.debug("bindVariables - var '" + bv.getName() + "' is int");
                stmt.setInt(bv.getPosition(), Integer.parseInt(bv.getValue().toString(), 10));
            }
			else if (bv.getType() == Long.class || "long".equals(bv.getType().getName()))
			{
				log.debug("bindVariables - var '" + bv.getName() + "' is long");
				stmt.setLong(bv.getPosition(), Long.parseLong(bv.getValue().toString(), 10));
			}
			else if (bv.getType() == Double.class || "double".equals(bv.getType().getName()))
			{
				log.debug("bindVariables - var '" + bv.getName() + "' is double");
				stmt.setDouble(bv.getPosition(), Double.parseDouble(bv.getValue().toString()));
			}
			else if (bv.getType() == Float.class ||  "float".equals(bv.getType().getName()))
			{
				log.debug("bindVariables - var '" + bv.getName() + "' is float");
				stmt.setFloat(bv.getPosition(), Float.parseFloat(bv.getValue().toString()));
			}
            else if ("char".equals(bv.getType().getName()))
            {
				log.debug("bindVariables - var '" + bv.getName() + "' is char");
                stmt.setString(bv.getPosition(), bv.getValue().toString());
            }
			else if ( bv.getValue() instanceof java.util.Date )
            {
				log.debug("bindVariables - var '" + bv.getName() + "' is Date");
                stmt.setTimestamp(bv.getPosition(), new Timestamp(((java.util.Date)bv.getValue()).getTime()));
            }
            else
            {
                log.debug("bindStatementVariable - var '" + bv.getName() + "' is not mapped");
                stmt.setString(bv.getPosition(), bv.getValue().toString());
            }
        }
    }

    public static PreparedStatement prepareStatement(Connection con, String sql, ArrayList<BoundVariable> boundVariables)
        throws SQLException
    {
        log.debug("bindVariables - begin");

        PreparedStatement statement = con.prepareStatement(sql);
        for (BoundVariable bv : boundVariables)
        {
            // this takes care of the WHERE clause of an update
            if (bv.getPosition() == 0)
            {
				log.debug("bindVariables - found the key");
                bv.setPosition(boundVariables.size());
            }

            if ( bv.getValue() == null ) {
                statement.setNull( bv.getPosition(), Types.NULL ); // changed from Types.VARCHAR - 20150310
                continue;
            }

            if (bv.getType() == String.class)
            {
                if (bv.getName().toLowerCase().indexOf("_date") > 0)
                {
                    if ("".equals(bv.getValue().toString()))
                    {
						log.debug("bindVariables - var '{}' is null date (String)", bv.getName());
                        statement.setNull(bv.getPosition(), Types.DATE);
                    }
                    else
                    {
						log.debug("bindVariables - var is date (String)");

                        String format = "M/d/yyyy";
                        if ( bv.getName().toLowerCase().indexOf("time") > 0 )
                            format = "M/d/yyyy HH:mm";

                        SimpleDateFormat sdf = new SimpleDateFormat(format);
                        sdf.setLenient(false);
                        try
                        {
                            java.util.Date dt = sdf.parse(bv.getValue().toString());
                            Date newDate = new Date(dt.getTime());
                            statement.setDate(bv.getPosition(), newDate);
                        }
                        catch (ParseException e)
                        {
                            throw new RuntimeException("unable to parse date: " + bv.getValue(),e);
                        }
                    }
                }
                else
                {
                    if ("".equals(bv.getValue().toString()))
                    {
						log.debug("bindVariables - var '{}' is null String", bv.getName());
                        statement.setNull(bv.getPosition(), Types.VARCHAR);
                    }
                    else
                    {
						log.debug("bindVariables - var '{}' is String", bv.getName());
                        statement.setString(bv.getPosition(), bv.getValue().toString());
                    }
                }
            }
            else if (bv.getType() == Integer.class || "int".equals(bv.getType().getName()))
            {
				log.debug("bindVariables - var '{}' is int", bv.getName());
                statement.setInt(bv.getPosition(), Integer.parseInt(bv.getValue().toString(), 10));
            }
			else if (bv.getType() == Long.class || "long".equals(bv.getType().getName()))
			{
                log.debug("bindVariables - var '{}' is long", bv.getName());
				statement.setLong(bv.getPosition(), Long.parseLong(bv.getValue().toString(), 10));
			}
			else if (bv.getType() == Double.class || "double".equals(bv.getType().getName()))
			{
                log.debug("bindVariables - var '{}' is double", bv.getName());
				statement.setDouble(bv.getPosition(), Double.parseDouble(bv.getValue().toString()));
			}
			else if (bv.getType() == Float.class ||  "float".equals(bv.getType().getName()))
			{

				statement.setFloat(bv.getPosition(), Float.parseFloat(bv.getValue().toString()));
			}
            else if ( bv.getType() == byte[].class) // BLOB
            {
                log.debug("bindVariables - var '{}' is a byte array - assume blob",  bv.getName());
                statement.setBlob(bv.getPosition(), new ByteArrayInputStream((byte[])bv.getValue()));
            }
            else if ("char".equals(bv.getType().getName()))
            {
				log.debug("bindVariables - var '{}' is char", bv.getName());
                statement.setString(bv.getPosition(), bv.getValue().toString());
            }
			else if ( bv.getValue() instanceof java.util.Date )
            {
				log.debug("bindVariables - var '{}' is Date", bv.getName() );
                statement.setTimestamp(bv.getPosition(), new Timestamp(((java.util.Date)bv.getValue()).getTime()));
            }
            else
            {
                log.debug("bindStatementVariable - var '{}' is not mapped", bv.getName());
                statement.setString(bv.getPosition(), bv.getValue().toString());
            }

        }
        log.debug("bindVariables - end");
        return statement;
    }

    /**
     * returns a map of bean property names based on the columns found in the resultset
     * @param metaData resultsetmetadata
     * @param rs resultset
     * @return hashmap
     * @throws Exception that you should catch
     */
    public static Map getPropsFromColumns( ResultSetMetaData metaData, ResultSet rs ) throws SQLException
    {
        HashMap<String,String> props = new HashMap<String,String>();
        int columnCount = metaData.getColumnCount();
        for ( int i = 1; i <= columnCount; i++ )
        {
            props.put ( Utils.getCamelCaseColumnName( metaData.getColumnName( i ) ), rs.getString( i ) );
        }
        return props;
    }

    public static Map<String,String> getColumnMapFromProps( Map<String,String> properties )
    {
        Map<String,String> columns = new HashMap<String,String>();
        for (String property : properties.keySet())
        {
            String column = properties.get(property);
            if (column == null || column.isEmpty())
            {
                column = getPropertyDBName(property);
            }
            columns.put(column, property);
        }
        return columns;
    }

    public static Map<String,String> getColumnPropertyMap( Map<String,ColumnDefinition> props )
    {
        Map<String,String> columns = new HashMap<String,String>();
        for (String property : props.keySet())
        {
            columns.put( props.get(property).getName().toUpperCase(), property);
        }
        return columns;
    }

    public static void closeConnection( Connection con)
    {
        try
        {
            if (con != null )
            {
                con.close();
            }
        }
        catch (SQLException ex)
        {
            // no need to catch
        }
    }
}

