package example.argument;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.FloatArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/** Example test */
public class FloatArgumentTest {

    @TestEngine.Argument protected FloatArgument floatArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<FloatArgument> arguments() {
        Collection<FloatArgument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(FloatArgument.of(i + 0.1f));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + floatArgument.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + floatArgument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
