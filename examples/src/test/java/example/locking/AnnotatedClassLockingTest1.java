package example.locking;

import org.antublue.test.engine.api.Store;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.assertj.core.api.Fail.fail;

public class AnnotatedClassLockingTest1 {

    public static final String LOCK_NAME = "class.lock";
    public static final String COUNTER_NAME = "class.counter";

    static {
        Store.getOrCreate(COUNTER_NAME, name -> new AtomicInteger());
    }

    @TestEngine.Argument
    public IntegerArgument integerArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<IntegerArgument> arguments() {
        return Stream.of(
                IntegerArgument.of(1),
                IntegerArgument.of(2),
                IntegerArgument.of(3));
    }

    @TestEngine.Prepare
    @TestEngine.Lock(value=LOCK_NAME)
    public void prepare() {
        System.out.println(getClass().getName() + " prepare()");
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
    public void test1() throws InterruptedException {
        int count = Store.getOrCreate("COUNTER", name -> new AtomicInteger()).incrementAndGet();

        if (count != 1) {
            fail("expected count = 1");
        }

        System.out.println(getClass().getName() + " test1(" + integerArgument + ")");

        count = Store.getOrCreate("COUNTER", name -> new AtomicInteger()).decrementAndGet();
        if (count != 0) {
            fail("expected count = 0");
        }
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + integerArgument + ")");
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
    @TestEngine.Unlock(value=LOCK_NAME)
    public void conclude() throws InterruptedException {
        System.out.println(getClass().getName() + " conclude()");
    }
}