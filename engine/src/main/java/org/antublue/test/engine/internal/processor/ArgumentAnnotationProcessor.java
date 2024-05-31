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

import static java.lang.String.format;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.internal.logger.Logger;
import org.antublue.test.engine.api.internal.logger.LoggerFactory;
import org.antublue.test.engine.exception.TestClassDefinitionException;
import org.antublue.test.engine.internal.predicate.AnnotationFieldPredicate;
import org.antublue.test.engine.internal.util.ThrowableContext;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to process @TestEngine.Argument annotations */
@SuppressWarnings("PMD.AvoidAccessibilityAlteration")
public class ArgumentAnnotationProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentAnnotationProcessor.class);

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
        return SingletonHolder.INSTANCE;
    }

    /**
     * Method to prepare @TestEngine.Argument annotations
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param throwableContext throwableContext
     */
    public void prepare(
            Object testInstance, Named<?> testArgument, ThrowableContext throwableContext) {
        LOGGER.trace(
                "prepare() class [%s] instance [%s], argument [%s]",
                testInstance.getClass(), testInstance, testArgument.getName());

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
            Object testInstance, Named<?> testArgument, ThrowableContext throwableContext) {
        LOGGER.trace(
                "conclude() class [%s] instance [%s], argument [%s]",
                testInstance.getClass(),
                testInstance,
                testArgument != null ? testArgument.getName() : null);

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
            Object testInstance, Named<?> testArgument, ThrowableContext throwableContext) {
        try {
            List<Field> fields =
                    ReflectionSupport.findFields(
                            testInstance.getClass(),
                            AnnotationFieldPredicate.of(TestEngine.Argument.class),
                            HierarchyTraversalMode.TOP_DOWN);

            for (Field field : fields) {
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                String fieldTypeName = fieldType.getName();
                if (fieldTypeName.contains("$")) {
                    fieldTypeName = fieldTypeName.substring(0, fieldTypeName.indexOf("$"));
                }

                if (testArgument == null) {
                    field.set(testInstance, testArgument);
                } else if (fieldType.isAssignableFrom(testArgument.getClass())) {
                    field.set(testInstance, testArgument);
                } else if (fieldType.isAssignableFrom(testArgument.getPayload().getClass())) {
                    field.set(testInstance, testArgument.getPayload());
                } else {
                    Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) genericType;
                        Type[] typeArguments = parameterizedType.getActualTypeArguments();
                        for (Type typeArgument : typeArguments) {
                            System.out.println("Generic Type: " + typeArgument.getTypeName());
                        }
                    }

                    throw new TestClassDefinitionException(
                            format(
                                    "Class [%s] field [%s] can't be assigned argument type [%s]",
                                    testInstance.getClass().getName(),
                                    field.getName(),
                                    toString(field)));
                }
            }
        } catch (Throwable t) {
            throwableContext.add(testInstance.getClass(), t);
        }
    }

    private static String toString(Field field) {
        StringBuilder result = new StringBuilder();

        // Get the field's type
        Class<?> fieldType = field.getType();
        result.append(fieldType.getName());

        // Get the field's generic type if it's parameterized
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            result.append("<");
            int i = 0;
            for (Type typeArgument : typeArguments) {
                if (i > 0) {
                    result.append(", ");
                }
                result.append(typeArgument.getTypeName());
                i++;
            }
        }
        return result.toString();
    }

    /** Class to hold the singleton instance */
    private static final class SingletonHolder {

        /** The singleton instance */
        private static final ArgumentAnnotationProcessor INSTANCE =
                new ArgumentAnnotationProcessor();
    }
}
