package deprecated;

import org.antublue.test.engine.api.SimpleParameter;
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
public class FilteredParameterStreamTest {

    @TestEngine.Parameter
    private SimpleParameter<String> simpleParameter;

    @TestEngine.ParameterSupplier
    public static Stream<SimpleParameter<String>> parameters() {
        return ParameterSupplier
                .parameters(simpleParameter -> !simpleParameter.value().contains("b"))
                .collect(Collectors.toList())
                .stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + simpleParameter.value() + ")");
        assertThat(simpleParameter.value()).isNotEqualTo("b");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + simpleParameter.value() + ")");
        assertThat(simpleParameter.value()).isNotEqualTo("b");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    private static class ParameterSupplier {

        private static final String[] VALUES = { "a", "b", "c" };

        private ParameterSupplier() {
            // DO NOTHING
        }

        public static Stream<SimpleParameter<String>> parameters() {
            Collection<SimpleParameter<String>> parameters = new ArrayList<>();
            for (String value : VALUES) {
                parameters.add(SimpleParameter.of(value));
            }
            return parameters.stream();
        }

        public static Stream<SimpleParameter<String>> parameters(Predicate<SimpleParameter<String>> predicate) {
            return predicate != null ? parameters().filter(predicate) : parameters();
        }
    }
}
