package example.inheritance;

import org.antublue.test.engine.api.ObjectArgument;
import org.antublue.test.engine.api.TestEngine;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcreteOddTest extends OddBaseTest {

    @TestEngine.ArgumentSupplier
    protected static Stream<ObjectArgument<Integer>> arguments() {
        return OddBaseTest.arguments();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + objectArgument + ")");
        assertThat((Integer) objectArgument.value() % 2).isOdd();
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + objectArgument + ")");
        assertThat((Integer) objectArgument.value() % 2).isOdd();
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }
}
