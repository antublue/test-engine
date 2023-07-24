package example.argument.supplier;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

import java.util.ArrayList;
import java.util.List;

/** Example test */
public class ListTest {

    @TestEngine.Argument protected IntegerArgument integerArgument;

    @TestEngine.ArgumentSupplier
    public static List<IntegerArgument> arguments() {
        ArrayList<IntegerArgument> arguments = new ArrayList<>();
        arguments.add(IntegerArgument.of(1));
        arguments.add(IntegerArgument.of(2));
        arguments.add(IntegerArgument.of(3));
        return arguments;
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
        System.out.println("test1(" + integerArgument + ")");
    }

    @TestEngine.Test
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
