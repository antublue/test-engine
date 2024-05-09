/*
 * Copyright (C) 2023 The AntuBLUE test-engine project authors
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** Class to implement a LockManager */
public class LockManager {

    private static LockManager INSTANCE;

    private final ReentrantLock reentrantLock;
    private final Map<String, ReentrantReadWriteLock> reentrantReadWriteLockMap;

    /** Constructor */
    private LockManager() {
        reentrantLock = new ReentrantLock(true);
        reentrantReadWriteLockMap = new HashMap<>();
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static synchronized LockManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LockManager();
        }
        return INSTANCE;
    }

    /**
     * Method to get a ReentrantReadWriteLock. Creates the ReentrantReadWriteLock if it doesn't
     * exist.
     *
     * @param name name
     * @return a ReentrantReadWriteLock
     */
    public ReentrantReadWriteLock getLock(String name) {
        ReentrantReadWriteLock reentrantReadWriteLock;

        try {
            reentrantLock.lock();
            reentrantReadWriteLock = reentrantReadWriteLockMap.get(name);
            if (reentrantReadWriteLock == null) {
                reentrantReadWriteLock = new ReentrantReadWriteLock(true);
                reentrantReadWriteLockMap.put(name, reentrantReadWriteLock);
            }
        } finally {
            reentrantLock.unlock();
        }

        return reentrantReadWriteLock;
    }
}
