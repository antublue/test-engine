package example.order;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
public class ConcreteOrderTest extends BaseOrderTest {

    private static final List<String> EXPECTED_LIST =
            listOf(
                    "beforeClass2",
                    "beforeClass",
                    "beforeAll2",
                    "beforeAll",
                    "test2",
                    "test3",
                    "testA",
                    "testB",
                    "afterAll2",
                    "afterAll",
                    "afterClass2",
                    "afterClass");

    @TestEngine.Arguments
    public static Stream<Argument> arguments() {
        Collection<Argument> collection = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            int value = i * 3;
            collection.add(org.antublue.test.engine.api.Argument.of(String.valueOf(value)));
        }
        return collection.stream();
    }

    @TestEngine.Argument
    public void argument(Argument argument) {
        this.argument = argument;
    }

    @TestEngine.BeforeClass
    @TestEngine.Order(2)
    public static void beforeClass() {
        System.out.println("beforeClass()");
        ACTUAL_LIST.add("beforeClass");
    }

    @TestEngine.BeforeAll
    @TestEngine.Order(2)
    public void beforeAll() {
        System.out.println("beforeAll()");
        ACTUAL_LIST.add("beforeAll");
    }

    @TestEngine.Test
    public void testA() {
        System.out.println("testA(" + argument.value() + ")");
        ACTUAL_LIST.add("testA");
    }

    @TestEngine.Test
    public void testB() {
        System.out.println("testB(" + argument.value() + ")");
        ACTUAL_LIST.add("testB");
    }

    @TestEngine.Test
    @TestEngine.Order(2)
    public void test3() {
        System.out.println("test3(" + argument.value() + ")");
        ACTUAL_LIST.add("test3");
    }

    @TestEngine.AfterAll
    @TestEngine.Order(2)
    public void afterAll() {
        System.out.println("afterAll()");
        ACTUAL_LIST.add("afterAll");
    }

    @TestEngine.AfterClass
    @TestEngine.Order(2)
    public static void afterClass() {
        System.out.println("afterClass()");
        ACTUAL_LIST.add("afterClass");
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
