package example;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

/**
 * Example test
 */
public class ArgumentWithMixedObjectTest {

    private Object argument;

    // Function to test... typically this would be core project code
    private static Function<Object, String> TO_SPECIAL_NAME = object -> {
        if (object == null) {
            throw new NullPointerException();
        }

        if (object instanceof String) {
            return "string/" + object;
        } else if (object instanceof Integer) {
            return "int/" + object;
        } else if (object instanceof BigDecimal) {
            return "bigDecimal/" + object;
        } else {
            throw new IllegalArgumentException("Unhandled type [" + object.getClass().getName() + "]");
        }
    };

    @TestEngine.Arguments
    public static Stream<Argument> arguments() {
        Set<Argument> collection = new LinkedHashSet<>();

        collection.add(org.antublue.test.engine.api.Argument.of("BigDecimal", new BigDecimal("1000000000000000000000")));
        collection.add(org.antublue.test.engine.api.Argument.of("Integer", 1));
        collection.add(org.antublue.test.engine.api.Argument.of("Map", new HashMap<String, String>()));
        collection.add(org.antublue.test.engine.api.Argument.of("String", "This is a string"));
        collection.add(org.antublue.test.engine.api.Argument.of("null", null));

        return collection.stream();
    }

    @TestEngine.Argument
    public void argument(Argument argument) {
        this.argument = argument.value();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test() {
        System.out.println("[" + argument + "]");

        if (argument instanceof String) {
            assertThat(TO_SPECIAL_NAME.apply(argument)).isEqualTo("string/" + argument);
        } else if (argument instanceof Integer) {
            assertThat(TO_SPECIAL_NAME.apply(argument)).isEqualTo("int/" + argument);
        } else if (argument instanceof BigDecimal) {
            assertThat(TO_SPECIAL_NAME.apply(argument)).isEqualTo("bigDecimal/" + argument);
        } else if (argument == null) {
            assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> TO_SPECIAL_NAME.apply(argument));
        } else {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> TO_SPECIAL_NAME.apply(argument));
        }

        System.out.println("[" + argument + "] PASSED");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
