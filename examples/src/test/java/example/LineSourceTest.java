package example;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;
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

    @TestEngine.Argument
    public StringArgument stringArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<StringArgument> arguments() throws IOException {
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
        System.out.println("test1(" + stringArgument  + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + stringArgument  + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
