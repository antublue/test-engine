package example.order;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestEngine.BaseClass
class BaseOrderTest {

    protected final List<String> EXPECTED_LIST =
            listOf(
                    "BaseOrderTest.prepare()",
                    "ConcreteOrderTest.prepare()",
                    "BaseOrderTest.beforeAll()",
                    "ConcreteOrderTest.beforeAll()",
                    "ConcreteOrderTest.afterAll()",
                    "BaseOrderTest.afterAll()",
                    "ConcreteOrderTest.conclude()",
                    "BaseOrderTest.conclude()");

    protected final List<String> ACTUAL_LIST = new ArrayList<>();

    @TestEngine.Argument
    protected StringArgument stringArgument;

    @TestEngine.Prepare
    @TestEngine.Order(1)
    public void prepare() {
        System.out.println("BaseOrderTest.prepare()");
        ACTUAL_LIST.add("BaseOrderTest.prepare()");
    }

    @TestEngine.BeforeAll
    @TestEngine.Order(1)
    public void beforeAll() {
        System.out.println("BaseOrderTest.beforeAll()");
        ACTUAL_LIST.add("BaseOrderTest.beforeAll()");
    }

    @TestEngine.Test
    @TestEngine.Order(1)
    public void test2() {
        System.out.println("BaseOrderTest.test2(" + stringArgument.value() + ")");
    }

    @TestEngine.AfterAll
    @TestEngine.Order(2)
    public void afterAll() {
        System.out.println("BaseOrderTest.afterAll()");
        ACTUAL_LIST.add("BaseOrderTest.afterAll()");
    }

    @TestEngine.Conclude
    @TestEngine.Order(2)
    public void conclude() {
        System.out.println("BaseOrderTest.conclude()");
        ACTUAL_LIST.add("BaseOrderTest.conclude()");
        assertThat(ACTUAL_LIST).isEqualTo(EXPECTED_LIST);
    }

    private static List<String> listOf(String ... strings) {
        List<String> list = new ArrayList<>(strings.length);
        for (String string : strings) {
            list.add(string);
        }
        return list;
    }
}
