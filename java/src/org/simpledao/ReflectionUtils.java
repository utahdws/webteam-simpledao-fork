package org.simpledao;

import lombok.extern.slf4j.Slf4j;
import org.simpledao.annotations.*;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class ReflectionUtils
{

    public static BeanDescriptor describeBean( Object bean)
    {
        BeanDescriptor descriptor = null;
        if ( bean instanceof SimpleBean)
            descriptor = ((SimpleBean)bean).describe();
        else
        {
            //PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(bean.getClass());
            //PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors( bean );
            /*for (PropertyDescriptor prop : descriptors)
            {
                if ( prop.getPropertyType() == BeanDescriptor.class )
                {
                    try
                    {
                        //return BeanUtils.getPropertyDescriptor(bean.getClass(), prop.getName());
                        //return (BeanDescriptor) PropertyUtils.getProperty(bean,prop.getName());
                    }
                    catch (Exception e)
                    {
                        log.error("Unable to get property '{}'", prop.getName(), e);
                    }
                }
            }*/
            descriptor = new BeanDescriptor();
            descriptor.setTable( inferBeanDBTableName(bean));
            descriptor.setUpdateKeys( inferBeanDBUpdateKeys( bean));
            descriptor.setPropertyMap( getBeanPropertyDBColumnMap(bean));
            descriptor.setOrderedColumns( getBeanDBOrderBy(bean));
        }
        return descriptor;
    }

    public static Map<String,ColumnDefinition> getBeanPropertyDBColumnMap(Object bean)
    {
        Map<String,ColumnDefinition> props = new HashMap<String,ColumnDefinition>();
        //PropertyDescriptor descriptors[] = PropertyUtils.getPropertyDescriptors( bean );
        PropertyDescriptor descriptors[] = BeanUtils.getPropertyDescriptors( bean.getClass() );
        for (PropertyDescriptor descriptor : descriptors)
        {
            String property = descriptor.getName();
            if (!"class".equals(property) && descriptor.getReadMethod().getAnnotation(ExcludedProperty.class) == null)
            {
                ColumnDefinition column = new ColumnDefinition(Utils.getPropertyDBName(property));

                Column ca = descriptor.getReadMethod().getAnnotation(Column.class);
                NullableProperty np = descriptor.getReadMethod().getAnnotation(NullableProperty.class);
                OrderedColumn oc = descriptor.getReadMethod().getAnnotation(OrderedColumn.class);
                UpdateKeyColumn ukc = descriptor.getReadMethod().getAnnotation(UpdateKeyColumn.class);


                if ( ca != null )
                {
                    if (!"".equals(ca.value()))
                        column.setName( ca.value());
                    column.setNullable( ca.nullable());
                    column.setNullValue( ca.nullValue());
                    column.setUpdateKey( ca.updateKey());

                    if ( ca.sortOrder() != SortOrder.UNDEFINED)
                    {
                        column.setSortOrder( ca.sortOrder());
                        column.setOrderByPosition( ca.orderByPosition());
                    }
                }
                if (ukc != null )
                    column.setUpdateKey(true);
                if ( np != null )
                {
                    column.setNullable( np.value());
                    column.setNullValue(np.nullValue());
                }
                if ( oc != null )
                {
                    column.setSortOrder( oc.sortOrder());
                    column.setOrderByPosition( oc.orderPosition());
                }
                props.put(property,column);
            }
        }

        return props;
    }

    /**
     * Tries to determine the database table name via two methods:
     * 1. A @Table type annotation on the class (e.g., @Table("LIBRARY_BOOKS")
     *  *Note this annotation is marked inheritable so will be picked up
     *  by subclasses
     * 2. Converting the Class name to a table name via camel case rules
     *  (e.g., a class named LibraryBooks will become LIRARY_BOOKS)
     * **Inner classes that don't have an annotation in the class heirarchy
     *   will be queried for their superclass and this method will recurse 
     * @param bean - the object to inspect
     * @return a database table name
     */
    public static String inferBeanDBTableName( Object bean )
    {
        Table ta = bean.getClass().getAnnotation(Table.class);
        if (ta != null && !"".equals(ta.value()))
        {
            return ta.value();
        }
        else if ( bean.getClass().getName().contains("$"))
        {
            // most likely an anonymous inner class, behave appropriately
            try
            {
                return inferBeanDBTableName( bean.getClass().getSuperclass().newInstance() );
            }
            catch (Exception e)
            {
                log.error("infer table name - unable to instantiate super class. {}", e.getMessage(), e);
                throw new RuntimeException("infer table name - unable to instantiate super class of inner class",e);
            }
        }
        else
        {
            return Utils.getPropertyDBName( bean.getClass().getName().replaceAll("\\w+\\.","").replaceAll("Bean",""));
        }
    }

    public static String[] inferBeanDBUpdateKeys( Object bean )
    {
        List<String> keys = new ArrayList<String>();
        String guessedKey = null;
        //PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors( bean );
        PropertyDescriptor descriptors[] = BeanUtils.getPropertyDescriptors( bean.getClass() );
        for (PropertyDescriptor descriptor : descriptors)
        {
            String property = descriptor.getName();
            Column ca = descriptor.getReadMethod().getAnnotation(Column.class);
            UpdateKeyColumn ua = descriptor.getReadMethod().getAnnotation(UpdateKeyColumn.class);
            if ( (ca != null  && ca.updateKey()) || ua != null)
            {
                String columnName = Utils.getPropertyDBName(property); 
                if ( ca != null && !"".equals(ca.value()))
                {
                    columnName = ca.value();
                }
                keys.add(columnName);
            }
            else
            {
                if  ("id".equals(property) || property.toUpperCase().equals(bean.getClass().getName().replaceAll("\\w+\\.","").replaceAll("Bean","").toUpperCase() +  "ID"))
                {
                    guessedKey = Utils.getPropertyDBName(property);
                }
            }
        }
        if (keys.isEmpty() && guessedKey != null && !guessedKey.isEmpty())
        {
            keys.add(guessedKey);
        }
        return keys.toArray(new String[keys.size()]);
    }

    public static Map<Integer, SortedColumn> getBeanDBOrderBy( Object bean )
    {
        Map<Integer, SortedColumn> sorts = new HashMap<Integer,SortedColumn>();
        //PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors( bean );
        PropertyDescriptor descriptors[] = BeanUtils.getPropertyDescriptors( bean.getClass() );
        for (PropertyDescriptor descriptor : descriptors)
        {
            String property = descriptor.getName();
            Column ca = descriptor.getReadMethod().getAnnotation(Column.class);
            OrderedColumn oca = descriptor.getReadMethod().getAnnotation(OrderedColumn.class);
            if ( (ca != null && ca.sortOrder() != SortOrder.UNDEFINED) || (oca!= null && oca.sortOrder() != SortOrder.UNDEFINED))
            {
                SortedColumn column = new SortedColumn();
                column.setName(Utils.getPropertyDBName(property));
                int position = 1;

                if ( ca != null )
                {
                    if ( !"".equals(ca.value())) column.setName( ca.value());
                    if ( ca.sortOrder() != SortOrder.UNDEFINED ) column.setSortOrder( ca.sortOrder());
                    position =  ca.orderByPosition();
                }

                if ( oca != null && oca.sortOrder() != SortOrder.UNDEFINED)
                {
                    column.setSortOrder(oca.sortOrder());
                    position =oca.orderPosition();
                }

                if ( position == 0 || sorts.containsKey(position) )
                    throw new RuntimeException("Order Position must be > 0 and unique");

                sorts.put(position, column);
            }
        }
        return sorts;
    }

    /**
     * Set all the properties in the bean based on a map of properties passed in
     * @param bean the bean to reflect upon
     * @param  props  HashMap of properties to use when populating
     */
    public static void populateBean( Object bean, HashMap props )
    {
        log.debug("populate - begin");

        for (Object o : props.keySet())
        {
            String propName = (String) o;
            log.debug("populate - property '{}'", propName);

            if (propName == null)
            {
                log.debug("somehow we managed to come to a null property name, weird");
                continue;
            }

            Object value = props.get(propName);
            if ( value == null )
            {
                log.debug("populate - property '{}' - null property value not set", propName);
                continue;
            }
            try
            {
                //PropertyDescriptor descriptor = PropertyUtils.getPropertyDescriptor(bean, propName);
                PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor( bean.getClass(), propName);
                if(value instanceof String){
                    if(descriptor.getPropertyType().equals(String.class)){
                        descriptor.getWriteMethod().invoke(bean, value.toString());
                    }else if(descriptor.getPropertyType().equals(Integer.class)){
                        descriptor.getWriteMethod().invoke(bean, Integer.parseInt(value.toString()));
                    }else if(descriptor.getPropertyType().equals(BigDecimal.class)){
                        descriptor.getWriteMethod().invoke(bean, new BigDecimal(value.toString()));
                    }else if(value instanceof Long){
                        descriptor.getWriteMethod().invoke(bean, Long.parseLong(value.toString()));
                    } else if (propName.matches(".*[dD]ate$")){
                        log.debug("populate - property '{}' is a string and has date in the name, format it", propName);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
                        Date dt = sdf.parse(value.toString());
                        sdf.applyPattern("MM/dd/yyyy");
                        descriptor.getWriteMethod().invoke(bean, sdf.format(dt));
                    }
                }else{
                    descriptor.getWriteMethod().invoke(bean, value);
                }

                /*if ( value instanceof java.sql.Timestamp || value instanceof java.sql.Date || value instanceof java.sql.Time)
                {
                    log.debug("populate - set the date property '{}'", propName);

                    if (descriptor.getPropertyType().equals(java.util.Date.class) )
                    {
                        log.debug("populate - property '{}' expects date", propName);
                        LocaleBeanUtils.setProperty(bean, propName, value);
                    }
                    else if ( descriptor.getPropertyType().equals(java.lang.String.class))
                    {
                        log.debug("populate - property '{}' expects string", propName);
                        BeanUtils.setProperty(bean, propName, value);
                    }

                }
                else
                {
                    log.debug("populate - set the property '{}'", propName);
                    BeanUtils.setProperty(bean, propName, value);
                }*/
            }
            catch (Exception e)
            {
                log.error("populate - unable to set the property. {}", e.getMessage(), e);
                // do nothing, property not found or set
            }
        }
    }


}
