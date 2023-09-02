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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.antublue.test.engine.configuration.Configuration;
import org.antublue.test.engine.configuration.Constants;

/** Class to implement ThrowableUtils */
public class ThrowableUtils {

    private static final boolean STACK_TRACE_PRUNING =
            Configuration.getSingleton()
                    .getBooleanOrDefault(Constants.STACK_TRACE_PRUNE, true)
                    .get();

    private static final Predicate<String> EXCLUDE =
            s ->
                    s.startsWith("org.antublue.test.engine.test.descriptor.")
                            || s.startsWith("org.antublue.test.engine.util.Invocation.")
                            || s.startsWith("org.antublue.test.engine.Executor")
                            || s.startsWith("java.base/");

    /** Constructor */
    private ThrowableUtils() {
        // DO NOTHING
    }

    /**
     * Method to prune a Throwable
     *
     * @param clazz clazz
     * @param throwable throwable
     * @return a pruned Throwable
     */
    public static Throwable prune(Class<?> clazz, Throwable throwable) {
        if (!STACK_TRACE_PRUNING) {
            return throwable;
        }

        Throwable rootThrowable = throwable;

        while (!isClassInStackTrace(rootThrowable, clazz) && rootThrowable.getCause() != null) {
            rootThrowable = rootThrowable.getCause();
        }

        Throwable tempThrowable = rootThrowable;
        while (tempThrowable != null) {
            prune(tempThrowable);
            tempThrowable = tempThrowable.getCause();
        }

        return rootThrowable;
    }

    /**
     * Method to prune a Throwable
     *
     * @param throwable throwable
     */
    private static void prune(Throwable throwable) {
        List<StackTraceElement> prunedStackTraceElements = new ArrayList<>();

        List<StackTraceElement> stackTraceElements =
                new ArrayList<>(Arrays.asList(throwable.getStackTrace()));
        Collections.reverse(stackTraceElements);

        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if (EXCLUDE.test(stackTraceElement.toString())) {
                continue;
            }
            prunedStackTraceElements.add(stackTraceElement);
        }

        Collections.reverse(prunedStackTraceElements);
        throwable.setStackTrace(prunedStackTraceElements.toArray(new StackTraceElement[0]));
    }

    /**
     * Method to determine if a class is in the stack trace
     *
     * @param throwable throwable
     * @param clazz class
     * @return true if the class exists in a stack trace of a Throwable, otherwise false
     */
    private static boolean isClassInStackTrace(Throwable throwable, Class<?> clazz) {
        String className = clazz.getName();
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if (stackTraceElement.toString().startsWith(className)) {
                return true;
            }
        }
        return false;
    }
}
