package org.antublue.test.engine.testing;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

import java.util.stream.Stream;

import static org.antublue.test.engine.api.ResourceLockMode.READ_WRITE;
import static org.assertj.core.api.Fail.fail;

@TestEngine.ResourceLock(value="LOCK_2", mode=READ_WRITE)
public class LockingTest1 extends LockingTestBaseClass {

    @TestEngine.Argument
    public IntegerArgument integerArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<IntegerArgument> arguments() {
        return Stream.of(
                IntegerArgument.of(1),
                IntegerArgument.of(2),
                IntegerArgument.of(3));
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
    @TestEngine.ResourceLock(value="LOCK_METHOD", mode=READ_WRITE)
    public void test1() throws InterruptedException {
        count++;
        if (count != 1) {
            fail("expected count = 1");
        }

        System.out.println(getClass().getName() + " test1(" + integerArgument.value() + ")");

        count--;
        if (count != 0) {
            fail("expected count = 0");
        }
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + integerArgument.value() + ")");
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
