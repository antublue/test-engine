package example;

import org.antublue.test.engine.api.Map;
import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ParameterMapTest2 {

    private Map map;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        Collection<Parameter> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(
                    Parameter.of(
                            "Map[" + i + "]",
                            new Map().put("key1", "value1")));
        }
        collection.add(Parameter.of("null value", new Map().put("null key", null)));
        return collection.stream();
    }

    @TestEngine.Parameter
    public void parameter(Parameter parameter) {
        map = parameter.value();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        String value = map.get("key1", String.class);
        System.out.println("test1(" + value + ")");
    }

    @TestEngine.Test
    public void test2() {
        String value = map.get("key1", String.class);
        System.out.println("test2(" + value + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
