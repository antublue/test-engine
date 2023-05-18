package deprecated;

import org.antublue.test.engine.api.ObjectArgument;
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
    public ObjectArgument<String> objectArgument;

    @TestEngine.ParameterSupplier
    public static Stream<ObjectArgument<String>> parameters() throws IOException {
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
        System.out.println("test1(" + objectArgument.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + objectArgument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
