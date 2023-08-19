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

package org.antublue.test.engine.internal.descriptor;

import java.io.PrintStream;
import java.io.PrintWriter;
import org.antublue.test.engine.Constants;
import org.antublue.test.engine.internal.Configuration;

/** Class to implement a PrunedThrowable */
public class PrunedThrowable extends Throwable {

    private final boolean prune;

    private final Class<?> clazz;
    private final Throwable throwable;

    /**
     * Constructor
     *
     * @param clazz clazz
     * @param throwable throwable
     */
    private PrunedThrowable(Class<?> clazz, Throwable throwable) {
        this.clazz = clazz;

        Throwable t = throwable;

        prune =
                Configuration.singleton()
                        .getBooleanOrDefault(Constants.STACK_TRACE_PRUNE, true)
                        .get();

        if (prune) {
            while (!isClassInStackTrace(clazz, t)) {
                t = t.getCause();
            }
        }

        this.throwable = t;
    }

    @Override
    public void printStackTrace() {
        throwable.printStackTrace(System.err);
    }

    @Override
    public void printStackTrace(PrintStream printStream) {
        printStackTrace(new PrintWriter(printStream, false));
    }

    @Override
    public void printStackTrace(PrintWriter printWriter) {
        if (prune) {
            synchronized (printWriter) {
                printStackTraceElements(0, clazz, throwable);
            }
        } else {
            throwable.printStackTrace(printWriter);
        }

        printWriter.flush();
    }

    /**
     * Method to create a PrunedThrowable from a Throwable
     *
     * @param clazz
     * @param throwable
     * @return a PrunedThrowable
     */
    public static PrunedThrowable of(Class<?> clazz, Throwable throwable) {
        return new PrunedThrowable(clazz, throwable);
    }

    private static boolean isClassInStackTrace(Class<?> clazz, Throwable throwable) {
        String className = clazz.getName();
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if (stackTraceElement.toString().startsWith(className)) {
                return true;
            }
        }
        return false;
    }

    private static void printStackTraceElements(int depth, Class<?> clazz, Throwable throwable) {
        if (depth == 0) {
            System.err.format("%s", throwable.getClass().getName());
        } else {
            System.err.format("Caused by: %s", throwable.getClass().getName());
        }

        String message = throwable.getMessage();
        if (message != null) {
            System.err.format(": %s", message);
        }

        System.err.println();

        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            if (stackTraceElement
                    .toString()
                    .contains("org.antublue.test.engine.internal.descriptor.")) {
                break;
            }

            System.err.format("\tat %s", stackTraceElement).println();
        }

        Throwable cause = throwable.getCause();
        if (cause != null) {
            printStackTraceElements(depth + 1, clazz, throwable.getCause());
        }
    }
}
