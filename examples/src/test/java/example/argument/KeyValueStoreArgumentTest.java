package example.argument;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.support.KeyValueStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class KeyValueStoreArgumentTest {

    private static final String KEY = "key";

    @TestEngine.Argument
    protected KeyValueStoreArgument keyValueStoreArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<KeyValueStoreArgument> arguments() {
        Collection<KeyValueStoreArgument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            KeyValueStoreArgument keyValueStoreArgument = new KeyValueStoreArgument("keyValueStore[" + i + "]");
            keyValueStoreArgument.keyValueStore().put(KEY, "String " + i);

            collection.add(keyValueStoreArgument);

        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        String value = keyValueStoreArgument.keyValueStore().get(KEY);
        System.out.println("test1(" + value + ")");
    }

    @TestEngine.Test
    public void test2() {
        String value = keyValueStoreArgument.keyValueStore.get(KEY);
        System.out.println("test2(" + value + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
        keyValueStoreArgument.keyValueStore().dispose();
    }

    public static class KeyValueStoreArgument implements Argument {

        private final String name;
        private final KeyValueStore keyValueStore;


        public KeyValueStoreArgument(String name) {
            this.name = name;
            this.keyValueStore = new KeyValueStore();
        }

        /**
         * Method to get the Argument name
         *
         * @return the Argument name
         */
        @Override
        public String name() {
            return name;
        }

        public KeyValueStore keyValueStore() {
            return keyValueStore;
        }
    }
}
