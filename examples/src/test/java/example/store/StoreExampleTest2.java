package example.store;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Store;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

/** Example test */
public class StoreExampleTest2 {

    private static final String PREFIX = "StoreExampleTest";
    private static final String TEST_OBJECT = PREFIX + ".testObject";

    private Store store;

    @TestEngine.Argument protected StringArgument stringArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<StringArgument> arguments() {
        Collection<StringArgument> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(StringArgument.of("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");

        store = new Store();
        store.put(TEST_OBJECT, new TestObject());
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + stringArgument + ")");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + stringArgument + ")");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + stringArgument + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + stringArgument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + stringArgument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + stringArgument + ")");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");

        store.remove(TEST_OBJECT, (Consumer<TestObject>) testObject -> testObject.close());

        assertThat(store.get(TEST_OBJECT)).isNotPresent();
    }

    private static class TestObject {

        public TestObject() {
            // DO NOTHING
        }

        public void close() {
            System.out.println(getClass().getName() + ".close()");
        }
    }
}
