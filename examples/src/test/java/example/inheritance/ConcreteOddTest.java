package example.inheritance;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

public class ConcreteOddTest extends OddBaseTest {

    @TestEngine.ArgumentSupplier
    protected static Stream<IntegerArgument> arguments() {
        return OddBaseTest.arguments();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + integerArgument + ")");
        assertThat((Integer) integerArgument.value() % 2).isOdd();
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + integerArgument + ")");
        assertThat((Integer) integerArgument.value() % 2).isOdd();
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }
}
