// TestBean.java - A sample bean for testing
package org.simpledao;

import lombok.Data;
import org.simpledao.annotations.*;

import java.math.BigDecimal;

@Data
@Table("TEST_BEANS")
public class TestBean {

    private Integer id;

    private String firstName;

    private Boolean active;

    private Character middleInitial;

    private String lastName;

    private Integer age;

    private String createdDate;

    private String someExcludedProperty;

    private BigDecimal salary;

    private Short version;

    @Column(value = "CREATED_DATE")
    public String getCreatedDate() {
        return createdDate;
    }

    @Column(value = "FIRST_NAME")
    @OrderedColumn(sortOrder = SortOrder.ASCENDING, orderPosition = 2)
    public String getFirstName() {
        return firstName;
    }

    @UpdateKeyColumn
    @OrderedColumn(sortOrder = SortOrder.DESCENDING, orderPosition = 1)
    public String getLastName() {
        return lastName;
    }

    @ExcludedProperty
    public String getSomeExcludedProperty() {
        return someExcludedProperty;
    }

}