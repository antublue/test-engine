package example;

import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

/** Example test */
@TestEngine.DisplayName(name = "_A_ClassDisplayNameMethodDisplayNameTest")
public class ClassDisplayNameMethodDisplayNameTest {

    @TestEngine.Argument protected IntegerArgument integerArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<IntegerArgument> arguments() {
        return Stream.of(IntegerArgument.of(1), IntegerArgument.of(2), IntegerArgument.of(3));
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
    @TestEngine.DisplayName(name = "Test A")
    public void test1() {
        System.out.println("test1(" + integerArgument + ")");
    }

    @TestEngine.Test
    @TestEngine.DisplayName(name = "Test B")
    public void test2() {
        System.out.println("test2(" + integerArgument + ")");
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
