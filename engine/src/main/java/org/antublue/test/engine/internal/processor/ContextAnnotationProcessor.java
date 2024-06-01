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

package org.antublue.test.engine.internal.processor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.internal.logger.Logger;
import org.antublue.test.engine.api.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.predicate.AnnotationFieldPredicate;
import org.antublue.test.engine.internal.util.ThrowableContext;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to process @TestEngine.Argument annotations */
@SuppressWarnings("PMD.AvoidAccessibilityAlteration")
public class ContextAnnotationProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextAnnotationProcessor.class);

    /** Constructor */
    private ContextAnnotationProcessor() {
        // DO NOTHING
    }

    /**
     * Method to get the singleton
     *
     * @return the singleton
     */
    public static ContextAnnotationProcessor getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Method to process @TestEngine.Argument annotations
     *
     * @param testInstance testInstance
     * @param throwableContext throwableContext
     */
    public void process(Object testInstance, ThrowableContext throwableContext) {
        LOGGER.trace("process()");
        try {
            List<Field> fields =
                    ReflectionSupport.findFields(
                            testInstance.getClass(),
                            AnnotationFieldPredicate.of(TestEngine.Context.class),
                            HierarchyTraversalMode.TOP_DOWN);

            for (Field field : fields) {
                field.setAccessible(true);
                if (Modifier.isStatic(field.getModifiers())) {
                    field.set(null, org.antublue.test.engine.api.Context.getInstance());
                } else {
                    field.set(testInstance, org.antublue.test.engine.api.Context.getInstance());
                }
            }
        } catch (Throwable t) {
            throwableContext.add(testInstance.getClass(), t);
        }
    }

    /** Class to hold the singleton instance */
    private static final class SingletonHolder {

        /** The singleton instance */
        private static final ContextAnnotationProcessor INSTANCE = new ContextAnnotationProcessor();
    }
}
