package example;

import org.antublue.test.engine.api.ObjectArgument;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class DisabledMethodTest {

    @TestEngine.Argument
    protected ObjectArgument<String> objectArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<ObjectArgument<String>> arguments() {
        return StringArgumentSupplier.arguments();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + objectArgument + ")");
    }

    @TestEngine.Disabled
    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + objectArgument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    private static class StringArgumentSupplier {

        public static Stream<ObjectArgument<String>> arguments() {
            Collection<ObjectArgument<String>> collection = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                collection.add(ObjectArgument.of(String.valueOf(i)));
            }
            return collection.stream();
        }
    }
}
