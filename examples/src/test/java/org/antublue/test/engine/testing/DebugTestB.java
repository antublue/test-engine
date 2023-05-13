package org.antublue.test.engine.testing;

import org.antublue.test.engine.api.SimpleParameter;
import org.antublue.test.engine.api.TestEngine;
import org.opentest4j.AssertionFailedError;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test used for debugging IntelliJ
 */
@TestEngine.Disabled
public class DebugTestB {

    @TestEngine.Parameter
    public SimpleParameter<String> simpleParameter;

    @TestEngine.ParameterSupplier
    public static Stream<SimpleParameter<String>> parameters() {
        return Stream.of(
                SimpleParameter.of("a"),
                SimpleParameter.of("b"),
                SimpleParameter.of("c"));
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + simpleParameter.value() + ")");
        assertThat(simpleParameter.value().getClass()).isEqualTo(String.class);
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + simpleParameter.value() + ")");
        assertThat(simpleParameter.value().getClass()).isEqualTo(String.class);
        if (simpleParameter.value().equals("b")) {
            throw new AssertionFailedError("Forced");
        }
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
