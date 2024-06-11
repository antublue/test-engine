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

import java.util.concurrent.locks.ReentrantReadWriteLock;

public interface LockManager {

    /** Global Store namespace */
    String GLOBAL = "__GLOBAL__";

    /**
     * Method to get the LockManager namespace
     *
     * @return the LockManager namespace
     */
    String getNamespace();

    /**
     * Method to get a ReentrantReadWriteLock
     *
     * @param lockName lockName
     * @return a ReentrantReadWriteLock
     */
    ReentrantReadWriteLock getLock(String lockName);

    void executeInReadLock(String lockName, ReadLockExecutable readLockExecutable) throws Throwable;

    void executeInWriteLock(String lockName, WriteLockExecutable writeLockExecutable)
            throws Throwable;

    interface ReadLockExecutable {

        void inReadLock() throws Throwable;
    }

    interface WriteLockExecutable {

        void inWriteLock() throws Throwable;
    }
}
