package example.nested;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

import java.util.stream.Stream;

/**
 * Example test
 */
public class NestedTest {

    public static class Concrete1 extends BaseTest {

        @Override
        protected void setup() {
            System.out.println("Concrete1.setup(" + integerArgument.value() + ")");
        }
    }

    public static class Concrete2 extends BaseTest {

        @Override
        protected void setup() {
            System.out.println("Concrete2.setup(" + integerArgument.value() + ")");
        }
    }

    @TestEngine.BaseClass
    public static abstract class BaseTest {

        @TestEngine.Argument
        protected IntegerArgument integerArgument;

        protected abstract void setup();

        @TestEngine.ArgumentSupplier
        public static Stream<IntegerArgument> arguments() {
            return Stream.of(
                    IntegerArgument.of(1),
                    IntegerArgument.of(2),
                    IntegerArgument.of(3));
        }

        @TestEngine.BeforeAll
        public void beforeAll() {
            System.out.println("beforeAll()");
            setup();
        }

        @TestEngine.BeforeEach
        public void beforeEach() {
            System.out.println("beforeEach()");
        }

        @TestEngine.Test
        public void test1() {
            System.out.println("test1(" + integerArgument.value() + ")");
        }

        @TestEngine.Test
        public void test2() {
            System.out.println("test2(" + integerArgument.value() + ")");
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
}
