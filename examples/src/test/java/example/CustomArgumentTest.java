package example;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/** Example test */
public class CustomArgumentTest {

    @TestEngine.Argument private CustomArgument customArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<CustomArgument> arguments() {
        Collection<CustomArgument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int value = i * 3;
            collection.add(
                    CustomArgument.of(
                            "CustomArgument(" + i + ") = " + value, String.valueOf(value)));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + customArgument.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + customArgument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    private static class CustomArgument implements Argument {

        private final String name;
        private final String value;

        private CustomArgument(String name, String value) {
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

        public static CustomArgument of(String name, String value) {
            Objects.requireNonNull(name);
            return new CustomArgument(name, value);
        }
    }
}
