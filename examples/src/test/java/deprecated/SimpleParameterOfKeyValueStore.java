package deprecated;

import org.antublue.test.engine.api.KeyValueStore;
import org.antublue.test.engine.api.SimpleParameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class SimpleParameterOfKeyValueStore {

    private static final String KEY = "key";

    @TestEngine.Parameter
    private SimpleParameter<KeyValueStore> simpleParameter;

    @TestEngine.ParameterSupplier
    public static Stream<SimpleParameter<KeyValueStore>> parameters() {
        Collection<SimpleParameter<KeyValueStore>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(
                    new SimpleParameter<>(
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
        String value = simpleParameter.value().get(KEY);
        System.out.println("test1(" + value + ")");
    }

    @TestEngine.Test
    public void test2() {
        String value = simpleParameter.value().get(KEY);
        System.out.println("test2(" + value + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
        simpleParameter.value().dispose();
    }
}
