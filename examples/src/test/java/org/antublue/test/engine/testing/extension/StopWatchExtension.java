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

package org.antublue.test.engine.testing.extension;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.util.StopWatch;

public class StopWatchExtension implements Extension {

    private static final StopWatchExtension SINGLETON = new StopWatchExtension();

    private final Map<Class<?>, StopWatch> stopWatchMap;

    private StopWatchExtension() {
        stopWatchMap = new ConcurrentHashMap<>();
    }

    public static StopWatchExtension singleton() {
        return SINGLETON;
    }

    public void beforePrepare(Object testInstance) {
        stopWatchMap.put(testInstance.getClass(), new StopWatch().start());
    }

    public void afterConclude(Object testInstance) {
        StopWatch stopWatch = stopWatchMap.remove(testInstance.getClass()).stop();
        System.out.println(
                String.format(
                        "%s testClass [%s] elapsedTime [%s] ms",
                        getClass().getSimpleName(),
                        testInstance.getClass().getName(),
                        TimeUnit.MILLISECONDS.convert(
                                stopWatch.elapsedTime(), TimeUnit.NANOSECONDS)));
        System.out.flush();
    }
}
