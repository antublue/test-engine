package example.conditions;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.Conditions;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

public class ConditionAwaitTest1 {

    @TestEngine.Argument
    public Argument<String> argument;

    @TestEngine.ArgumentSupplier
    public static Stream<Argument<String>> arguments() {
        Collection<Argument<String>> collection = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            collection.add(Argument.ofString("String " + i));
        }

        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        Conditions.await("ExampleCondition");
    }

    @TestEngine.Test
    public void test() throws Throwable {
        System.out.println("test(" + argument + ")");
    }
}
