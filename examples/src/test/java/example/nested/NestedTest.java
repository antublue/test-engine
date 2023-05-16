package example.nested;

import org.antublue.test.engine.api.SimpleParameter;
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
            System.out.println("Concrete1.setup(" + simpleParameter.value() + ")");
        }
    }

    public static class Concrete2 extends BaseTest {

        @Override
        protected void setup() {
            System.out.println("Concrete2.setup(" + simpleParameter.value() + ")");
        }
    }

    @TestEngine.BaseClass
    public static abstract class BaseTest {

        @TestEngine.Parameter
        protected SimpleParameter<Integer> simpleParameter;

        protected abstract void setup();

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
            setup();
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
}
