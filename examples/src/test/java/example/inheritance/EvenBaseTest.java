package example.inheritance;

import org.antublue.test.engine.api.argument.IntegerArgument;

import java.util.stream.Stream;

public abstract class EvenBaseTest extends BaseTest {

    protected static Stream<IntegerArgument> arguments() {
        return BaseTest.arguments()
                .filter(
                        argument -> {
                            int value = argument.value();
                            return (value % 2) == 0;
                        });
    }
}
