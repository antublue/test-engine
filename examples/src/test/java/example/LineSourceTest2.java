package example;

import org.antublue.test.engine.api.ObjectArgument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.source.LineSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

/**
 * Example test
 */
public class LineSourceTest2 {

    private static final String RESOURCE_NAME = "/sample.txt";

    @TestEngine.Argument
    public ObjectArgument<String> objectArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<ObjectArgument<String>> arguments() throws IOException {
        try (InputStream inputStream = LineSourceTest2.class.getResourceAsStream(RESOURCE_NAME)) {
            return LineSource.of(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
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
