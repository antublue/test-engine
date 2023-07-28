package example.locking;

import static org.assertj.core.api.Fail.fail;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Store;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

public class ClassLockingTest2 {

    public static final String PREFIX = "ClassLockingTest";
    public static final String LOCK_NAME = PREFIX + ".lock";
    public static final String COUNTER_NAME = PREFIX + ".counter";

    static {
        Store.singleton().putIfAbsent(COUNTER_NAME, k -> new AtomicInteger());
    }

    @TestEngine.Argument public IntegerArgument integerArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<IntegerArgument> arguments() {
        return Stream.of(IntegerArgument.of(1), IntegerArgument.of(2), IntegerArgument.of(3));
    }

    @TestEngine.Prepare
    @TestEngine.Lock(name = LOCK_NAME)
    public void prepare() {
        System.out.println("prepare()");
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
        System.out.println("test1()");

        AtomicInteger atomicInteger =
                Store.singleton().get(COUNTER_NAME, AtomicInteger.class).get();

        int count = atomicInteger.incrementAndGet();
        if (count != 1) {
            fail("expected count = 1");
        }

        count = atomicInteger.decrementAndGet();
        if (count != 0) {
            fail("expected count = 0");
        }
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2()");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    @TestEngine.Conclude
    @TestEngine.Unlock(name = LOCK_NAME)
    public void conclude() {
        System.out.println("conclude()");
    }
}
