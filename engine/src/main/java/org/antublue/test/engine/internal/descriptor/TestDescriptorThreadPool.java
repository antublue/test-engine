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

package org.antublue.test.engine.internal.descriptor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestDescriptorThreadPool {

    private final int threadCount;
    private final ExecutorService executorService;

    private TestDescriptorThreadPool() {
        threadCount = Math.max(Runtime.getRuntime().availableProcessors() - 2, 50);
        executorService = Executors.newFixedThreadPool(threadCount);
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void submit(Runnable runnable) {
        executorService.submit(runnable);
    }

    public static TestDescriptorThreadPool getInstance() {
        return SingletonHolder.SINGLETON;
    }

    private static class SingletonHolder {

        public static final TestDescriptorThreadPool SINGLETON = new TestDescriptorThreadPool();
    }
}
