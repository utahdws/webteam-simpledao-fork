package org.simpledao;

import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.simpledao.annotations.Table;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SimpleDAOBaseTest {

    // A sample bean for testing
    @Data
    @Table("TEST_BEAN")
    static class TestBean {
        private Integer id;
        private String name;
    }

    // Real connection for integration testing with H2
    private Connection h2Connection;

    @BeforeEach
    void setUp() throws SQLException {
        // H2 database setup for integration tests
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        try (Statement stmt = h2Connection.createStatement()) {
            // Table name comes from the @Table annotation on the bean
            stmt.execute("CREATE TABLE TEST_BEAN (ID INT PRIMARY KEY, NAME VARCHAR(255))");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Clean up H2 database
        if (h2Connection != null && !h2Connection.isClosed()) {
            try (Statement stmt = h2Connection.createStatement()) {
                stmt.execute("DROP TABLE TEST_BEAN");
            }
            h2Connection.close();
        }
    }

    @Test
    void testSimpleInsertWithH2Database() throws Exception {
        TestBean beanToInsert = new TestBean();
        beanToInsert.setId(101);
        beanToInsert.setName("Integration Test User");

        SimpleDAOBase simpleDaoBase = new SimpleDAOBase();
        simpleDaoBase.simpleInsert(h2Connection, beanToInsert);

        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID, NAME FROM TEST_BEAN WHERE ID = 101")) {

            assertTrue(rs.next(), "A record should have been inserted");
            assertEquals(101, rs.getInt("ID"));
            assertEquals("Integration Test User", rs.getString("NAME"));
            assertFalse(rs.next(), "Only one record should be found");
        }
    }

    @Test
    void testSimpleUpdateWithH2Database() throws Exception {
        // Arrange: Insert an initial record directly into the H2 database
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO TEST_BEAN (ID, NAME) VALUES (102, 'Initial Name')");
        }

        // Create a bean with the updated data
        TestBean beanToUpdate = new TestBean();
        beanToUpdate.setId(102); // This is the key to find the record
        beanToUpdate.setName("Updated Name"); // This is the new value

        // Act: Perform the update
        SimpleDAOBase simpleDaoBase = new SimpleDAOBase();
        simpleDaoBase.simpleUpdate(h2Connection, beanToUpdate);

        // Assert: Verify the data was updated correctly
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID, NAME FROM TEST_BEAN WHERE ID = 102")) {

            assertTrue(rs.next(), "The updated record should be found");
            assertEquals(102, rs.getInt("ID"));
            assertEquals("Updated Name", rs.getString("NAME"), "The name should have been updated");
            assertFalse(rs.next(), "Only one record should be found");
        }
    }

    @Test
    void testSimpleSelectWithH2Database() throws Exception {
        // Arrange: Insert a record to be selected
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO TEST_BEAN (ID, NAME) VALUES (103, 'Select Test Name')");
        }

        // Create a criteria bean with the ID of the record to find
        TestBean criteria = new TestBean();
        criteria.setId(103);

        // Act: Perform the select
        SimpleDAOBase simpleDaoBase = new SimpleDAOBase();
        TestBean resultBean = simpleDaoBase.simpleSelect(h2Connection, criteria);

        // Assert: Verify the correct bean was returned
        assertNotNull(resultBean, "A bean should have been returned from the select");
        assertEquals(103, resultBean.getId());
        assertEquals("Select Test Name", resultBean.getName());
    }

    @Test
    void testSimpleSelectListWithH2Database() throws Exception {
        // Arrange: Insert multiple records, some of which will match the criteria
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO TEST_BEAN (ID, NAME) VALUES (201, 'Multi-Select Test')");
            stmt.executeUpdate("INSERT INTO TEST_BEAN (ID, NAME) VALUES (202, 'John Doe')");
            stmt.executeUpdate("INSERT INTO TEST_BEAN (ID, NAME) VALUES (203, 'Multi-Select Test')");
        }

        // Create a criteria bean to find all records with a specific name
        TestBean criteria = new TestBean();
        criteria.setName("Multi-Select Test");

        // Act: Perform the select list operation
        SimpleDAOBase simpleDaoBase = new SimpleDAOBase();
        List<TestBean> resultList = simpleDaoBase.simpleSelectList(h2Connection, criteria);

        // Assert: Verify the correct list of beans was returned
        assertNotNull(resultList, "A list of beans should have been returned");
        assertEquals(2, resultList.size(), "The list should contain two beans");

        // Verify the contents of the returned beans (ignoring order)
        assertTrue(resultList.stream().anyMatch(b -> b.getId() == 201 && "Multi-Select Test".equals(b.getName())));
        assertTrue(resultList.stream().anyMatch(b -> b.getId() == 203 && "Multi-Select Test".equals(b.getName())));
    }

    @Test
    void testSimpleDeleteWithH2Database() throws Exception {
        // Arrange: Insert records into the database
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO TEST_BEAN (ID, NAME) VALUES (301, 'To Be Deleted')");
            stmt.executeUpdate("INSERT INTO TEST_BEAN (ID, NAME) VALUES (302, 'Should Remain')");
        }

        // Create a criteria bean to specify which record to delete
        TestBean criteria = new TestBean();
        criteria.setId(301);

        // Act: Perform the delete operation
        SimpleDAOBase simpleDaoBase = new SimpleDAOBase();
        simpleDaoBase.simpleDelete(h2Connection, criteria);

        // Assert: Verify the record was deleted and the other remains
        try (Statement stmt = h2Connection.createStatement()) {
            // Check that the record is gone
            ResultSet rsDeleted = stmt.executeQuery("SELECT ID FROM TEST_BEAN WHERE ID = 301");
            assertFalse(rsDeleted.next(), "The record with ID 301 should have been deleted");

            // Check that the other record is still there
            ResultSet rsRemaining = stmt.executeQuery("SELECT ID FROM TEST_BEAN WHERE ID = 302");
            assertTrue(rsRemaining.next(), "The record with ID 302 should not have been deleted");
        }
    }

    @Test
    void testSimpleInsertCollectionImplicitDescriptor() throws Exception {
        TestBean b1 = new TestBean();
        b1.setId(401);
        b1.setName("Batch One");
        TestBean b2 = new TestBean();
        b2.setId(402);
        b2.setName("Batch Two");
        SimpleDAOBase base = new SimpleDAOBase();
        base.simpleInsert(h2Connection, List.of(b1, b2));
        try (Statement stmt = h2Connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS CNT FROM TEST_BEAN WHERE ID IN (401,402)")) {
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("CNT"));
        }
    }

    @Test
    void testSimpleInsertCollectionExplicitDescriptor() throws Exception {
        TestBean b1 = new TestBean();
        b1.setId(501);
        b1.setName("Explicit One");
        TestBean b2 = new TestBean();
        b2.setId(502);
        b2.setName("Explicit Two");
        SimpleDAOBase base = new SimpleDAOBase();
        BeanDescriptor descriptor = base.getBeanDescriptor(b1);
        base.simpleInsert(h2Connection, List.of(b1, b2), descriptor);
        try (Statement stmt = h2Connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS CNT FROM TEST_BEAN WHERE ID IN (501,502)")) {
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("CNT"));
        }
    }

    @Test
    void testSimpleDeleteCollectionImplicitDescriptor() throws Exception {
        TestBean b1 = new TestBean();
        b1.setId(601);
        b1.setName("Del One");
        TestBean b2 = new TestBean();
        b2.setId(602);
        b2.setName("Del Two");

        SimpleDAOBase base = new SimpleDAOBase();
        // insert the beans first
        base.simpleInsert(h2Connection, List.of(b1, b2));
        // now delete the collection using implicit descriptor
        base.simpleDelete(h2Connection, List.of(b1, b2));

        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS CNT FROM TEST_BEAN WHERE ID IN (601,602)")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt("CNT"));
        }
    }

    @Test
    void testSimpleDeleteCollectionExplicitDescriptor() throws Exception {
        TestBean b1 = new TestBean();
        b1.setId(601);
        b1.setName("Del One");
        TestBean b2 = new TestBean();
        b2.setId(602);
        b2.setName("Del Two");

        SimpleDAOBase base = new SimpleDAOBase();
        BeanDescriptor descriptor = base.getBeanDescriptor(b1);
        // insert the beans first
        base.simpleInsert(h2Connection, List.of(b1, b2), descriptor);
        // now delete the collection using implicit descriptor
        base.simpleDelete(h2Connection, List.of(b1, b2), descriptor);

        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS CNT FROM TEST_BEAN WHERE ID IN (601,602)")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt("CNT"));
        }
    }

}