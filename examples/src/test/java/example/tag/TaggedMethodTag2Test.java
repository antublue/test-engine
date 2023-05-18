package example.tag;

import org.antublue.test.engine.api.ObjectArgument;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 *
 * All test methods are executed due to the fact that the test engine system
 * properties / environment variables have to be defined during test discovery
 */
public class TaggedMethodTag2Test {

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
    @TestEngine.Tag("/tag2/")
    public void test1() {
        System.out.println("test1(" + objectArgument.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + objectArgument.value() + ")");
    }

    @TestEngine.Test
    public void test3() {
        System.out.println("test3(" + objectArgument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
