package example;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
public class FilteredArgumentStreamTest {

    @TestEngine.Argument
    protected StringArgument stringArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<StringArgument> arguments() {
        return ArgumentSupplier
                .arguments(integerArgument -> !integerArgument.value().contains("b"))
                .collect(Collectors.toList())
                .stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + stringArgument  + ")");
        assertThat(stringArgument.value()).isNotEqualTo("b");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + stringArgument  + ")");
        assertThat(stringArgument.value()).isNotEqualTo("b");
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

        public static Stream<StringArgument> arguments() {
            Collection<StringArgument> arguments = new ArrayList<>();
            for (String value : VALUES) {
                arguments.add(StringArgument.of(value));
            }
            return arguments.stream();
        }

        public static Stream<StringArgument> arguments(Predicate<StringArgument> predicate) {
            return predicate != null ? arguments().filter(predicate) : arguments();
        }
    }
}
