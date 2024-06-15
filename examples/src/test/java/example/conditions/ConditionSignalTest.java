package example.conditions;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.Conditions;
import org.antublue.test.engine.api.TestEngine;

import java.util.stream.Stream;

public class ConditionSignalTest {

    @TestEngine.Argument
    public Argument<Boolean> argument;

    @TestEngine.ArgumentSupplier
    public static Stream<Argument<Boolean>> arguments() {
        return Stream.of(Argument.ofBoolean(true));
    }

    @TestEngine.Test
    public void test() throws Throwable {
        Thread.sleep(5000);
        Conditions.signal("ExampleCondition");
    }
}
