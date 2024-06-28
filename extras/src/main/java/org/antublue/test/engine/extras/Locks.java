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

package org.antublue.test.engine.extras;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** Class to implement Locks */
public class Locks {

    private static final LockManager LOCK_MANAGER = new LockManager();

    /** Constructor */
    private Locks() {
        // DO NOTHING
    }

    /**
     * Method to get a LockReference
     *
     * @param key key
     * @return a LockReference
     */
    public static LockReference getReference(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        return new LockReference(LOCK_MANAGER, key);
    }

    /** Class to implement LockReference */
    public static class LockReference {

        private final LockManager lockManager;
        private final Object name;

        /**
         * Constructor
         *
         * @param name name
         */
        private LockReference(LockManager lockManager, Object name) {
            this.lockManager = lockManager;
            this.name = name;
        }

        /** Method to lock */
        public void lock() {
            lockManager.acquire(name);
        }

        /** Method to unlock */
        public void unlock() {
            lockManager.release(name);
        }

        @Override
        public String toString() {
            return name.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LockReference that = (LockReference) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }
    }

    /**
     * Method to execute an Executable in a lock
     *
     * @param key key
     * @param executable executable
     * @throws Throwable Throwable
     */
    public static void execute(Object key, Executable executable) throws Throwable {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        if (executable == null) {
            throw new IllegalArgumentException("executable is null");
        }

        LockReference lockReference = getReference(key);

        try {
            lockReference.lock();
            executable.execute();
        } finally {
            lockReference.unlock();
        }
    }

    /** Class to implement LockManager */
    private static class LockManager {

        private final Lock LOCK = new ReentrantLock(true);
        private final Map<Object, LockHolder> MAP = new HashMap<>();

        /**
         * Method to acquire the Lock
         *
         * @param key key
         */
        void acquire(Object key) {
            LockHolder lockHolder;
            try {
                LOCK.lock();

                lockHolder =
                        MAP.compute(
                                key,
                                (k, lh) -> {
                                    if (lh == null) {
                                        lh = new LockHolder();
                                    }
                                    return lh;
                                });

                lockHolder.increaseLockCount();
            } finally {
                LOCK.unlock();
            }

            lockHolder.getLock().lock();
        }

        /**
         * Method to release the Lock
         *
         * @param key key
         */
        void release(Object key) {
            try {
                LOCK.lock();

                LockHolder lockHolder = MAP.get(key);
                if (lockHolder == null) {
                    throw new IllegalMonitorStateException(
                            format("LockReference [%s] not locked", key));
                }

                if (lockHolder.getLockCount() == 0) {
                    throw new IllegalMonitorStateException(
                            format("LockReference [%s] already unlocked", key));
                }

                lockHolder.getLock().unlock();
                lockHolder.decreaseLockCount();

                if (lockHolder.getLockCount() == 0) {
                    MAP.remove(key);
                }
            } finally {
                LOCK.unlock();
            }
        }
    }

    /** Class to implement LockHolder */
    private static class LockHolder {

        private final ReentrantLock lock;
        private int lockCount;

        /** Constructor */
        LockHolder() {
            lock = new ReentrantLock(true);
        }

        /**
         * Method to get the Lock
         *
         * @return the Lock
         */
        Lock getLock() {
            return lock;
        }

        /** Method to increase lock count */
        void increaseLockCount() {
            lockCount++;
        }

        /** Method to decrease lock count */
        void decreaseLockCount() {
            lockCount--;
        }

        /**
         * Method to get the lock count
         *
         * @return the lock count
         */
        int getLockCount() {
            return lockCount;
        }
    }
}
