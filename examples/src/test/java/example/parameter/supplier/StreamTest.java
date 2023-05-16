package example.parameter.supplier;

import org.antublue.test.engine.api.SimpleParameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
public class StreamTest {

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
    public void test1() {
        System.out.println("test1(" + simpleParameter.value() + ")");
        assertThat(simpleParameter.value().getClass()).isEqualTo(Integer.class);
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + simpleParameter.value() + ")");
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
