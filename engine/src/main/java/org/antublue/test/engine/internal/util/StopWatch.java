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

import java.util.Objects;

/** Class to implement a stop watch */
@SuppressWarnings("UnusedReturnValue")
public class StopWatch {

    private long startNanoTime;
    private Long stopNanoTime;

    /** Constructor */
    public StopWatch() {
        reset();
    }

    /**
     * Constructor
     *
     * <p>The stop watch starts automatically
     *
     * <p>/ public StopWatch() { reset(); }
     *
     * <p>/** Method to reset the stop watch
     *
     * @return this
     */
    public StopWatch reset() {
        startNanoTime = System.nanoTime();
        stopNanoTime = null;
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
     * Method to get the elapsed time in nanoseconds
     *
     * @return the elapsed time in nanoseconds
     */
    public long elapsedNanoseconds() {
        if (stopNanoTime == null) {
            return System.nanoTime() - startNanoTime;
        } else {
            return stopNanoTime - startNanoTime;
        }
    }

    /**
     * Method to get the elapsed time in milliseconds
     *
     * @return the elapsed time in milliseconds
     */
    public long elapsedMilliseconds() {
        long elapsedNanoseconds = elapsedNanoseconds();
        if (elapsedNanoseconds <= 0) {
            return 0;
        } else {
            return elapsedNanoseconds() / 1000000;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StopWatch stopWatch = (StopWatch) o;
        return startNanoTime == stopWatch.startNanoTime && Objects.equals(stopNanoTime, stopWatch.stopNanoTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startNanoTime, stopNanoTime);
    }
}
