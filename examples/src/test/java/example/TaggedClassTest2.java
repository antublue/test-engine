package example;

import org.antublue.test.engine.api.ObjectArgument;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
@TestEngine.Tag("/tag2/")
public class TaggedClassTest2 {

    @TestEngine.Argument
    protected ObjectArgument<String> objectArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<ObjectArgument<String>> arguments() {
        Collection<ObjectArgument<String>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int value = i * 3;
            collection.add(ObjectArgument.of(String.valueOf(value)));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + objectArgument.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + objectArgument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
