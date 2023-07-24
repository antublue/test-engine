package example.autoclose;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/** Example test */
public class AutoCloseExampleTest2 {

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

        concludeAutoCloseable = new TestCloseable("concludeCloseable");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + stringArgument + ")");

        afterAllAutoClosable = new TestCloseable("afterAllCloseable");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + stringArgument + ")");

        afterEachAutoClosable = new TestCloseable("afterEachCloseable");
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

    private static class TestCloseable implements Closeable {

        private final String name;

        public TestCloseable(String name) {
            this.name = name;
        }

        public void close() {
            System.out.println(name + ".close()");
        }
    }
}
