// TestBean.java - A sample bean for testing
package org.simpledao;

import lombok.Data;
import org.simpledao.annotations.*;

@Data
@Table("TEST_BEANS")
public class TestBean {

    private int id;

    private String firstName;

    private String lastName;

    private String createdDate;

    private String someExcludedProperty;

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