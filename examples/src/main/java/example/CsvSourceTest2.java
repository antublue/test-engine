package example;

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.ParameterMap;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.source.CsvSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Example test
 */
public class CsvSourceTest2 {

    private static final String RESOURCE_NAME = "/sample.missing-headers.csv";

    private ParameterMap parameterMap;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() throws IOException {
        try (InputStream inputStream = CsvSourceTest2.class.getResourceAsStream(RESOURCE_NAME)) {
            return CsvSource.of(inputStream, StandardCharsets.UTF_8);
        }
    }

    @TestEngine.ParameterSetter
    public void setParameter(Parameter parameter) {
        this.parameterMap = parameter.value();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + parameterMap.get("First Name") + " " + parameterMap.get("Last Name") + ")");

        Set<Map.Entry<String, Object>> entrySet = parameterMap.entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            System.out.println("entry [" + entry.getKey() + "] = [" + entry.getValue() + "]");
        }
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + parameterMap.get("Email") + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
