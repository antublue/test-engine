package example;

import org.antublue.test.engine.api.ObjectArgument;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ObjectArgumentOfFloatTest {

    @TestEngine.Argument
    protected ObjectArgument<Float> objectArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<ObjectArgument<Float>> arguments() {
        Collection<ObjectArgument<Float>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(ObjectArgument.of(i + 0.1f));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        float value = objectArgument.value();
        System.out.println("test1(" + value + ")");
    }

    @TestEngine.Test
    public void test2() {
        float value = objectArgument.value();
        System.out.println("test2(" + value + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
