package example;

import org.antublue.test.engine.api.ObjectArgument;
import org.antublue.test.engine.api.TestEngine;

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
    protected ObjectArgument<String> objectArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<ObjectArgument<String>> arguments() {
        return ArgumentSupplier
                .arguments(objectArgument -> !objectArgument.value().contains("b"))
                .collect(Collectors.toList())
                .stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + objectArgument.value() + ")");
        assertThat(objectArgument.value()).isNotEqualTo("b");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + objectArgument.value() + ")");
        assertThat(objectArgument.value()).isNotEqualTo("b");
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

        public static Stream<ObjectArgument<String>> arguments() {
            Collection<ObjectArgument<String>> arguments = new ArrayList<>();
            for (String value : VALUES) {
                arguments.add(ObjectArgument.of(value));
            }
            return arguments.stream();
        }

        public static Stream<ObjectArgument<String>> arguments(Predicate<ObjectArgument<String>> predicate) {
            return predicate != null ? arguments().filter(predicate) : arguments();
        }
    }
}
