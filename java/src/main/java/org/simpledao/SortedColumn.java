package org.simpledao;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SortedColumn
{
    private SortOrder sortOrder = SortOrder.ASCENDING;
    private String name;

    public SortedColumn() {}
    
    public SortedColumn(String name, SortOrder order)
    {
        sortOrder = order;
        this.name = name;
    }

}
