package deprecated.example.inheritance;

import org.antublue.test.engine.api.SimpleParameter;

import java.util.stream.Stream;

public abstract class EvenBaseTest extends BaseTest {

    protected static Stream<SimpleParameter<Integer>> parameters() {
        return BaseTest
                .parameters()
                .filter(parameter -> {
                    int value = parameter.value();
                    return (value % 2) == 0;
                });
    }
}
