package example;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Example test
 */
@SuppressWarnings("unchecked")
public class CustomArgumentTest2 {

    private Long value;

    @TestEngine.Arguments
    public static Stream<Argument> arguments() {
        Collection<Argument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            long value = i * 3;
            collection.add(CustomArgument.of("CustomArgument(" + i + ") = " + value, value));
        }
        return collection.stream();
    }

    @TestEngine.Argument
    public void argument(Argument argument) {
        value = argument.value(Long.class);
    }

    @TestEngine.BeforeClass
    public static void beforeClass() {
        System.out.println("beforeClass()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + value + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + value + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    @TestEngine.AfterClass
    public static void afterClass() {
        System.out.println("afterClass()");
    }

    private static class CustomArgument implements Argument {

        private final String name;
        private final Long value;

        private CustomArgument(String name, Long value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public <T> T value() {
            return (T) this;
        }

        @Override
        public <T> T value(Class<T> clazz) {
            return clazz.cast(value);
        }

        public static CustomArgument of(String name, Long value) {
            Objects.requireNonNull(name);
            return new CustomArgument(name, value);
        }
    }
}
