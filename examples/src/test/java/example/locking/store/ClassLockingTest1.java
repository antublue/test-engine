package example.locking.store;

import org.antublue.test.engine.api.Store;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Fail.fail;

public class ClassLockingTest1 {

    public static final String LOCK_NAME = "class.lock";
    public static final String COUNTER_NAME = "class.counter";

    static {
        Store.computeIfAbsent(COUNTER_NAME, name -> new AtomicInteger());
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
        Store.computeIfAbsent(LOCK_NAME, name -> new ReentrantLock(true)).lock();
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
    public void test1() throws InterruptedException {
        System.out.println("test1()");

        int count = Store.computeIfAbsent(COUNTER_NAME, name -> new AtomicInteger()).incrementAndGet();

        if (count != 1) {
            fail("expected count = 1");
        }

        if (Store.get(COUNTER_NAME, AtomicInteger.class).get().decrementAndGet() != 0) {
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
    public void conclude() {
        System.out.println("conclude()");
        Store.get(LOCK_NAME, ReentrantLock.class).ifPresent(reentrantLock -> reentrantLock.unlock());
    }
}
