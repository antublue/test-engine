package example;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
public class SimpleExampleTest {

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

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + stringArgument  + ")");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + stringArgument  + ")");
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
        System.out.println("conclude()");

        assertThat(stringArgument).isNull();
    }
}
