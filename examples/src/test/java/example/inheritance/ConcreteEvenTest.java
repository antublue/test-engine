package example.inheritance;

import org.antublue.test.engine.api.SimpleParameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcreteEvenTest extends EvenBaseTest {

    @TestEngine.ParameterSupplier
    public static Stream<SimpleParameter<Integer>> parameters() {
        return EvenBaseTest.parameters();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + simpleParameter.value() + ")");
        assertThat((Integer) simpleParameter.value() % 2).isEven();
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + simpleParameter.value() + ")");
        assertThat((Integer) simpleParameter.value() % 2).isEven();
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }
}
