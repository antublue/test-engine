package example.argument;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.LongArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/** Example test */
public class LongArgumentTest {

    @TestEngine.Argument protected LongArgument longArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<LongArgument> arguments() {
        Collection<LongArgument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(LongArgument.of((long) i));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + longArgument.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + longArgument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
