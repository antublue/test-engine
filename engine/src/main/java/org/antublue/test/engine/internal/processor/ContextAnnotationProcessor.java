/*
 * Copyright (C) 2024 The AntuBLUE test-engine project authors
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
import java.util.List;
import org.antublue.test.engine.api.Context;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.predicate.AnnotationFieldPredicate;
import org.antublue.test.engine.internal.util.ThrowableContext;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to process @TestEngine.Store annotations */
@SuppressWarnings("PMD.AvoidAccessibilityAlteration")
public class ContextAnnotationProcessor {

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
        return ContextAnnotationProcessor.SingletonHolder.INSTANCE;
    }

    /**
     * Method to prepare @TestEngine.Store annotations
     *
     * @param testInstance testInstance
     * @param throwableContext throwableContext
     */
    public void process(Object testInstance, ThrowableContext throwableContext) {
        try {

            List<Field> fields =
                    ReflectionSupport.findFields(
                            testInstance.getClass(),
                            AnnotationFieldPredicate.of(TestEngine.Context.class),
                            HierarchyTraversalMode.TOP_DOWN);

            for (Field field : fields) {
                field.setAccessible(true);
                field.set(testInstance, Context.getInstance());
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
