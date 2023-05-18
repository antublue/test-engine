package example.nested;

import org.antublue.test.engine.api.ObjectArgument;
import org.antublue.test.engine.api.TestEngine;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
public class NestedTest {

    public static class Concrete1 extends BaseTest {

        @Override
        protected void setup() {
            System.out.println("Concrete1.setup(" + objectArgument.value() + ")");
        }
    }

    public static class Concrete2 extends BaseTest {

        @Override
        protected void setup() {
            System.out.println("Concrete2.setup(" + objectArgument.value() + ")");
        }
    }

    @TestEngine.BaseClass
    public static abstract class BaseTest {

        @TestEngine.Argument
        protected ObjectArgument<Integer> objectArgument;

        protected abstract void setup();

        @TestEngine.ArgumentSupplier
        public static Stream<ObjectArgument<Integer>> arguments() {
            return Stream.of(
                    ObjectArgument.of(1),
                    ObjectArgument.of(2),
                    ObjectArgument.of(3));
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
            System.out.println("test1(" + objectArgument.value() + ")");
            assertThat(objectArgument.value().getClass()).isEqualTo(Integer.class);
        }

        @TestEngine.Test
        public void test2() {
            System.out.println("test2(" + objectArgument.value() + ")");
            assertThat(objectArgument.value().getClass()).isEqualTo(Integer.class);
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
