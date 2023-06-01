package org.antublue.test.engine.testing;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class MethodOrderTest {

    @TestEngine.Argument
    protected StringArgument stringArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<StringArgument> arguments() {
        return StringArgumentSupplier.arguments();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    @TestEngine.Order(0)
    public void test1() {
        System.out.println("test1(" + stringArgument.value() + ")");
    }

    @TestEngine.Test
    @TestEngine.Order(1)
    public void test2() {
        System.out.println("test2(" + stringArgument.value() + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    private static class StringArgumentSupplier {

        public static Stream<StringArgument> arguments() {
            Collection<StringArgument> collection = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                collection.add(StringArgument.of(String.valueOf(i)));
            }
            return collection.stream();
        }
    }
}
