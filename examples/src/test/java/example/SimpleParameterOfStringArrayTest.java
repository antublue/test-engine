package example;

import org.antublue.test.engine.api.SimpleParameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class SimpleParameterOfStringArrayTest {

    private String[] values;

    @TestEngine.Parameter
    public SimpleParameter<String[]> simpleParameter;

    @TestEngine.ParameterSupplier
    public static Stream<SimpleParameter<String[]>> parameters() {
        Collection<SimpleParameter<String[]>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(
                    new SimpleParameter<>(
                            "Array [" + i + "]",
                            new String[] { String.valueOf(i), String.valueOf(i * 2) }));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
        values = simpleParameter.value();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + values[0] + ", " + values[1] + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + values[0] + ", " + values[1] + ")");
    }

    @TestEngine.Test
    public void test3() {
        System.out.println("test3(" + values[0] + ", " + values[1] + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
