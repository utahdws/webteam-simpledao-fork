package org.simpledao;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.simpledao.annotations.ExcludedProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>SimpleBean is an abstract class intended to be extended by
 * all beans used by the {@link SimpleDAO1 SimpleDAO} framework.  It includes public
 * methods for populating the bean from the database and determining
 * specific database properties at runtime (e.g. database table name)</p>
 * <p/>
 */
public abstract class SimpleBean
{
    private static final Logger log = LoggerFactory.getLogger(SimpleBean.class);

    /**
     * The table used in the statements sent to the database.  uses class name by default.
     * Can be overridden at the bean level.
     */
    protected String dbTableName ;

    protected String[] dbPrimaryKey;

    protected Map<Integer, SortedColumn> dbOrderBy = null;

    protected Map<String, ColumnDefinition> dbColumnMap = null;


    /**
     * get the database table name this bean maps to.  most likely just the bean name
     * @return string database table name
     */
    @ExcludedProperty
    public String getDBTableName()
    {
        if ( dbTableName == null || "".equals( dbTableName ) )
        {
            dbTableName = ReflectionUtils.inferBeanDBTableName(this);
        }
        return  dbTableName;
    }

    public void setDBTableName(String dbTableName)
    {
        this.dbTableName = dbTableName;
    }

    @ExcludedProperty
    public String[] getDBPrimaryKey()
    {
        if ( dbPrimaryKey == null  )
        {
            dbPrimaryKey = ReflectionUtils.inferBeanDBUpdateKeys(this);
        }
        return dbPrimaryKey;
    }

    public void setDBPrimaryKey( String dbPrimaryKey )
    {
        this.dbPrimaryKey = new String[] { dbPrimaryKey };
    }

    public void setDBPrimaryKey( String[] dbPrimaryKey )
    {

        this.dbPrimaryKey = dbPrimaryKey;
    }

    @ExcludedProperty
    public Map<Integer, SortedColumn> getDBOrderBy()
    {
        if ( dbOrderBy == null  )
        {
            dbOrderBy = ReflectionUtils.getBeanDBOrderBy(this);
            return dbOrderBy;
        }
        else
        {
            return dbOrderBy;
        }
    }

    public void setDBOrderBy(Map<Integer, SortedColumn> dbOrderBy)
    {
        this.dbOrderBy = dbOrderBy;
    }

    @ExcludedProperty
    public Map<String, ColumnDefinition> getDBColumnMap()
    {
        if ( dbColumnMap == null )
        {
            dbColumnMap = ReflectionUtils.getBeanPropertyDBColumnMap(this);
            dbColumnMap.remove("DBTableName");
            dbColumnMap.remove("DBPrimaryKey");
            dbColumnMap.remove("DBOrderBy");
            dbColumnMap.remove("DBColumnMap");
        }
        return dbColumnMap;
    }

    public void setDBColumnMap(Map<String, ColumnDefinition> dbColumnMap)
    {
        this.dbColumnMap = dbColumnMap;
    }

    /**
     * Retrieve a map of the available accessors in the bean.
     * @return Map
     */
    public BeanDescriptor describe()
    {
        return new BeanDescriptor(getDBTableName(),getDBPrimaryKey(),getDBOrderBy(),getDBColumnMap());
    }

	public Map<String, Object> describeWithValues()
	{
		Map<String, Object> props = new HashMap<String, Object>();
		PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors( this );
		for (PropertyDescriptor descriptor : descriptors)
		{
			String property = descriptor.getName();
			if (!"class".equals(property) && !property.contains("DB"))
			{
                try
                {
                    props.put(property, PropertyUtils.getProperty( this, property));
                }
                catch (Exception e)
                {
                    log.error("Unable to set property '" + property + "'",e);
                }
            }
		}
		return props;
	}

	/**
     * Set all the properties in the bean based on a map of properties passed in
     * @param  props  HashMap of properties to use when populating
     * @deprecated
     */
    public void populate( HashMap props )
    {
        if ( log.isDebugEnabled() ) { log.debug("populate - begin");}
        ReflectionUtils.populateBean(this, props);
    }

    /**
     * Set null all the public properties in the bean
     */
    public void reset()
    {
        Map props = null;
        try
        {
            props = BeanUtils.describe( this );
        }
        catch (Exception e)
        {
            log.error("Unable to get propes", e);
        }
        if (props != null)
        {
            for (Object o : props.keySet())
            {
                String propName = (String) o;
                try
                {
                    BeanUtils.setProperty(this, propName, null);
                }
                catch (Exception e)
                {
                    log.error("Unable to set prop '" + propName + "'",e);
                }
            }
        }
    }

}
