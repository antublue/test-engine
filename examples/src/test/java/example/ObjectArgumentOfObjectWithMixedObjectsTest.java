package example;

import org.antublue.test.engine.api.ObjectArgument;
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
public class ObjectArgumentOfObjectWithMixedObjectsTest {

    private Object value;

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

    @TestEngine.Argument
    public ObjectArgument<Object> objectArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<ObjectArgument<Object>> arguments() {
        Set<ObjectArgument<Object>> collection = new LinkedHashSet<>();

        collection.add(new ObjectArgument<>("BigDecimal", new BigDecimal("1000000000000000000000")));
        collection.add(new ObjectArgument<>("Integer", 1));
        collection.add(new ObjectArgument<>("Map", new HashMap<String, String>()));
        collection.add(new ObjectArgument<>("String", "This is a string"));
        collection.add(new ObjectArgument<>("((null))", null));

        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
        value = objectArgument.value();
    }

    @TestEngine.Test
    public void test() {
        System.out.println("[" + value + "]");

        if (value instanceof String) {
            assertThat(TO_SPECIAL_NAME.apply(value)).isEqualTo("string/" + value);
        } else if (value instanceof Integer) {
            assertThat(TO_SPECIAL_NAME.apply(value)).isEqualTo("int/" + value);
        } else if (value instanceof BigDecimal) {
            assertThat(TO_SPECIAL_NAME.apply(value)).isEqualTo("bigDecimal/" + value);
        } else if (value == null) {
            assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> TO_SPECIAL_NAME.apply(value));
        } else {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> TO_SPECIAL_NAME.apply(value));
        }

        System.out.println("[" + value + "] PASSED");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
