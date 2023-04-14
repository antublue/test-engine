package example.inheritance2;

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcreteEvenTest extends EvenBaseTest {

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        return EvenBaseTest.parameters();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + parameter.value() + ")");
        assertThat((Integer) parameter.value() % 2).isEven();
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + parameter.value() + ")");
        assertThat((Integer) parameter.value() % 2).isEven();
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }
}
