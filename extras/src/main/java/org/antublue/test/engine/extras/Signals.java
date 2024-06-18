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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/** Class to implement Conditions */
public class Signals {

    private static final ReentrantLock LOCK = new ReentrantLock(true);
    private static final Map<Object, LinkedBlockingQueue<Boolean>> MAP = new HashMap<>();

    /** Constructor */
    private Signals() {
        // DO NOTHING
    }

    /**
     * Method to signal a Condition
     *
     * @param name name
     */
    public static void signal(Object name) {
        LinkedBlockingQueue<Boolean> linkedBlockingQueue;

        try {
            LOCK.lock();
            linkedBlockingQueue = MAP.get(name);
            if (linkedBlockingQueue == null) {
                linkedBlockingQueue = new LinkedBlockingQueue<>();
                MAP.put(name, linkedBlockingQueue);
            }
        } finally {
            LOCK.unlock();
        }

        try {
            linkedBlockingQueue.put(Boolean.TRUE);
        } catch (InterruptedException e) {
            // DO NOTHING
        }
    }

    /**
     * Method to wait for a Condition
     *
     * @param name name
     */
    public static void await(Object name) {
        LinkedBlockingQueue<Boolean> linkedBlockingQueue;

        try {
            LOCK.lock();
            linkedBlockingQueue = MAP.get(name);
            if (linkedBlockingQueue == null) {
                linkedBlockingQueue = new LinkedBlockingQueue<>();
                MAP.put(name, linkedBlockingQueue);
            }
        } finally {
            LOCK.unlock();
        }

        try {
            linkedBlockingQueue.add(linkedBlockingQueue.take());
        } catch (InterruptedException e) {
            // DO NOTHING
        }
    }
}
