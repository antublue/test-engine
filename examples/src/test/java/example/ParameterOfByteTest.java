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
public class ParameterOfByteTest {

    private Parameter parameter;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        Collection<Parameter> collection = new ArrayList<>();
        collection.add(Parameter.of((byte) 1));
        collection.add(Parameter.of((byte) 2));
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
        assertThat(parameter.value(Byte.class).getClass()).isEqualTo(Byte.class);
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
