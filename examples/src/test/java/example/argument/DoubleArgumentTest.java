package example.argument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.DoubleArgument;

/** Example test */
public class DoubleArgumentTest {

    @TestEngine.Argument protected DoubleArgument doubleArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<DoubleArgument> arguments() {
        Collection<DoubleArgument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(DoubleArgument.of(i + 0.1d));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + doubleArgument.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + doubleArgument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
