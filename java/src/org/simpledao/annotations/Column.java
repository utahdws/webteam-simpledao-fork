package org.simpledao.annotations;

import org.simpledao.SortOrder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Column
{
    String value() default "";
    boolean nullable() default false;
    String nullValue() default "";
    String defaultValue() default "-1";
    boolean updateKey() default false;
    SortOrder sortOrder() default SortOrder.UNDEFINED;
    int orderByPosition() default 1;
}
