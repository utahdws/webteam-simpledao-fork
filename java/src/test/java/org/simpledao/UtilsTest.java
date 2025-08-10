package org.simpledao;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class UtilsTest {

    @Test
    void testGetCamelCaseColumnName() {
        assertEquals("firstName", Utils.getCamelCaseColumnName("FIRST_NAME"));
        assertEquals("lastName", Utils.getCamelCaseColumnName("LAST_NAME"));
        assertEquals("id", Utils.getCamelCaseColumnName("ID"));
        assertEquals("addressLine1", Utils.getCamelCaseColumnName("ADDRESS_LINE_1"));
        assertEquals("zipcode", Utils.getCamelCaseColumnName("ZIPCODE"));
    }

    @Test
    void testGetPropertyDBName() {
        assertEquals("FIRST_NAME", Utils.getPropertyDBName("firstName"));
        assertEquals("LAST_NAME", Utils.getPropertyDBName("lastName"));
        assertEquals("ID", Utils.getPropertyDBName("id"));
        assertEquals("ADDRESS_LINE1", Utils.getPropertyDBName("addressLine1"));
        assertEquals("ZIPCODE", Utils.getPropertyDBName("zipcode"));
    }

    @Test
    @SuppressWarnings("deprecation")
    void testGetBeanPropertyMap() {
        TestBean bean = new TestBean();
        Map<String, String> props = Utils.getBeanPropertyMap(bean);

        assertNotNull(props);
        assertEquals(10, props.size());
        assertEquals("ID", props.get("id"));
        assertEquals("FIRST_NAME", props.get("firstName"));
        assertEquals("LAST_NAME", props.get("lastName"));
        assertEquals("CREATED_DATE", props.get("createdDate"));
        assertEquals("ACTIVE", props.get("active"));
        assertEquals("AGE", props.get("age"));
        assertEquals("MIDDLE_INITIAL", props.get("middleInitial"));
        assertEquals("SALARY", props.get("salary"));
        assertEquals("VERSION", props.get("version"));
        assertEquals("SOME_EXCLUDED_PROPERTY", props.get("someExcludedProperty"));
    }

    @Test
    void testIsPropertyNull() {
        assertTrue(Utils.isPropertyNull(String.class, null));
        assertTrue(Utils.isPropertyNull(String.class, ""));
        assertFalse(Utils.isPropertyNull(String.class, "test"));

        assertTrue(Utils.isPropertyNull(Integer.class, -1));
        assertTrue(Utils.isPropertyNull(int.class, -5));
        assertFalse(Utils.isPropertyNull(Integer.class, 0));
        assertFalse(Utils.isPropertyNull(Integer.class, 10));

        assertTrue(Utils.isPropertyNull(Long.class, -1L));
        assertTrue(Utils.isPropertyNull(long.class, -10L));
        assertFalse(Utils.isPropertyNull(Long.class, 0L));

        assertTrue(Utils.isPropertyNull(Double.class, -0.1d));
        assertTrue(Utils.isPropertyNull(double.class, -10.5d));
        assertFalse(Utils.isPropertyNull(Double.class, 0.0d));

        assertTrue(Utils.isPropertyNull(char.class, '\u0000'));
        assertFalse(Utils.isPropertyNull(char.class, 'a'));
    }

    @Test
    void testIsPropertyNullWithNullValue() {
        assertTrue(Utils.isPropertyNull(String.class, "Y", "Y"));
        assertFalse(Utils.isPropertyNull(String.class, "N", "Y"));

        assertTrue(Utils.isPropertyNull(Long.class, 99L, "99"));
        assertFalse(Utils.isPropertyNull(Long.class, 1L, "99"));

        assertTrue(Utils.isPropertyNull(Double.class, 0.0d, "0.0"));
        assertFalse(Utils.isPropertyNull(Double.class, 1.0d, "0.0"));

        assertTrue(Utils.isPropertyNull(Float.class, 0.0f, "0.0"));
        assertFalse(Utils.isPropertyNull(Float.class, 1.0f, "0.0"));

        assertTrue(Utils.isPropertyNull(Object.class, null, null));
    }

}