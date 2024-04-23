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

package example.extension;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.internal.util.StopWatch;

/** Example Extension */
public class StopWatchExtension implements Extension {

    private static final StopWatchExtension SINGLETON = new StopWatchExtension();

    private final Map<Class<?>, StopWatch> stopWatchMap;

    /** Constructor */
    private StopWatchExtension() {
        stopWatchMap = new ConcurrentHashMap<>();
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static StopWatchExtension getSingleton() {
        return SINGLETON;
    }

    @Override
    public void postInstantiateCallback(Object testInstance) {
        stopWatchMap.put(testInstance.getClass(), new StopWatch().start());
    }

    @Override
    public void postConcludeMethodsCallback(Object testInstance) {
        StopWatch stopWatch = stopWatchMap.remove(testInstance.getClass()).stop();
        synchronized (System.out) {
            System.out.println(
                    "------------------------------------------------------------------------");
            System.out.println(
                    String.format(
                            "test class [%s] elapsed time [%s] ms",
                            testInstance.getClass().getName(),
                            TimeUnit.MILLISECONDS.convert(
                                    stopWatch.elapsedNanoTime(), TimeUnit.NANOSECONDS)));
            System.out.println(
                    "------------------------------------------------------------------------");
            System.out.flush();
        }
    }
}
