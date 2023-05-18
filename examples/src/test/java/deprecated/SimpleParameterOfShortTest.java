package deprecated;

import org.antublue.test.engine.api.SimpleParameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class SimpleParameterOfShortTest {

    @TestEngine.Parameter
    private SimpleParameter<Short> simpleParameter;

    @TestEngine.ParameterSupplier
    public static Stream<SimpleParameter<Short>> parameters() {
        Collection<SimpleParameter<Short>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(SimpleParameter.of((short) i));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        short value = simpleParameter.value();
        System.out.println("test1(" + value + ")");
    }

    @TestEngine.Test
    public void test2() {
        short value = simpleParameter.value();
        System.out.println("test2(" + value + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
