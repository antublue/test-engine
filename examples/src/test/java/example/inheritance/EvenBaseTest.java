package example.inheritance;

import java.util.stream.Stream;
import org.antublue.test.engine.api.argument.IntegerArgument;

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
