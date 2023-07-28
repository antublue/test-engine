package example.autoclose;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

/** Example test */
public class AutoCloseExampleTest1 {

    @TestEngine.Argument protected StringArgument stringArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<StringArgument> arguments() {
        Collection<StringArgument> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(StringArgument.of("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.AutoClose(lifecycle = "@TestEngine.AfterEach")
    private AutoCloseable afterEachAutoClosable;

    @TestEngine.AutoClose(lifecycle = "@TestEngine.AfterAll")
    private AutoCloseable afterAllAutoClosable;

    @TestEngine.AutoClose(lifecycle = "@TestEngine.Conclude")
    private AutoCloseable concludeAutoCloseable;

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");

        concludeAutoCloseable = new TestAutoCloseable("concludeAutoCloseable");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + stringArgument + ")");

        afterAllAutoClosable = new TestAutoCloseable("afterAllAutoCloseable");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + stringArgument + ")");

        afterEachAutoClosable = new TestAutoCloseable("afterEachAutoCloseable");
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
    }

    private static class TestAutoCloseable implements AutoCloseable {

        private final String name;

        public TestAutoCloseable(String name) {
            this.name = name;
        }

        public void close() {
            System.out.println(name + ".close()");
        }
    }
}
