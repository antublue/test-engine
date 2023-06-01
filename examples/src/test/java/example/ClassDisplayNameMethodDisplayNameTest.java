package example;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

import java.util.stream.Stream;

/**
 * Example test
 */
@TestEngine.DisplayName("_A_ClassDisplayNameMethodDisplayNameTest")
public class ClassDisplayNameMethodDisplayNameTest {

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
    @TestEngine.DisplayName("Test A")
    public void test1() {
        System.out.println("test1(" + integerArgument.value() + ")");
    }

    @TestEngine.Test
    @TestEngine.DisplayName("Test B")
    public void test2() {
        System.out.println("test2(" + integerArgument.value() + ")");
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
