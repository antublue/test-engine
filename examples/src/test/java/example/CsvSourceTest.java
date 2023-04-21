package example;

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.source.CsvSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Predicate;
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

    private String[] columns;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() throws IOException {
        Stream<Parameter> parameters;

        try (InputStream inputStream = CsvSourceTest.class.getResourceAsStream(RESOURCE_NAME)) {
            parameters = CsvSource.of(inputStream, StandardCharsets.UTF_8);
        }

        // Remove the header row
        return parameters.filter(parameter -> !parameter.name().equals("header"));
    }

    @TestEngine.Parameter
    public void parameter(Parameter parameter) {
        this.columns = parameter.value();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
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
