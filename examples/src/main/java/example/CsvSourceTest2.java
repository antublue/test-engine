package example;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.ArgumentMap;
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

    private ArgumentMap argumentMap;

    @TestEngine.Arguments
    public static Stream<Argument> arguments() throws IOException {
        try (InputStream inputStream = CsvSourceTest2.class.getResourceAsStream(RESOURCE_NAME)) {
            return CsvSource.of(inputStream, StandardCharsets.UTF_8);
        }
    }

    @TestEngine.Argument
    public void argument(Argument argument) {
        this.argumentMap = argument.value();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + argumentMap.get("First Name") + " " + argumentMap.get("Last Name") + ")");

        Set<Map.Entry<String, Object>> entrySet = argumentMap.entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            System.out.println("entry [" + entry.getKey() + "] = [" + entry.getValue() + "]");
        }
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argumentMap.get("Email") + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
