package test.java.com.spring.ia;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.spring.ia.IaApplication;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = IaApplication.class)
class IaApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testApplicationContextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    void testIaApplicationBeanExists() {
        assertTrue(applicationContext.containsBean("iaApplication"));
    }

    @Test
    void testSpringBootApplicationStarts() {
        assertNotNull(applicationContext);
    }

    @Test
    void testDataSourceExcluded() {
        assertFalse(applicationContext.containsBean("dataSource"));
    }

    @Test
    void testJpaExcluded() {
        assertFalse(applicationContext.containsBean("entityManagerFactory"));
    }
}
