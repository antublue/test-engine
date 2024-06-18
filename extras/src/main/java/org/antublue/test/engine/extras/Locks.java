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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/** Class to implement a LockReference */
public class Locks {

    private static final LockManager LOCK_MANAGER = new LockManager();

    public static LockReference getReference(Object name) {
        return new LockReference(name);
    }

    private static class LockManager {

        private final Map<Object, ReferenceCountingReentrantLock> MAP = new ConcurrentHashMap<>();

        private void lock(Object name) {
            ReferenceCountingReentrantLock referenceCountingReentrantLock =
                    MAP.compute(
                            name,
                            (o, referenceCountingReentrantLock1) -> {
                                if (referenceCountingReentrantLock1 == null) {
                                    referenceCountingReentrantLock1 =
                                            new ReferenceCountingReentrantLock();
                                } else {
                                    referenceCountingReentrantLock1.incrementCount();
                                }
                                return referenceCountingReentrantLock1;
                            });

            referenceCountingReentrantLock.lock();
        }

        private void unlock(Object name) {
            try {
                ReferenceCountingReentrantLock referenceCountingReentrantLock =
                        MAP.compute(
                                name,
                                (o, referenceCountingReentrantLock1) -> {
                                    if (referenceCountingReentrantLock1 == null) {
                                        throw new IllegalStateException(
                                                "lock [" + name + "] is not locked");
                                    } else {
                                        referenceCountingReentrantLock1.decrementCount();
                                    }
                                    return referenceCountingReentrantLock1;
                                });

                referenceCountingReentrantLock.unlock();

                if (referenceCountingReentrantLock.getCount() == 0) {
                    MAP.remove(name);
                }
            } catch (IllegalStateException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
    }

    public static class LockReference {

        private final Object name;

        private LockReference(Object name) {
            this.name = name;
        }

        public void lock() {
            LOCK_MANAGER.lock(name);
        }

        public void unlock() {
            LOCK_MANAGER.unlock(name);
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
     * Method to execute an Executable in a ReentrantLock
     *
     * @param name name
     * @param executable executable
     * @throws Throwable Throwable
     */
    public static void execute(Object name, Executable executable) throws Throwable {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }

        if (executable == null) {
            throw new IllegalArgumentException("executable is null");
        }

        LockReference lockReference = getReference(name);

        try {
            lockReference.lock();
            executable.execute();
        } finally {
            lockReference.unlock();
        }
    }

    static class ReferenceCountingReentrantLock extends ReentrantLock {

        private int count;

        public ReferenceCountingReentrantLock() {
            super(true);
            count = 1;
        }

        public void incrementCount() {
            count++;
        }

        public void decrementCount() {
            count--;
        }

        public int getCount() {
            return count;
        }
    }
}
