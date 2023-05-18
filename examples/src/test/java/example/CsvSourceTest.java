package example;

import org.antublue.test.engine.api.ObjectArgument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.source.CsvSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
public class CsvSourceTest {

    private static final String RESOURCE_NAME = "/sample.csv";
    private static final int FIRST_NAME = 2;
    private static final int LAST_NAME = 3;
    private static final int EMAIL_ADDRESS = 9;

    @TestEngine.Argument
    public ObjectArgument<String[]> objectArgument;

    private String[] columns;

    @TestEngine.ArgumentSupplier
    public static Stream<ObjectArgument<String[]>> arguments() throws IOException {
        Stream<ObjectArgument<String[]>> arguments;

        try (InputStream inputStream = CsvSourceTest.class.getResourceAsStream(RESOURCE_NAME)) {
            arguments = CsvSource.of(inputStream, StandardCharsets.UTF_8);
        }

        // Remove the header row
        return arguments.filter(argument -> !argument.name().equals("header"));
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
        columns = objectArgument.value();
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + columns[FIRST_NAME] + " " + columns[LAST_NAME] + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + columns[EMAIL_ADDRESS] + ")");
        assertThat(columns[EMAIL_ADDRESS]).contains("@");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
