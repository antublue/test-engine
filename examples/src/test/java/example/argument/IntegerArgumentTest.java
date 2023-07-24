package example.argument;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/** Example test */
public class IntegerArgumentTest {

    @TestEngine.Argument protected IntegerArgument integerArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<IntegerArgument> arguments() {
        Collection<IntegerArgument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(IntegerArgument.of(i));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + integerArgument + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + integerArgument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
