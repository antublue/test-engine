package example;

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Example test
 */
public class FilteredParameterStreamTest {

    private Parameter parameter;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        return ParameterSupplier
                .parameters(parameter -> !parameter.value(String.class).contains("b"))
                .collect(Collectors.toList())
                .stream();
    }

    @TestEngine.ParameterSetter
    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + parameter.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + parameter.value() + ")");
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

        public static Stream<Parameter> parameters() {
            Collection<Parameter> parameters = new ArrayList<>();
            for (String value : VALUES) {
                parameters.add(Parameter.of(value));
            }
            return parameters.stream();
        }

        public static Stream<Parameter> parameters(Predicate<Parameter> predicate) {
            return predicate != null ? parameters().filter(predicate) : parameters();
        }
    }
}
