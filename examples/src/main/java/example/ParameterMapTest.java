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
public class ParameterMapTest {

    private Map map;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        Collection<Parameter> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(
                    Parameter.of(
                            "Map[" + i + "]",
                            new Map().put("key", i)));
        }
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
        int value = map.get("key");
        System.out.println("test1(" + value + ")");
    }

    @TestEngine.Test
    public void test2() {
        int value = map.get("key");
        System.out.println("test2(" + value + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
