package example;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.ObjectArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/** Example test */
public class ArrayTestWithProtectedMethods {

    private String[] values;

    @TestEngine.Argument protected ObjectArgument<String[]> objectArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<ObjectArgument<String[]>> arguments() {
        Collection<ObjectArgument<String[]>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(
                    new ObjectArgument<>(
                            "Array [" + i + "]",
                            new String[] {String.valueOf(i), String.valueOf(i * 2)}));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");

        values = objectArgument.value();
    }

    @TestEngine.BeforeEach
    protected void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    protected void test1() {
        System.out.println("test1(" + values[0] + ", " + values[1] + ")");
    }

    @TestEngine.Test
    protected void test2() {
        System.out.println("test2(" + values[0] + ", " + values[1] + ")");
    }

    @TestEngine.Test
    protected void test3() {
        System.out.println("test3(" + values[0] + ", " + values[1] + ")");
    }

    @TestEngine.AfterEach
    protected void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    protected void afterAll() {
        System.out.println("afterAll()");
    }
}
