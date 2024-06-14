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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/** Class to implement a LockReference */
public class Locks {

    private static final Map<Object, ReentrantLock> LOCKS = new ConcurrentHashMap<>();

    /** Constructor */
    private Locks() {
        // DO NOTHING
    }

    /**
     * Method to get a ReentrantLock
     *
     * @param name name
     * @return a ReentrantLock
     */
    public static ReentrantLock get(Object name) {
        return LOCKS.computeIfAbsent(name, k -> new ReentrantLock(true));
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

        ReentrantLock reentrantLock = Locks.get(name);

        try {
            reentrantLock.lock();
            executable.execute();
        } finally {
            reentrantLock.unlock();
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
