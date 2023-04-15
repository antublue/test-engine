package example.inheritance2;

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

@TestEngine.BaseClass
public class BaseTest {

    @TestEngine.Parameter
    protected Parameter parameter;

    @TestEngine.ParameterSupplier
    protected static Stream<Parameter> parameters() {
        Collection<Parameter> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(Parameter.of("Array [" + i + "]", i));
        }

        return collection.stream();
    }

    @TestEngine.BeforeClass
    protected static void _beforeClass() {
        System.out.println("_beforeClass()");
    }

    @TestEngine.BeforeClass
    protected static void beforeClass() {
        System.out.println("beforeClass()");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void testA() {
        System.out.println("testA()");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterClass
    protected static void afterClass() {
        System.out.println("afterClass()");
    }

    @TestEngine.AfterClass
    protected static void afterClass_() {
        System.out.println("afterClass_()");
    }
}
