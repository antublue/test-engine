package example.order;

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.List;

@TestEngine.BaseClass
class BaseOrderTest {

    protected static final List<String> ACTUAL_LIST = new ArrayList<>();

    protected Parameter parameter;

    @TestEngine.BeforeClass
    @TestEngine.Order(1)
    public static void beforeClass2() {
        System.out.println("baseOrderTest.beforeClass2()");
        ConcreteOrderTest.ACTUAL_LIST.add("baseOrderTest.beforeClass2");
    }

    @TestEngine.BeforeAll
    @TestEngine.Order(1)
    public void beforeAll2() {
        System.out.println("baseOrderTest.beforeAll2()");
        ConcreteOrderTest.ACTUAL_LIST.add("baseOrderTest.beforeAll2");
    }

    @TestEngine.Test
    @TestEngine.Order(1)
    public void test2() {
        System.out.println("baseOrderTest.test2(" + parameter.value() + ")");
    }

    @TestEngine.AfterAll
    @TestEngine.Order(1)
    public void afterAll2() {
        System.out.println("baseOrderTest.afterAll2()");
        ConcreteOrderTest.ACTUAL_LIST.add("baseOrderTest.afterAll2");
    }

    @TestEngine.AfterClass
    @TestEngine.Order(1)
    public static void afterClass2() {
        System.out.println("baseOrderTest.afterClass2()");
        ConcreteOrderTest.ACTUAL_LIST.add("baseOrderTest.afterClass2");
    }
}
