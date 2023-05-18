package example.inheritance;

import org.antublue.test.engine.api.ObjectArgument;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

@TestEngine.BaseClass
public class BaseTest {

    @TestEngine.Argument
    protected ObjectArgument<Integer> objectArgument;

    @TestEngine.ArgumentSupplier
    protected static Stream<ObjectArgument<Integer>> arguments() {
        Collection<ObjectArgument<Integer>> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(new ObjectArgument("Array [" + i + "]", i));
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
