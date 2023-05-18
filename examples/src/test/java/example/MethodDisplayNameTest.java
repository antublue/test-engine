package example;

import org.antublue.test.engine.api.ObjectArgument;
import org.antublue.test.engine.api.TestEngine;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
public class MethodDisplayNameTest {

    @TestEngine.Argument
    protected ObjectArgument<Integer> objectArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<ObjectArgument<Integer>> arguments() {
        return Stream.of(
                ObjectArgument.of(1),
                ObjectArgument.of(2),
                ObjectArgument.of(3));
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    @TestEngine.DisplayName("Test 2")
    public void testA() {
        System.out.println("testA(" + objectArgument.value() + ")");
        assertThat(objectArgument.value().getClass()).isEqualTo(Integer.class);
    }

    @TestEngine.Test
    @TestEngine.DisplayName("Test 1")
    public void testB() {
        System.out.println("testB(" + objectArgument.value() + ")");
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
