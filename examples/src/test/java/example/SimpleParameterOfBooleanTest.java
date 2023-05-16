package example;

import org.antublue.test.engine.api.SimpleParameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class SimpleParameterOfBooleanTest {

    @TestEngine.Parameter
    private SimpleParameter<Boolean> simpleParameter;

    @TestEngine.ParameterSupplier
    public static Stream<SimpleParameter<Boolean>> parameters() {
        Collection<SimpleParameter<Boolean>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            boolean value = (i % 2) == 0;
            collection.add(SimpleParameter.of(value));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        boolean value = simpleParameter.value();
        System.out.println("test1(" + value + ")");
    }

    @TestEngine.Test
    public void test2() {
        boolean value = simpleParameter.value();
        System.out.println("test2(" + value + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
