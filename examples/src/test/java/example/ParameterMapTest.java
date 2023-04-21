package example;

import org.antublue.test.engine.api.Map;
import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.ParameterMap;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ParameterMapTest {

    private ParameterMap parameterMap;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        Collection<Parameter> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(
                    ParameterMap
                            .named("Map[" + i + "]")
                            .put("key", i)
                            .parameter());
        }
        return collection.stream();
    }

    @TestEngine.Parameter
    public void parameter(Parameter parameter) {
        parameterMap = parameter.value();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        int value = parameterMap.get("key");
        System.out.println("test1(" + value + ")");
    }

    @TestEngine.Test
    public void test2() {
        int value = parameterMap.get("key");
        System.out.println("test2(" + value + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
