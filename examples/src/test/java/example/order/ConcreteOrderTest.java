package example.order;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ConcreteOrderTest extends BaseOrderTest {

    @TestEngine.ArgumentSupplier
    public static Stream<StringArgument> arguments() {
        Collection<StringArgument> collection = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            int value = i * 3;
            collection.add(StringArgument.of(String.valueOf(value)));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    @TestEngine.Order(2)
    public void prepare2() {
        System.out.println("ConcreteOrderTest.prepare()");
        ACTUAL_LIST.add("ConcreteOrderTest.prepare()");
    }

    @TestEngine.BeforeAll
    @TestEngine.Order(2)
    public void beforeAll2() {
        System.out.println("ConcreteOrderTest.beforeAll()");
        ACTUAL_LIST.add("ConcreteOrderTest.beforeAll()");
    }

    @TestEngine.Test
    public void testA() {
        System.out.println("testA(" + stringArgument.value() + ")");
    }

    @TestEngine.Test
    public void testB() {
        System.out.println("testB(" + stringArgument.value() + ")");
    }

    @TestEngine.Test
    @TestEngine.Order(2)
    public void test3() {
        System.out.println("test3(" + stringArgument.value() + ")");
    }

    @TestEngine.AfterAll
    @TestEngine.Order(1)
    public void afterAll2() {
        System.out.println("ConcreteOrderTest.afterAll()");
        ACTUAL_LIST.add("ConcreteOrderTest.afterAll()");
    }

    @TestEngine.Conclude
    @TestEngine.Order(1)
    public void conclude() {
        System.out.println("ConcreteOrderTest.conclude()");
        ACTUAL_LIST.add("ConcreteOrderTest.conclude()");
    }
}
