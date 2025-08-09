package org.simpledao;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SimpleDAOConfig
{
    private String beanNameSuffix = "";
    private int nullIntValue = -1;
    private String defaultDatabasePropFile = "database.properties";
}
