package example;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

import java.util.stream.Stream;

/**
 * Example test
 */
public class Junit5ReplacementExampleTest {

    // The stringArgument is required by the test engine, but is not actually used in test methods
    @TestEngine.Argument
    protected StringArgument stringArgument;

    // The stringArgument provides a node in the hierarchy, but is not actually used in test methods
    @TestEngine.ArgumentSupplier
    protected static Stream<StringArgument> arguments() {
        return Stream.of(StringArgument.of("tests"));
    }

    // For a single Argument, a @TestEngine.Prepare method is equivalent to a @TestEngine.BeforeAll method

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
        System.out.println("test1()");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2()");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    // For a single Argument, a @TestEngine.Conclude method is equivalent to a @TestEngine.AfterAll method
}
