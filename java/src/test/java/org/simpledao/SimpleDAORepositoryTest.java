package org.simpledao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class SimpleDAORepositoryTest {

    @Autowired
    private SimpleDAORepository repository;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws Exception {
        // Create the schema for each test
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS TEST_BEANS;");
            stmt.execute("CREATE TABLE TEST_BEANS (ID INT PRIMARY KEY, FIRST_NAME VARCHAR(255), LAST_NAME VARCHAR(255), CREATED_DATE VARCHAR(255), SOME_INCLUDED_PROPERTY VARCHAR(255));");
        }
    }

    @Test
    void testSimpleInsertAndSelect() throws Exception {
        // Create and insert a bean
        TestBean beanToInsert = new TestBean();
        beanToInsert.setId(1);
        beanToInsert.setFirstName("John");
        beanToInsert.setLastName("Doe");
        repository.simpleInsert(beanToInsert);

        // Create a criteria bean to select the inserted record
        TestBean criteria = new TestBean();
        criteria.setId(1);

        TestBean selectedBean = repository.simpleSelect(criteria);

        // Assertions
        assertThat(selectedBean, is(notNullValue()));
        assertThat(selectedBean.getFirstName(), is("John"));
        assertThat(selectedBean.getLastName(), is("Doe"));
    }

    @Test
    void testSimpleUpdate() throws Exception {
        // Insert initial record
        TestBean bean = new TestBean();
        bean.setId(2);
        bean.setFirstName("Jane");
        bean.setLastName("Doe");
        repository.simpleInsert(bean);

        // Update the bean
        bean.setFirstName("Janet");
        repository.simpleUpdate(bean);

        // Select it back to verify the update
        TestBean criteria = new TestBean();
        criteria.setId(2);
        TestBean updatedBean = repository.simpleSelect(criteria);

        assertThat(updatedBean.getFirstName(), is("Janet"));
    }

    @Test
    void testSimpleSelectList() throws Exception {
        // Insert multiple records
        TestBean bean1 = new TestBean();
        bean1.setId(10);
        bean1.setFirstName("Agent");
        bean1.setLastName("Smith");
        repository.simpleInsert(bean1);

        TestBean bean2 = new TestBean();
        bean2.setId(11);
        bean2.setFirstName("Agent");
        bean2.setLastName("Jones");
        repository.simpleInsert(bean2);

        // Select a list of beans
        TestBean criteria = new TestBean();
        criteria.setFirstName("Agent"); // Criteria to select both agents

        List<TestBean> results = repository.simpleSelectList(criteria);

        assertThat(results, hasSize(2));
        assertThat(results, containsInAnyOrder(
            hasProperty("lastName", is("Smith")),
            hasProperty("lastName", is("Jones"))
        ));
    }
}