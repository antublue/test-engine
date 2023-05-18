package example;

import org.antublue.test.engine.api.ObjectArgument;
import org.antublue.test.engine.api.TestEngine;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
@TestEngine.DisplayName("_Run First ClassDisplayNameTest because of classname_")
public class ClassDisplayNameTest {

    @TestEngine.Argument
    protected ObjectArgument<Integer> objectArgument;

    private int value;

    @TestEngine.ArgumentSupplier
    public static Stream<ObjectArgument> arguments() {
        return Stream.of(
                ObjectArgument.of(1),
                ObjectArgument.of(2),
                ObjectArgument.of(3));
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
        value = objectArgument.value();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + objectArgument.value() + ")");
        assertThat(objectArgument.value().getClass()).isEqualTo(Integer.class);
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + objectArgument.value() + ")");
        assertThat(objectArgument.value().getClass()).isEqualTo(Integer.class);
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
