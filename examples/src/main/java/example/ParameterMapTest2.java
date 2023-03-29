package example;

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.ParameterMap;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ParameterMapTest2 {

    private ParameterMap parameterMap;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        Collection<Parameter> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(
                    Parameter.of(
                            "ParameterMap[" + i + "]",
                            new ParameterMap().put("key1", "value1")));
        }
        collection.add(Parameter.of("null value", new ParameterMap().put("null key", null)));
        return collection.stream();
    }

    @TestEngine.ParameterSetter
    public void setParameter(Parameter parameter) {
        parameterMap = parameter.value();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        String value = parameterMap.get("key1", String.class);
        System.out.println("test1(" + value + ")");
    }

    @TestEngine.Test
    public void test2() {
        String value = parameterMap.get("key1", String.class);
        System.out.println("test2(" + value + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
