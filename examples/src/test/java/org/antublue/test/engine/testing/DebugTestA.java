package org.antublue.test.engine.testing;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;
import org.opentest4j.AssertionFailedError;

import java.util.stream.Stream;

/**
 * Test used for debugging IntelliJ
 */
@TestEngine.Disabled
public class DebugTestA {

    @TestEngine.Argument
    public IntegerArgument integerArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<IntegerArgument> arguments() {
        return Stream.of(
                IntegerArgument.of(1),
                IntegerArgument.of(2),
                IntegerArgument.of(3));
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
    public void test1() {
        System.out.println("test1(" + integerArgument.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + integerArgument.value() + ")");
        if (integerArgument.value() == 1) {
            throw new AssertionFailedError("Forced");
        }
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
