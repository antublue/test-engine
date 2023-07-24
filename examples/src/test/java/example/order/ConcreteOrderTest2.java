package example.order;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
@TestEngine.Order(order = 4)
public class ConcreteOrderTest2 extends BaseOrderTest {

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
    @TestEngine.Order(order = 2)
    public void prepare2() {
        System.out.println("ConcreteOrderTest.prepare()");
        ACTUAL_LIST.add("ConcreteOrderTest.prepare()");
    }

    @TestEngine.BeforeAll
    @TestEngine.Order(order = 2)
    public void beforeAll2() {
        System.out.println("ConcreteOrderTest.beforeAll()");
        ACTUAL_LIST.add("ConcreteOrderTest.beforeAll()");
    }

    @TestEngine.Test
    public void testA() {
        System.out.println("testA(" + stringArgument  + ")");
    }

    @TestEngine.Test
    public void testB() {
        System.out.println("testB(" + stringArgument  + ")");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 2)
    public void test3() {
        System.out.println("test3(" + stringArgument  + ")");
    }

    @TestEngine.AfterAll
    @TestEngine.Order(order = 1)
    public void afterAll2() {
        System.out.println("ConcreteOrderTest.afterAll()");

        ACTUAL_LIST.add("ConcreteOrderTest.afterAll()");
    }

    @TestEngine.Conclude
    @TestEngine.Order(order = 1)
    public void conclude() {
        System.out.println("ConcreteOrderTest.conclude()");

        assertThat(stringArgument).isNull();

        ACTUAL_LIST.add("ConcreteOrderTest.conclude()");
    }
}
