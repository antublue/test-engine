package example.order;

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
public class OrderTest {

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


    private static final List<String> ACTUAL_LIST = new ArrayList<>();

    private Parameter parameter;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        Collection<Parameter> collection = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            int value = i * 3;
            collection.add(Parameter.of(String.valueOf(value)));
        }
        return collection.stream();
    }

    @TestEngine.ParameterSetter
    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @TestEngine.BeforeClass
    @TestEngine.Order(2)
    public static void beforeClass() {
        System.out.println("beforeClass()");
        ACTUAL_LIST.add("beforeClass");
    }

    @TestEngine.BeforeClass
    @TestEngine.Order(1)
    public static void beforeClass2() {
        System.out.println("beforeClass2()");
        ACTUAL_LIST.add("beforeClass2");
    }

    @TestEngine.BeforeAll
    @TestEngine.Order(2)
    public void beforeAll() {
        System.out.println("beforeAll()");
        ACTUAL_LIST.add("beforeAll");
    }

    @TestEngine.BeforeAll
    @TestEngine.Order(1)
    public void beforeAll2() {
        System.out.println("beforeAll2()");
        ACTUAL_LIST.add("beforeAll2");
    }

    @TestEngine.Test
    public void testA() {
        System.out.println("testA(" + parameter.value() + ")");
        ACTUAL_LIST.add("testA");
    }

    @TestEngine.Test
    public void testB() {
        System.out.println("testB(" + parameter.value() + ")");
        ACTUAL_LIST.add("testB");
    }

    @TestEngine.Test
    @TestEngine.Order(2)
    public void test3() {
        System.out.println("test3(" + parameter.value() + ")");
        ACTUAL_LIST.add("test3");
    }

    @TestEngine.Test
    @TestEngine.Order(1)
    public void test2() {
        System.out.println("test2(" + parameter.value() + ")");
        ACTUAL_LIST.add("test2");
    }

    @TestEngine.AfterAll
    @TestEngine.Order(2)
    public void afterAll() {
        System.out.println("afterAll()");
        ACTUAL_LIST.add("afterAll");
    }

    @TestEngine.AfterAll
    @TestEngine.Order(1)
    public void afterAll2() {
        System.out.println("afterAll2()");
        ACTUAL_LIST.add("afterAll2");
    }

    @TestEngine.AfterClass
    @TestEngine.Order(2)
    public static void afterClass() {
        System.out.println("afterClass()");
        ACTUAL_LIST.add("afterClass");
        assertThat(ACTUAL_LIST).isEqualTo(EXPECTED_LIST);
    }

    @TestEngine.AfterClass
    @TestEngine.Order(1)
    public static void afterClass2() {
        System.out.println("afterClass2()");
        ACTUAL_LIST.add("afterClass2");
    }

    private static List<String> listOf(String ... strings) {
        List<String> list = new ArrayList<>(strings.length);
        for (String string : strings) {
            list.add(string);
        }
        return list;
    }
}
