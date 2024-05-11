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

import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.antublue.test.engine.api.Store;

/** Class to implement a LockManager */
public class LockManager {

    private final Store store;

    /** Constructor */
    private LockManager() {
        store = new Store();
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static LockManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Method to get a ReentrantReadWriteLock. Creates the ReentrantReadWriteLock if it doesn't
     * exist.
     *
     * @param key key
     * @return a ReentrantReadWriteLock
     */
    public ReentrantReadWriteLock getLock(String key) {
        key = checkKey(key);
        return (ReentrantReadWriteLock)
                store.putIfAbsent(key, s -> new ReentrantReadWriteLock(true)).get();
    }

    /**
     * Method to validate a key is not null and not blank
     *
     * @param key key
     * @return the key trimmed
     */
    private static String checkKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("key is empty");
        }

        return key.trim();
    }

    /** Class to hold the singleton instance */
    private static final class SingletonHolder {

        /** The singleton instance */
        private static final LockManager INSTANCE = new LockManager();
    }
}
