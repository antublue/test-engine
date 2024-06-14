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

package org.antublue.test.engine.api.support.lock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** Class to implement LockManager */
public class LockManager {

    private final ConcurrentHashMap<String, InternalLockReference> lockMap;

    /** Constructor */
    public LockManager() {
        lockMap = new ConcurrentHashMap<>();
    }

    /**
     * Acquires the lock for the given name. If the lock does not exist, it will be created.
     *
     * @param name the name of the lock
     */
    public LockReference acquire(String name) {
        InternalLockReference lockRef =
                lockMap.compute(
                        name,
                        (k, existingLockRef) -> {
                            if (existingLockRef == null) {
                                return new InternalLockReference(k);
                            } else {
                                existingLockRef.incrementCount();
                                return existingLockRef;
                            }
                        });

        lockRef.lock();
        return new LockReference(lockRef);
    }

    /**
     * Method to execute an Executable in a lock
     *
     * @param name the name of the lock
     * @param executable the executable
     * @throws Throwable Throwable
     */
    public void executeInLock(String name, Executable executable) throws Throwable {
        LockReference lockReference = acquire(name);
        try {
            executable.execute();
        } finally {
            lockReference.release();
        }
    }

    /** Class to implement LockReference */
    class InternalLockReference {
        private final Lock lock;
        private final AtomicInteger count;
        private final String name;

        /**
         * Constructor
         *
         * @param name name
         */
        InternalLockReference(String name) {
            this.lock = new ReentrantLock();
            this.count = new AtomicInteger(1);
            this.name = name;
        }

        /** Method to lock the Lock */
        void lock() {
            lock.lock();
        }

        /** Method to unlock the Lock */
        void unlock() {
            lock.unlock();
        }

        /** Method to increase the reference count */
        void incrementCount() {
            count.incrementAndGet();
        }

        /**
         * Method to release the lock. If the reference count reaches zero, the lock will be removed
         * from the LockManager.
         */
        public void release() {
            unlock();
            if (count.decrementAndGet() == 0) {
                lockMap.remove(name);
            }
        }
    }
}
