package example.inheritance;

import org.antublue.test.engine.api.Parameter;

import java.util.stream.Stream;

public abstract class OddBaseTest extends BaseTest {

    protected static Stream<Parameter> parameters() {
        return BaseTest
                .parameters()
                .filter(parameter -> {
                    int value = parameter.value();
                    return (value % 2) != 0;
                });
    }
}
