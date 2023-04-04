package example;

import org.antublue.test.engine.api.Argument;
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

    private Argument argument;

    @TestEngine.Arguments
    public static Stream<Argument> arguments() throws IOException {
        try (InputStream inputStream = LineSourceTest2.class.getResourceAsStream(RESOURCE_NAME)) {
            return LineSource.of(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        }
    }

    @TestEngine.Argument
    public void argument(Argument argument) {
        this.argument = argument;
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + argument.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
