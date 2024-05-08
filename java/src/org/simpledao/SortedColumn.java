package org.simpledao;

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

    public SortOrder getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

}
