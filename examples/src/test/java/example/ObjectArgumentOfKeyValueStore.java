package example;

import org.antublue.test.engine.api.KeyValueStore;
import org.antublue.test.engine.api.ObjectArgument;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ObjectArgumentOfKeyValueStore {

    private static final String KEY = "key";

    @TestEngine.Argument
    protected ObjectArgument<KeyValueStore> objectArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<ObjectArgument<KeyValueStore>> arguments() {
        Collection<ObjectArgument<KeyValueStore>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(
                    new ObjectArgument<>(
                            "keyValueStore[" + i + "]",
                            new KeyValueStore().put(KEY, "String " + i)));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        String value = objectArgument.value().get(KEY);
        System.out.println("test1(" + value + ")");
    }

    @TestEngine.Test
    public void test2() {
        String value = objectArgument.value().get(KEY);
        System.out.println("test2(" + value + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
        objectArgument.value().dispose();
    }
}
