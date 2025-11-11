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
import java.util.Collection;
import java.util.ArrayList;

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
            stmt.execute("""
                    CREATE TABLE TEST_BEANS (
                        ID INT PRIMARY KEY,
                        FIRST_NAME VARCHAR(255),
                        LAST_NAME VARCHAR(255),
                        CREATED_DATE VARCHAR(255),
                        ACTIVE BOOLEAN,
                        MIDDLE_INITIAL CHAR(1),
                        AGE INT,
                        SALARY DECIMAL(19, 2),
                        VERSION SMALLINT
                    );
                    """);
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

    @Test
    void testSimpleInsertAndSelectWithDescriptor() throws Exception {
        TestBean bean = new TestBean();
        bean.setId(100);
        bean.setFirstName("Alice");
        bean.setLastName("Wonderland");
        BeanDescriptor descriptor = repository.getBeanDescriptor(bean);
        repository.simpleInsert(bean, descriptor);

        TestBean criteria = new TestBean();
        criteria.setId(100);
        TestBean selected = repository.simpleSelect(criteria, descriptor);
        assertThat(selected, is(notNullValue()));
        assertThat(selected.getFirstName(), is("Alice"));
        assertThat(selected.getLastName(), is("Wonderland"));
    }

    @Test
    void testSimpleSelectListWithDescriptor() throws Exception {
        TestBean bean1 = new TestBean();
        bean1.setId(101);
        bean1.setFirstName("Bob");
        bean1.setLastName("Builder");
        TestBean bean2 = new TestBean();
        bean2.setId(102);
        bean2.setFirstName("Bob");
        bean2.setLastName("Marley");
        BeanDescriptor descriptor = repository.getBeanDescriptor(bean1);
        repository.simpleInsert(bean1, descriptor);
        repository.simpleInsert(bean2, descriptor);

        TestBean criteria = new TestBean();
        criteria.setFirstName("Bob");
        List<TestBean> results = repository.simpleSelectList(criteria, descriptor);
        assertThat(results, hasSize(2));
        assertThat(results, containsInAnyOrder(
            hasProperty("lastName", is("Builder")),
            hasProperty("lastName", is("Marley"))
        ));
    }

    @Test
    void testSimpleDelete() throws Exception {
        TestBean bean = new TestBean();
        bean.setId(200);
        bean.setFirstName("Charlie");
        bean.setLastName("Chocolate");
        repository.simpleInsert(bean);
        repository.simpleDelete(bean);
        TestBean criteria = new TestBean();
        criteria.setId(200);
        TestBean deleted = repository.simpleSelect(criteria);
        assertThat(deleted, is(nullValue()));
    }

    @Test
    void testSimpleDeleteWithDescriptor() throws Exception {
        TestBean bean = new TestBean();
        bean.setId(201);
        bean.setFirstName("Daisy");
        bean.setLastName("Duck");
        BeanDescriptor descriptor = repository.getBeanDescriptor(bean);
        repository.simpleInsert(bean, descriptor);
        repository.simpleDelete(bean, descriptor);
        TestBean criteria = new TestBean();
        criteria.setId(201);
        TestBean deleted = repository.simpleSelect(criteria, descriptor);
        assertThat(deleted, is(nullValue()));
    }

    @Test
    void testSimpleInsertCollectionImplicitDescriptor() throws Exception {
        TestBean bean1 = new TestBean();
        bean1.setId(1000);
        bean1.setFirstName("Batch");
        bean1.setLastName("One");
        TestBean bean2 = new TestBean();
        bean2.setId(1001);
        bean2.setFirstName("Batch");
        bean2.setLastName("Two");
        repository.simpleInsert(List.of(bean1, bean2));
        TestBean criteria = new TestBean();
        criteria.setFirstName("Batch");
        List<TestBean> results = repository.simpleSelectList(criteria);
        assertThat(results, hasSize(2));
        assertThat(results.stream().map(TestBean::getLastName).toList(), containsInAnyOrder("One", "Two"));
    }

    @Test
    void testSimpleInsertCollectionExplicitDescriptor() throws Exception {
        TestBean bean1 = new TestBean();
        bean1.setId(1010);
        bean1.setFirstName("BatchD");
        bean1.setLastName("One");
        TestBean bean2 = new TestBean();
        bean2.setId(1011);
        bean2.setFirstName("BatchD");
        bean2.setLastName("Two");
        BeanDescriptor descriptor = repository.getBeanDescriptor(bean1);
        repository.simpleInsert(List.of(bean1, bean2), descriptor);
        TestBean criteria = new TestBean();
        criteria.setFirstName("BatchD");
        List<TestBean> results = repository.simpleSelectList(criteria, descriptor);
        assertThat(results, hasSize(2));
        assertThat(results.stream().map(TestBean::getLastName).toList(), containsInAnyOrder("One", "Two"));
    }

    @Test
    void testSimpleInsertEmptyCollectionNoOp() throws Exception {
        repository.simpleInsert(new ArrayList<TestBean>()); // should do nothing
        TestBean criteria = new TestBean();
        List<TestBean> results = repository.simpleSelectList(criteria); // empty criteria returns all rows (none)
        assertThat(results, hasSize(0));
    }

    @Test
    void testSimpleDeleteCollectionImplicitDescriptor() throws Exception {
        TestBean bean1 = new TestBean();
        bean1.setId(3000);
        bean1.setFirstName("BatchDel");
        bean1.setLastName("One");
        TestBean bean2 = new TestBean();
        bean2.setId(3001);
        bean2.setFirstName("BatchDel");
        bean2.setLastName("Two");

        // insert then delete the collection using implicit descriptor
        repository.simpleInsert(List.of(bean1, bean2));
        repository.simpleDelete(List.of(bean1, bean2));

        TestBean criteria = new TestBean();
        criteria.setFirstName("BatchDel");
        List<TestBean> results = repository.simpleSelectList(criteria);
        assertThat(results, hasSize(0));
    }

    @Test
    void testSimpleDeleteCollectionExplicitDescriptor() throws Exception {
        TestBean bean1 = new TestBean();
        bean1.setId(3010);
        bean1.setFirstName("BatchDE");
        bean1.setLastName("One");
        TestBean bean2 = new TestBean();
        bean2.setId(3011);
        bean2.setFirstName("BatchDE");
        bean2.setLastName("Two");

        BeanDescriptor descriptor = repository.getBeanDescriptor(bean1);

        // insert then delete the collection using explicit descriptor
        repository.simpleInsert(List.of(bean1, bean2), descriptor);
        repository.simpleDelete(List.of(bean1, bean2), descriptor);

        TestBean criteria = new TestBean();
        criteria.setFirstName("BatchDE");
        List<TestBean> results = repository.simpleSelectList(criteria, descriptor);
        assertThat(results, hasSize(0));
    }

    @Test
    void testSimpleDeleteEmptyCollectionNoOp() throws Exception {
        // no-op should not fail and table remains empty
        repository.simpleDelete(new ArrayList<TestBean>());

        TestBean criteria = new TestBean();
        List<TestBean> results = repository.simpleSelectList(criteria);
        assertThat(results, hasSize(0));
    }

}