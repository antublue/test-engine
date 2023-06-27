package example.locking;

import org.antublue.test.engine.api.Store;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.assertj.core.api.Fail.fail;

public class MultipleMethodsLockingTest2 {

    public static final String LOCK_NAME = "multiple.methods.lock";
    public static final String COUNTER_NAME = "multiple.methods.counter";

    static {
        Store.getOrCreate(COUNTER_NAME, namw -> new AtomicInteger());
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
    public void prepare() {
        System.out.println("prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        Store.getOrCreate(LOCK_NAME, name -> new ReentrantLock(true)).lock();
        System.out.println(getClass().getName() + " beforeAll()");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1() throws InterruptedException {
        int count = Store.getOrCreate(COUNTER_NAME, name -> new AtomicInteger()).incrementAndGet();
        if (count != 1) {
            fail("expected count = 1");
        }

        System.out.println(getClass().getName() + " test1(" + integerArgument.value() + ")");

        count = Store.getOrCreate(COUNTER_NAME, name -> new AtomicInteger()).decrementAndGet();
        if (count != 0) {
            fail("expected count = 0");
        }
    }

    @TestEngine.Test
    public void test2() {
        int count = Store.getOrCreate(COUNTER_NAME, name -> new AtomicInteger()).incrementAndGet();
        if (count != 1) {
            fail("expected count = 1");
        }

        System.out.println(getClass().getName() + " test1(" + integerArgument.value() + ")");

        count = Store.getOrCreate(COUNTER_NAME, name -> new AtomicInteger()).decrementAndGet();
        if (count != 0) {
            fail("expected count = 0");
        }
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println(getClass().getName() + " afterAll()");
        Store.get(LOCK_NAME, ReentrantLock.class).unlock();
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
    }
}
