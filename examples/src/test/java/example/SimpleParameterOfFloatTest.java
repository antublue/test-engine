package example;

import org.antublue.test.engine.api.SimpleParameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class SimpleParameterOfFloatTest {

    @TestEngine.Parameter
    private SimpleParameter<Float> simpleParameter;

    @TestEngine.ParameterSupplier
    public static Stream<SimpleParameter<Float>> parameters() {
        Collection<SimpleParameter<Float>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(SimpleParameter.of(i + 0.1f));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        float value = simpleParameter.value();
        System.out.println("test1(" + value + ")");
    }

    @TestEngine.Test
    public void test2() {
        float value = simpleParameter.value();
        System.out.println("test2(" + value + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
