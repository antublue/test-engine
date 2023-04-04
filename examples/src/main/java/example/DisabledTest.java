package example;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
@TestEngine.Disabled
public class DisabledTest {

    private Argument argument;

    @TestEngine.Arguments
    public static Stream<Argument> arguments() {
        return StringArguments.stream();
    }

    @TestEngine.Argument
    public void argument(Argument argument) {
        this.argument = argument;
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
        System.out.println("test1(" + argument + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    private static class StringArguments {

        public static Stream<Argument> stream() {
            Collection<Argument> collection = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                collection.add(org.antublue.test.engine.api.Argument.of(String.valueOf(i)));
            }
            return collection.stream();
        }
    }
}
