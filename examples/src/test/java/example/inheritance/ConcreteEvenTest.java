package example.inheritance;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcreteEvenTest extends EvenBaseTest {

    @TestEngine.ArgumentSupplier
    public static Stream<IntegerArgument> arguments() {
        return EvenBaseTest.arguments();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + integerArgument.value() + ")");
        assertThat((Integer) integerArgument.value() % 2).isEven();
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + integerArgument.value() + ")");
        assertThat((Integer) integerArgument.value() % 2).isEven();
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }
}
