package example;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
@TestEngine.Tag("/tag1/")
public class TaggedClassTest {

    @TestEngine.Argument
    protected StringArgument stringArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<StringArgument> arguments() {
        Collection<StringArgument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int value = i * 3;
            collection.add(StringArgument.of(String.valueOf(value)));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + stringArgument  + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + stringArgument  + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
