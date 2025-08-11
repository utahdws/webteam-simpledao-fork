package org.simpledao;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class BoundVariable
{
    private int position;
    private String name;
    private Class type;
    private Object value;

    public BoundVariable(int position, String name, Class type, Object value)
    {
        this.position = position;
        this.name = name;
        this.type = type;
        this.value = value;
    }

}
