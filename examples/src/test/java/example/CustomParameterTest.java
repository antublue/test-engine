package example;

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Example test
 */
public class CustomParameterTest {

    @TestEngine.Parameter
    private CustomParameter customParameter;

    @TestEngine.ParameterSupplier
    public static Stream<CustomParameter> parameters() {
        Collection<CustomParameter> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int value = i * 3;
            collection.add(CustomParameter.of("CustomParameter(" + i + ") = " + value, String.valueOf(value)));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + customParameter.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + customParameter.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    private static class CustomParameter implements Parameter {

        private final String name;
        private final String value;

        private CustomParameter(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String name() {
            return name;
        }

        public String value() {
            return value;
        }

        public static CustomParameter of(String name, String value) {
            Objects.requireNonNull(name);
            return new CustomParameter(name, value);
        }
    }
}
