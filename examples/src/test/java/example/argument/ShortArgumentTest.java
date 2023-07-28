package example.argument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.ShortArgument;

/** Example test */
public class ShortArgumentTest {

    @TestEngine.Argument protected ShortArgument shortArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<ShortArgument> arguments() {
        Collection<ShortArgument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(ShortArgument.of((short) i));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + shortArgument.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + shortArgument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
