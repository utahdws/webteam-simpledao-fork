package org.simpledao;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ColumnDefinition
{
    private String name;
    private boolean updateKey;
    private boolean nullable;
    private String nullValue;
    private SortOrder sortOrder;
    private int orderByPosition;
    private Object value;

    public ColumnDefinition(String name)
    {
        this.name = name;
    }

}
