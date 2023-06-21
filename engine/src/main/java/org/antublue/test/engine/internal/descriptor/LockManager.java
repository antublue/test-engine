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

package org.antublue.test.engine.internal.descriptor;

import org.antublue.test.engine.internal.util.Precondition;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class to implement a lock manager
 */
class LockManager {

    private static final Map<String, ReentrantReadWriteLock> reentrantReadWriteLockCache;

    static {
        reentrantReadWriteLockCache = new HashMap<>();
    }

    /**
     * Constructor
     */
    private LockManager() {
        // DO NOTHING
    }

    /**
     * Method to get a ReentrantLock by name
     *
     * @param name name
     * @return the ReentrantReadWriteLock
     */
    static ReentrantReadWriteLock getLock(String name) {
        Precondition.notBlank(name, "name is blank");

        ReentrantReadWriteLock reentrantReadWriteLock;

        synchronized (reentrantReadWriteLockCache) {
            reentrantReadWriteLock = reentrantReadWriteLockCache.get(name);
            if (reentrantReadWriteLock == null) {
                reentrantReadWriteLock = new ReentrantReadWriteLock(true);
                reentrantReadWriteLockCache.put(name, reentrantReadWriteLock);
            }
        }

        return reentrantReadWriteLock;
    }
}
