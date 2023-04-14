package example;

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
public class ParameterOfCharTest {

    private Parameter parameter;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        Collection<Parameter> collection = new ArrayList<>();
        collection.add(Parameter.of('a'));
        collection.add(Parameter.of('b'));
        return collection.stream();
    }

    @TestEngine.Parameter
    public void parameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + parameter.value() + ")");
        if (parameter.value(Character.class) != null) {
            assertThat(parameter.value(Character.class).getClass()).isEqualTo(Character.class);
        }
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
