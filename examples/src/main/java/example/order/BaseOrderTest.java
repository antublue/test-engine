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
        System.out.println("beforeClass2()");
        ConcreteOrderTest.ACTUAL_LIST.add("beforeClass2");
    }

    @TestEngine.BeforeAll
    @TestEngine.Order(1)
    public void beforeAll2() {
        System.out.println("beforeAll2()");
        ConcreteOrderTest.ACTUAL_LIST.add("beforeAll2");
    }

    @TestEngine.Test
    @TestEngine.Order(1)
    public void test2() {
        System.out.println("test2(" + parameter.value() + ")");
    }

    @TestEngine.AfterAll
    @TestEngine.Order(1)
    public void afterAll2() {
        System.out.println("afterAll2()");
        ConcreteOrderTest.ACTUAL_LIST.add("afterAll2");
    }

    @TestEngine.AfterClass
    @TestEngine.Order(1)
    public static void afterClass2() {
        System.out.println("afterClass2()");
        ConcreteOrderTest.ACTUAL_LIST.add("afterClass2");
    }
}
