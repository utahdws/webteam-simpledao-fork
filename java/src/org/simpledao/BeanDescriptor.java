package org.simpledao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BeanDescriptor
{
    private String table;
    private String[] updateKeys;
    private Map<Integer, SortedColumn> orderedColumns;
    private Map<String,ColumnDefinition> propertyMap;
}

