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

package org.antublue.test.engine.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.antublue.test.engine.api.LockManager;

public class LockManagerImpl implements LockManager {

    private final String namespace;
    private final Map<String, ReentrantReadWriteLock> lockMap;

    public LockManagerImpl(String namespace) {
        this.namespace = namespace;
        this.lockMap = new ConcurrentHashMap<>();
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public ReentrantReadWriteLock getLock(String lockName) {
        return lockMap.computeIfAbsent(lockName, k -> new ReentrantReadWriteLock(true));
    }

    @Override
    public void executeInReadLock(String lockName, ReadLockExecutable readLockExecutable)
            throws Throwable {
        ReentrantReadWriteLock reentrantReadWriteLock = getLock(lockName);

        try {
            reentrantReadWriteLock.readLock().lock();
            readLockExecutable.withReadLock();
        } finally {
            reentrantReadWriteLock.readLock().unlock();
        }
    }

    @Override
    public void executeInWriteLock(String lockName, WriteLockExecutable writeLockExecutable)
            throws Throwable {
        ReentrantReadWriteLock reentrantReadWriteLock = getLock(lockName);

        try {
            reentrantReadWriteLock.writeLock().lock();
            writeLockExecutable.withWriteLock();
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }
}
