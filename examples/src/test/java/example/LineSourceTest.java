package example;

import org.antublue.test.engine.api.SimpleParameter;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.source.LineSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

/**
 * Example test
 */
public class LineSourceTest {

    private static final String RESOURCE_NAME = "/sample.txt";

    @TestEngine.Parameter
    public SimpleParameter<String> simpleParameter;

    @TestEngine.ParameterSupplier
    public static Stream<SimpleParameter<String>> parameters() throws IOException {
        try (InputStream inputStream = LineSourceTest.class.getResourceAsStream(RESOURCE_NAME)) {
            return LineSource.of(inputStream, StandardCharsets.UTF_8);
        }
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + simpleParameter.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + simpleParameter.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
