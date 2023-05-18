package example;

import org.antublue.test.engine.api.ObjectArgument;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ObjectArgumentOfBooleanTest {

    @TestEngine.Argument
    protected ObjectArgument<Boolean> objectArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<ObjectArgument<Boolean>> arguments() {
        Collection<ObjectArgument<Boolean>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            boolean value = (i % 2) == 0;
            collection.add(ObjectArgument.of(value));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        boolean value = objectArgument.value();
        System.out.println("test1(" + value + ")");
    }

    @TestEngine.Test
    public void test2() {
        boolean value = objectArgument.value();
        System.out.println("test2(" + value + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
