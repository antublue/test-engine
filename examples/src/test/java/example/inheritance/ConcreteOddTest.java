package example.inheritance;

import org.antublue.test.engine.api.SimpleParameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcreteOddTest extends OddBaseTest {

    @TestEngine.ParameterSupplier
    protected static Stream<SimpleParameter<Integer>> parameters() {
        return OddBaseTest.parameters();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + simpleParameter + ")");
        assertThat((Integer) simpleParameter.value() % 2).isOdd();
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + simpleParameter + ")");
        assertThat((Integer) simpleParameter.value() % 2).isOdd();
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }
}
