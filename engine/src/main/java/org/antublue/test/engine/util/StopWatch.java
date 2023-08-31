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

package org.antublue.test.engine.util;

/** Class to implement a stop watch */
public class StopWatch {

    private long startNanoTime;
    private long stopNanoTime;

    /**
     * Method to start the stop watch
     *
     * @return this
     */
    public StopWatch start() {
        startNanoTime = System.nanoTime();
        return this;
    }

    /**
     * Method to stop the stop watch
     *
     * @return this
     */
    public StopWatch stop() {
        stopNanoTime = System.nanoTime();
        return this;
    }

    /**
     * Method to get the elapsed time
     *
     * @return the elapsed time in nanoseconds
     */
    public long elapsedTime() {
        return stopNanoTime - startNanoTime;
    }
}
