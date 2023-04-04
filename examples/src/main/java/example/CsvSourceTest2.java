package example;

import org.antublue.test.engine.api.Map;
import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.source.CsvSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Example test
 */
public class CsvSourceTest2 {

    private static final String RESOURCE_NAME = "/sample.missing-headers.csv";

    private Map map;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() throws IOException {
        try (InputStream inputStream = CsvSourceTest2.class.getResourceAsStream(RESOURCE_NAME)) {
            return CsvSource.of(inputStream, StandardCharsets.UTF_8);
        }
    }

    @TestEngine.Parameter
    public void parameter(Parameter parameter) {
        this.map = parameter.value();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + map.get("First Name") + " " + map.get("Last Name") + ")");

        Set<java.util.Map.Entry<String, Object>> entrySet = map.entrySet();
        for (java.util.Map.Entry<String, Object> entry : entrySet) {
            System.out.println("entry [" + entry.getKey() + "] = [" + entry.getValue() + "]");
        }
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + map.get("Email") + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
