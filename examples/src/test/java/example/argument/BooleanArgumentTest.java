package example.argument;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.BooleanArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/** Example test */
public class BooleanArgumentTest {

    @TestEngine.Argument protected BooleanArgument booleanArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<BooleanArgument> arguments() {
        Collection<BooleanArgument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            boolean value = (i % 2) == 0;
            collection.add(BooleanArgument.of(value));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + booleanArgument.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + booleanArgument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
