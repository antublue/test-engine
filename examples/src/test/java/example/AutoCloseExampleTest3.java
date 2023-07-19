package example;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class AutoCloseExampleTest3 {

    @TestEngine.Argument
    protected StringArgument stringArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<StringArgument> arguments() {
        Collection<StringArgument> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(StringArgument.of("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.AutoClose(scope="@TestEngine.AfterEach",method="destroy")
    private TestObject afterEachTestObject;

    @TestEngine.AutoClose(scope="@TestEngine.AfterAll",method="destroy")
    private TestObject afterAllTestObject;

    @TestEngine.AutoClose(scope="@TestEngine.Conclude",method="destroy")
    private TestObject concludeTestObject;

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare(" + stringArgument  + ")");
        concludeTestObject = new TestObject("concludeTestObject");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + stringArgument  + ")");
        afterAllTestObject = new TestObject("afterAllTestObject");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + stringArgument  + ")");
        afterEachTestObject = new TestObject("afterEachTestObject");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + stringArgument  + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + stringArgument  + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + stringArgument  + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + stringArgument  + ")");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude(" + stringArgument  + ")");
    }

    private static class TestObject {

        private final String name;

        public TestObject(String name) {
            this.name = name;
        }

        public void destroy() {
            System.out.println(name + ".destroy()");
        }
    }
}
