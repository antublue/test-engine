package example.parameter.supplier;

import org.antublue.test.engine.api.SimpleParameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
public class CollectionTest {

    @TestEngine.Parameter
    private SimpleParameter<Integer> simpleParameter;

    @TestEngine.ParameterSupplier
    public static Collection<SimpleParameter<Integer>> parameters() {
        ArrayList<SimpleParameter<Integer>> parameters = new ArrayList<>();
        parameters.add(SimpleParameter.of(1));
        parameters.add(SimpleParameter.of(2));
        parameters.add(SimpleParameter.of(3));
        return parameters;
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
