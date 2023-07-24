package example.inheritance;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

@TestEngine.BaseClass
public class BaseTest {

    @TestEngine.Argument protected IntegerArgument integerArgument;

    @TestEngine.ArgumentSupplier
    protected static Stream<IntegerArgument> arguments() {
        Collection<IntegerArgument> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(IntegerArgument.of(i));
        }

        return collection.stream();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void testA() {
        System.out.println("testA()");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }
}
