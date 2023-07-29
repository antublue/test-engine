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

package org.antublue.test.engine.internal.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** Class to implement a named ThreadFactory */
public final class NamedThreadFactory implements ThreadFactory {

    private final AtomicInteger threadId = new AtomicInteger(1);

    private final String format;

    /**
     * Constructor
     *
     * @param format format
     */
    public NamedThreadFactory(String format) {
        this.format = format;
    }

    /**
     * Method to create a new Thread
     *
     * @param r a runnable to be executed by new thread instance
     * @return the Thread
     */
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(String.format(format, threadId.getAndIncrement()));
        thread.setDaemon(true);
        return thread;
    }
}
