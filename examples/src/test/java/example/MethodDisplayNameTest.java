package example;

import org.antublue.test.engine.api.SimpleParameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
public class MethodDisplayNameTest {

    @TestEngine.Parameter
    private SimpleParameter<Integer> simpleParameter;

    @TestEngine.ParameterSupplier
    public static Stream<SimpleParameter<Integer>> parameters() {
        return Stream.of(
                SimpleParameter.of(1),
                SimpleParameter.of(2),
                SimpleParameter.of(3));
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
        System.out.println("testA(" + simpleParameter.value() + ")");
        assertThat(simpleParameter.value().getClass()).isEqualTo(Integer.class);
    }

    @TestEngine.Test
    @TestEngine.DisplayName("Test 1")
    public void testB() {
        System.out.println("testB(" + simpleParameter.value() + ")");
        assertThat(simpleParameter.value().getClass()).isEqualTo(Integer.class);
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
