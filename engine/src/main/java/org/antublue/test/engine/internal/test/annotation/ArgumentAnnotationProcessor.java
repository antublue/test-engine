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

package org.antublue.test.engine.internal.test.annotation;

import java.lang.reflect.Field;
import java.util.List;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.test.descriptor.filter.AnnotationFieldFilter;
import org.antublue.test.engine.internal.test.util.ThrowableContext;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to process @TestEngine.Argument annotations */
@SuppressWarnings("PMD.AvoidAccessibilityAlteration")
public class ArgumentAnnotationProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentAnnotationProcessor.class);

    private static final ArgumentAnnotationProcessor SINGLETON = new ArgumentAnnotationProcessor();

    /** Constructor */
    private ArgumentAnnotationProcessor() {
        // DO NOTHING
    }

    /**
     * Method to get the singleton
     *
     * @return the singleton
     */
    public static ArgumentAnnotationProcessor getInstance() {
        return SINGLETON;
    }

    /**
     * Method to prepare @TestEngine.Argument annotations
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param throwableContext throwableContext
     */
    public void prepare(
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
        LOGGER.trace(
                "prepare() class [%s] instance [%s], argument [%s]",
                testInstance.getClass(), testInstance, testArgument.name());

        process(testInstance, testArgument, throwableContext);
    }

    /**
     * Method to conclude @TestEngine.Argument annotations
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param throwableContext throwableContext
     */
    public void conclude(
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
        LOGGER.trace(
                "conclude() class [%s] instance [%s], argument [%s]",
                testInstance.getClass(),
                testInstance,
                testArgument != null ? testArgument.name() : null);

        process(testInstance, testArgument, throwableContext);
    }

    /**
     * Method to process @TestEngine.Argument annotations
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param throwableContext throwableContext
     */
    private void process(
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
        try {
            List<Field> fields =
                    ReflectionSupport.findFields(
                            testInstance.getClass(),
                            AnnotationFieldFilter.of(TestEngine.Argument.class),
                            HierarchyTraversalMode.TOP_DOWN);

            for (Field field : fields) {
                field.setAccessible(true);
                field.set(testInstance, testArgument);
            }
        } catch (Throwable t) {
            throwableContext.add(testInstance.getClass(), t);
        }
    }
}
