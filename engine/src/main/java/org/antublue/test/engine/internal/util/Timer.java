/*
 * Copyright 2022-2023 Douglas Hoard
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

/**
 * Class to implement a Timer
 */
public final class Timer {

    private long startMs;
    private long stopMs;

    /**
     * Constructor (autostarted)
     */
    public Timer() {
        this(true);
    }

    /**
     * Constructor
     *
     * @param autoStart autoStart
     */
    public Timer(boolean autoStart) {
        if (autoStart) {
             start();
        }
    }

    /**
     * Method to start the Timer
     *
     * @return the return value
     */
    public Timer start() {
        startMs = System.currentTimeMillis();
        stopMs = startMs;
        return this;
    }

    /**
     * Method to stop the Timer
     *
     * @return the return value
     */
    public Timer stop() {
        stopMs = System.currentTimeMillis();
        return this;
    }

    /**
     * Method to get the Timer's elapsed time
     *
     * @return the return value
     */
    public long elapsedTime() {
        return stopMs - startMs;
    }
}
