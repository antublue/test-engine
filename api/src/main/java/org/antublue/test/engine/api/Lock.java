/*
 * Copyright (C) 2024 The AntuBLUE test-engine project authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.antublue.test.engine.api;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/** Class to implement a LockReference */
public class Lock {

    private static final LockManager LOCK_MANAGER = new LockManager();

    private final LockManager.LockDelegate lockDelegate;

    /**
     * Constructor
     *
     * @param lockDelegate lockDelegate
     */
    Lock(LockManager.LockDelegate lockDelegate) {
        this.lockDelegate = lockDelegate;
    }

    /**
     * Method to lock the Lock
     *
     * @return the Lock
     */
    public Lock lock() {
        lockDelegate.lock();
        return this;
    }

    /**
     * Method to unlock the Lock
     *
     * @return the Lock
     */
    public Lock unlock() {
        lockDelegate.unlock();
        return this;
    }

    /** Method to unlock and remove the Lock if not being used */
    public void release() {
        lockDelegate.release();
    }

    /**
     * Method to get a Lock
     *
     * @param namespace namespace
     * @return a Lock
     */
    public static Lock get(Object namespace) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace is null");
        }
        return LOCK_MANAGER.get(namespace);
    }

    /**
     * Method to execute an Executable in a Lock
     *
     * @param namespace namespace
     * @param executable executable
     * @throws Throwable Throwable
     */
    public static void execute(Object namespace, Executable executable) throws Throwable {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace is null");
        }

        if (executable == null) {
            throw new IllegalArgumentException("executable is null");
        }

        Lock lock = get(namespace);

        try {
            lock.lock();
            executable.execute();
        } finally {
            lock.release();
        }
    }

    /** Class to implement LockManager */
    private static class LockManager {

        private final ConcurrentHashMap<Object, LockDelegate> lockMap;

        /** Constructor */
        public LockManager() {
            lockMap = new ConcurrentHashMap<>();
        }

        /**
         * Method to get a Lock
         *
         * @param namespace namespace
         * @return a Lock
         */
        public Lock get(Object namespace) {
            LockDelegate lockDelegate =
                    lockMap.compute(
                            namespace,
                            (o, existingLockRef) -> {
                                if (existingLockRef == null) {
                                    return new LockDelegate(o);
                                } else {
                                    existingLockRef.incrementCount();
                                    return existingLockRef;
                                }
                            });

            return new Lock(lockDelegate);
        }

        /** Class to implement LockDelegate */
        class LockDelegate {

            private final java.util.concurrent.locks.Lock lock;
            private final AtomicInteger count;
            private final Object namespace;

            /**
             * Constructor
             *
             * @param namespace namespace
             */
            LockDelegate(Object namespace) {
                this.lock = new ReentrantLock();
                this.count = new AtomicInteger(1);
                this.namespace = namespace;
            }

            /** Method to increase the reference count */
            void incrementCount() {
                count.incrementAndGet();
            }

            /** Method to lock the Lock */
            public void lock() {
                lock.lock();
            }

            /** Method to unlock the Lock */
            public void unlock() {
                lock.unlock();
            }

            /**
             * Method to release the lock. If the reference count reaches zero, the lock will be
             * removed from the LockManager.
             */
            public void release() {
                unlock();
                if (count.decrementAndGet() == 0) {
                    lockMap.remove(namespace);
                }
            }
        }
    }

    /** Interface to implement an Executable */
    public interface Executable {

        /**
         * Method to execute
         *
         * @throws Throwable Throwable
         */
        void execute() throws Throwable;
    }
}
