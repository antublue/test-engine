package example;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;
import org.antublue.test.engine.api.source.LineSource;

/** Example test */
public class LineSourceTest2 {

    private static final String RESOURCE_NAME = "/sample.txt";

    @TestEngine.Argument public StringArgument stringArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<StringArgument> arguments() throws IOException {
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
        System.out.println("test1(" + stringArgument + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + stringArgument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
