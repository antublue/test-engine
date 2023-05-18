package deprecated;

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.SimpleParameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
@TestEngine.Disabled
public class DisabledTest {

    @TestEngine.Parameter
    private Parameter parameter;

    @TestEngine.ParameterSupplier
    public static Stream<SimpleParameter<String>> parameters() {
        return StringParameterSupplier.parameters();
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
    public void test1() {
        System.out.println("test1(" + parameter + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + parameter + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    private static class StringParameterSupplier {

        public static Stream<SimpleParameter<String>> parameters() {
            Collection<SimpleParameter<String>> collection = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                collection.add(SimpleParameter.of(String.valueOf(i)));
            }
            return collection.stream();
        }
    }
}
