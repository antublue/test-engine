package org.antublue.test.engine.testing;

import static org.assertj.core.api.Assertions.assertThat;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/** Example test */
public class MethodOrderTest {

    private static final List<String> EXPECTED_ORDER = new ArrayList<>();
    private static final List<String> ACTUAL_ORDER = new ArrayList<>();

    static {
        EXPECTED_ORDER.add("prepare2()");
        EXPECTED_ORDER.add("prepare()");

        StringArgumentSupplier.arguments()
                .forEach(
                        stringArgument -> {
                            EXPECTED_ORDER.add(stringArgument + ".beforeAll()");
                            EXPECTED_ORDER.add(stringArgument + ".beforeEach()");
                            EXPECTED_ORDER.add(stringArgument + ".test2()");
                            EXPECTED_ORDER.add(stringArgument + ".afterEach()");
                            EXPECTED_ORDER.add(stringArgument + ".beforeEach()");
                            EXPECTED_ORDER.add(stringArgument + ".test1()");
                            EXPECTED_ORDER.add(stringArgument + ".afterEach()");
                            EXPECTED_ORDER.add(stringArgument + ".afterAll2()");
                            EXPECTED_ORDER.add(stringArgument + ".afterAll()");
                        });

        EXPECTED_ORDER.add("conclude()");
    }

    @TestEngine.Argument protected StringArgument stringArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<StringArgument> arguments() {
        return StringArgumentSupplier.arguments();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");

        assertThat(stringArgument).isNull();

        ACTUAL_ORDER.add("prepare()");
    }

    @TestEngine.Prepare
    @TestEngine.Order(order = 0)
    public void prepare2() {
        System.out.println("prepare2()");

        assertThat(stringArgument).isNull();

        ACTUAL_ORDER.add("prepare2()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");

        ACTUAL_ORDER.add(stringArgument + ".beforeAll()");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");

        ACTUAL_ORDER.add(stringArgument + ".beforeEach()");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 1)
    public void test1() {
        System.out.println("test1(" + stringArgument + ")");

        ACTUAL_ORDER.add(stringArgument + ".test1()");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 0)
    public void test2() {
        System.out.println("test2(" + stringArgument + ")");

        ACTUAL_ORDER.add(stringArgument + ".test2()");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");

        ACTUAL_ORDER.add(stringArgument + ".afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");

        ACTUAL_ORDER.add(stringArgument + ".afterAll()");
    }

    @TestEngine.AfterAll
    @TestEngine.Order(order = 0)
    public void afterAll2() {
        System.out.println("afterAll2()");

        ACTUAL_ORDER.add(stringArgument + ".afterAll2()");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");

        assertThat(stringArgument).isNull();

        ACTUAL_ORDER.add("conclude()");

        assertThat(ACTUAL_ORDER).isEqualTo(EXPECTED_ORDER);
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
