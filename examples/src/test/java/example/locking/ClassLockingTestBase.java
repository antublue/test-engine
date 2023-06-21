package example.locking;

import org.antublue.test.engine.api.support.Store;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClassLockingTestBase {

    private static final String LOCK_NAME = "class.lock";

    protected static int count;

    protected void lock() {
        Store.getOrCreate(LOCK_NAME, name -> new ReentrantReadWriteLock(true)).writeLock().lock();
    }

    protected void unlock() {
        Store.getOrCreate(LOCK_NAME, name -> new ReentrantReadWriteLock(true)).writeLock().unlock();
    }
}
