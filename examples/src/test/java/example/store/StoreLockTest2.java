package example.store;

import org.antublue.test.engine.api.Store;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Fail.fail;

public class StoreLockTest2 {

    public static final String LOCK_NAME = "store.lock";
    public static final String COUNTER_NAME = "store.counter";

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
        try {
            Store.getLock().lock();

            System.out.println(getClass().getName() + " test1(" + integerArgument + ")");

            Store.put(COUNTER_NAME, 2);

            if (Store.get(COUNTER_NAME, Integer.class).get() != 2) {
                fail("expected count = 2");
            }

            Store.remove(COUNTER_NAME);
        } finally {
            Store.getLock().unlock();
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
    public void conclude() {
        System.out.println(getClass().getName() + " conclude()");
    }
}
