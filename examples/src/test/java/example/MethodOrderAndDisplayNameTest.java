package example;

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
public class MethodOrderAndDisplayNameTest {

    private Parameter parameter;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        return Stream.of(
                Parameter.of(1),
                Parameter.of(2),
                Parameter.of(3));
    }

    @TestEngine.Parameter
    public void parameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @TestEngine.BeforeClass
    public static void beforeClass() {
        System.out.println("beforeClass()");
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
    @TestEngine.Order(1)
    @TestEngine.DisplayName("Test 2")
    public void testA() {
        System.out.println("testA(" + parameter.value() + ")");
        assertThat(parameter.value(Integer.class).getClass()).isEqualTo(Integer.class);
    }

    @TestEngine.Test
    @TestEngine.Order(2)
    @TestEngine.DisplayName("Test 1")
    public void testB() {
        System.out.println("testB(" + parameter.value() + ")");
        assertThat(parameter.value(Integer.class).getClass()).isEqualTo(Integer.class);
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    @TestEngine.AfterClass
    public static void afterClass() {
        System.out.println("afterClass()");
    }
}
