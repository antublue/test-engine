package example;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

import java.util.stream.Stream;

/**
 * Example test
 */
public class MethodOrderAndDisplayNameTest {

    @TestEngine.Argument
    protected IntegerArgument integerArgument;

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
    @TestEngine.Order(order = 1)
    @TestEngine.DisplayName(name = "Test 2")
    public void testA() {
        System.out.println("testA(" + integerArgument + ")");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 2)
    @TestEngine.DisplayName(name = "Test 1")
    public void testB() {
        System.out.println("testB(" + integerArgument + ")");
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
