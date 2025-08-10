package org.simpledao;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

class ReflectionUtilsTest {

    @Test
    void testDescribeBean() {
        TestBean bean = new TestBean();
        BeanDescriptor descriptor = ReflectionUtils.describeBean(bean);
        assertThat(descriptor.getPropertyMap().keySet(),
                containsInAnyOrder("id", "firstName", "active", "middleInitial", "lastName",
                        "age", "createdDate", "salary", "version"));
        assertNotNull(descriptor);
        assertEquals("TEST_BEANS", descriptor.getTable());
        assertEquals(1, descriptor.getUpdateKeys().length);
        assertEquals("LAST_NAME", descriptor.getUpdateKeys()[0]);
        assertEquals(9, descriptor.getPropertyMap().size());
        assertEquals(2, descriptor.getOrderedColumns().size());
    }

    @Test
    void testGetBeanPropertyDBColumnMap() {
        TestBean bean = new TestBean();
        Map<String, ColumnDefinition> columnMap = ReflectionUtils.getBeanPropertyDBColumnMap(bean);
        assertNotNull(columnMap);
        assertEquals(9, columnMap.size());
        assertTrue(columnMap.containsKey("id"));
        assertTrue(columnMap.containsKey("firstName"));
        assertTrue(columnMap.containsKey("lastName"));
        assertTrue(columnMap.containsKey("createdDate"));

        assertEquals("LAST_NAME", columnMap.get("lastName").getName());
        assertEquals("FIRST_NAME", columnMap.get("firstName").getName()); // Inferred name
        assertTrue(columnMap.get("lastName").isUpdateKey());
    }

    @Test
    void testInferBeanDBTableName() {
        // Test with @Table annotation
        String tableName = ReflectionUtils.inferBeanDBTableName(new TestBean());
        assertEquals("TEST_BEANS", tableName);

        // Test without @Table annotation (inference from class name)
        String inferredTableName = ReflectionUtils.inferBeanDBTableName(new AnotherBean());
        assertEquals("ANOTHER", inferredTableName);
    }

    @Test
    void testInferBeanDBUpdateKeys() {
        String[] keysForTestBean = ReflectionUtils.inferBeanDBUpdateKeys(new TestBean());
        assertEquals(1, keysForTestBean.length);
        assertEquals("LAST_NAME", keysForTestBean[0]);

        // Test with guessed key from property name
        String[] keysForAnotherBean = ReflectionUtils.inferBeanDBUpdateKeys(new AnotherBean());
        assertEquals(0, keysForAnotherBean.length);
    }

    @Test
    void testGetBeanDBOrderBy() {
        TestBean bean = new TestBean();
        Map<Integer, SortedColumn> sortedColumns = ReflectionUtils.getBeanDBOrderBy(bean);

        assertNotNull(sortedColumns);
        assertEquals(2, sortedColumns.size());
        assertThat(sortedColumns.keySet(), containsInAnyOrder(1, 2));

        SortedColumn sortedColumn = sortedColumns.get(1);
        assertEquals("LAST_NAME", sortedColumn.getName());
        assertEquals(SortOrder.DESCENDING, sortedColumn.getSortOrder());
    }

    @Test
    void testPopulateBean() {
        TestBean bean = new TestBean();
        Map<String, Object> props = new HashMap<>();
        props.put("firstName", "John");
        props.put("lastName", "Doe");
        props.put("id", 123);

        ReflectionUtils.populateBean(bean, props);

        assertEquals("John", bean.getFirstName());
        assertEquals("Doe", bean.getLastName());
        assertEquals(123, bean.getId());
        assertNull(bean.getCreatedDate()); // Not in map, should be null
    }

    @Test
    void testPopulateBeanWithAllTypesFromString() {
        TestBean bean = new TestBean();
        Map<String, Object> props = new HashMap<>();
        props.put("firstName", "Jane");
        props.put("middleInitial", "X");
        props.put("active", "true");
        props.put("age", "30");
        props.put("salary", "12345.67");
        props.put("version", "12345");
        props.put("middleInitial", "X");
        props.put("createdDate", "2024-07-31");

        ReflectionUtils.populateBean(bean, props);

        assertEquals("Jane", bean.getFirstName());
        assertEquals(Integer.valueOf(30), bean.getAge());
        assertEquals(new BigDecimal("12345.67"), bean.getSalary());
        assertEquals(Short.valueOf("12345"), bean.getVersion());
        assertEquals(Character.valueOf('X'), bean.getMiddleInitial());
        assertEquals("2024-07-31", bean.getCreatedDate());
    }

    @Test
    void testPopulateBeanWithNativeTypes() {
        TestBean bean = new TestBean();
        Map<String, Object> props = new HashMap<>();
        props.put("id", 456);
        props.put("active", false);
        props.put("age", 42);
        props.put("salary", new BigDecimal("9876.54"));
        props.put("version", (short) 2);
        props.put("middleInitial", 'Z');

        ReflectionUtils.populateBean(bean, props);

        assertEquals(456, bean.getId());
        assertFalse(bean.getActive());
        assertEquals(Integer.valueOf(42), bean.getAge());
        assertEquals(new BigDecimal("9876.54"), bean.getSalary());
        assertEquals(Short.valueOf((short) 2), bean.getVersion());
        assertEquals(Character.valueOf('Z'), bean.getMiddleInitial());
    }

    @Test
    void testPopulateBeanWithNullAndInvalidProperties() {
        TestBean bean = new TestBean();
        bean.setFirstName("Initial");
        Map<String, Object> props = new HashMap<>();
        props.put("firstName", null); // Should be skipped
        props.put(null, "someValue"); // Should be skipped
        props.put("nonExistentProperty", "should be ignored");

        System.out.println("We will expect an error to be printed to the console below, this is expected behavior.");
        ReflectionUtils.populateBean(bean, props);
        System.out.println("We will expect an error to be printed to the console above, this is expected behavior.");

        // The initial value should not be overwritten by null
        assertEquals("Initial", bean.getFirstName());
    }
}