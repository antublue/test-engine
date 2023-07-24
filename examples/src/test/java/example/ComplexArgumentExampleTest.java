package example;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.AbstractArgument;

import java.util.stream.Stream;

/**
 * Example test
 */
public class ComplexArgumentExampleTest {

    @TestEngine.Argument
    @TestEngine.AutoClose(lifecycle = "@TestEngine.AfterAll")
    protected ComplexArgument complexArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<ComplexArgument> arguments() {
        return Stream.of(
                new ComplexArgument("A", "http://foo.bar"),
                new ComplexArgument("B", "http://bar.foo"));
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + complexArgument.name()  + ")");

        complexArgument.initialize();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + complexArgument.name()  + ")");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + complexArgument.name()  + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + complexArgument.name()  + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + complexArgument.name()  + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + complexArgument.name()  + ")");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
    }

    private static class ComplexArgument extends AbstractArgument implements AutoCloseable {

        private final String name;
        private final String url;

        public ComplexArgument(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String name() {
            return name;
        }

        public void initialize() {
            System.out.println(name + " -> initialize(" + url + ")");
        }

        public void close() {
            System.out.println(name + " -> close(" + url + ")");
        }
    }
}
