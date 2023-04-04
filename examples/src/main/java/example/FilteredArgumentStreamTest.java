package example;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Example test
 */
public class FilteredArgumentStreamTest {

    private Argument argument;

    @TestEngine.Arguments
    public static Stream<Argument> arguments() {
        return ArgumentSupplier
                .arguments(argument -> !argument.value(String.class).contains("b"))
                .collect(Collectors.toList())
                .stream();
    }

    @TestEngine.Argument
    public void argument(Argument argument) {
        this.argument = argument;
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + argument.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    private static class ArgumentSupplier {

        private static final String[] VALUES = { "a", "b", "c" };

        private ArgumentSupplier() {
            // DO NOTHING
        }

        public static Stream<Argument> arguments() {
            Collection<Argument> arguments = new ArrayList<>();
            for (String value : VALUES) {
                arguments.add(org.antublue.test.engine.api.Argument.of(value));
            }
            return arguments.stream();
        }

        public static Stream<Argument> arguments(Predicate<Argument> predicate) {
            return predicate != null ? arguments().filter(predicate) : arguments();
        }
    }
}
