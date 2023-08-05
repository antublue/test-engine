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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.antublue.test.engine.Constants;
import org.antublue.test.engine.internal.Configuration;

public class ThrowableUtils {

    private static final ThrowableUtils SINGLETON = new ThrowableUtils();

    private static boolean pruneStackTraces;

    private ThrowableUtils() {
        Optional<String> optional = Configuration.singleton().get(Constants.PRUNE_STACK_TRACES);
        pruneStackTraces = optional.map(Constants.TRUE::equals).orElse(true);
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static ThrowableUtils singleton() {
        return SINGLETON;
    }

    /**
     * Method to prune a stack trace if configured, removing reflection and engine code
     *
     * @param clazz clazz
     * @param throwable throwable
     * @return the pruned Throwable exception
     */
    public Throwable pruneStackTrace(Class<?> clazz, Throwable throwable) {
        if (!pruneStackTraces) {
            return throwable;
        }

        Throwable prunedThrowable = throwable;

        if (prunedThrowable instanceof InvocationTargetException) {
            prunedThrowable = prunedThrowable.getCause();
        }

        String className = clazz.getName();
        List<StackTraceElement> workingStackTrace = new ArrayList<>();
        List<StackTraceElement> stackTraceElements = Arrays.asList(prunedThrowable.getStackTrace());

        Iterator<StackTraceElement> stackTraceElementIterator = stackTraceElements.iterator();
        while (stackTraceElementIterator.hasNext()) {
            StackTraceElement stackTraceElement = stackTraceElementIterator.next();
            String stackTraceClassName = stackTraceElement.getClassName();
            if (!stackTraceClassName.equals(className)) {
                break;
            }
            workingStackTrace.add(stackTraceElement);
        }

        prunedThrowable.setStackTrace(workingStackTrace.toArray(new StackTraceElement[0]));
        return prunedThrowable;
    }
}
