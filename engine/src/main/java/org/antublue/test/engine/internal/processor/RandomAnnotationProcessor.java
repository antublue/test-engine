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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.support.RandomGenerator;
import org.antublue.test.engine.internal.predicate.AnnotationFieldPredicate;
import org.antublue.test.engine.internal.util.ThrowableContext;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to process @TestEngine.Random.X annotations */
@SuppressWarnings("PMD.AvoidAccessibilityAlteration")
public class RandomAnnotationProcessor {

    /** Constructor */
    private RandomAnnotationProcessor() {
        // DO NOTHING
    }

    public static RandomAnnotationProcessor getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Method to prepare @TestEngine.RandomX annotated fields
     *
     * @param testInstance testInstance
     * @param throwableContext throwableContext
     */
    public void setRandomFields(Object testInstance, ThrowableContext throwableContext) {
        setBooleanFields(testInstance, throwableContext);
        setIntegerFields(testInstance, throwableContext);
        setLongFields(testInstance, throwableContext);
        setFloatFields(testInstance, throwableContext);
        setDoubleFields(testInstance, throwableContext);
        setBigIntegerFields(testInstance, throwableContext);
        setBigDecimalFields(testInstance, throwableContext);
        setUUIDFields(testInstance, throwableContext);
    }

    public void clearRandomFields(Object testInstance, ThrowableContext throwableContext) {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        AnnotationFieldPredicate.of(TestEngine.Random.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                field.set(testInstance, null);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to prepare @TestEngine.Random.Boolean annotations
     *
     * @param testInstance testInstance
     * @param throwableContext throwableContext
     */
    private void setBooleanFields(Object testInstance, ThrowableContext throwableContext) {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        AnnotationFieldPredicate.of(TestEngine.Random.Boolean.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            try {
                boolean value = RandomGenerator.nextBoolean();

                field.setAccessible(true);
                if (field.getType().equals(String.class)) {
                    field.set(testInstance, String.valueOf(value));
                } else {
                    field.set(testInstance, value);
                }
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to prepare @TestEngine.Random.Integer annotations
     *
     * @param testInstance testInstance
     * @param throwableContext throwableContext
     */
    private void setIntegerFields(Object testInstance, ThrowableContext throwableContext) {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        AnnotationFieldPredicate.of(TestEngine.Random.Integer.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            try {
                TestEngine.Random.Integer annotation =
                        field.getAnnotation(TestEngine.Random.Integer.class);

                int value = RandomGenerator.nextInteger(annotation.minimum(), annotation.maximum());

                field.setAccessible(true);
                if (field.getType().equals(String.class)) {
                    field.set(testInstance, String.valueOf(value));
                } else {
                    field.set(testInstance, value);
                }
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to prepare @TestEngine.Random.Long annotations
     *
     * @param testInstance testInstance
     * @param throwableContext throwableContext
     */
    private void setLongFields(Object testInstance, ThrowableContext throwableContext) {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        AnnotationFieldPredicate.of(TestEngine.Random.Long.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            try {
                TestEngine.Random.Long annotation =
                        field.getAnnotation(TestEngine.Random.Long.class);

                long value = RandomGenerator.nextLong(annotation.minimum(), annotation.maximum());

                field.setAccessible(true);
                if (field.getType().equals(String.class)) {
                    field.set(testInstance, String.valueOf(value));
                } else {
                    field.set(testInstance, value);
                }
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to prepare @TestEngine.Random.Float annotations
     *
     * @param testInstance testInstance
     * @param throwableContext throwableContext
     */
    private void setFloatFields(Object testInstance, ThrowableContext throwableContext) {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        AnnotationFieldPredicate.of(TestEngine.Random.Float.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            try {
                TestEngine.Random.Float annotation =
                        field.getAnnotation(TestEngine.Random.Float.class);

                float value = RandomGenerator.nextFloat(annotation.minimum(), annotation.maximum());

                field.setAccessible(true);
                if (field.getType().equals(String.class)) {
                    field.set(testInstance, String.valueOf(value));
                } else {
                    field.set(testInstance, value);
                }
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to prepare @TestEngine.Random.Double annotations
     *
     * @param testInstance testInstance
     * @param throwableContext throwableContext
     */
    private void setDoubleFields(Object testInstance, ThrowableContext throwableContext) {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        AnnotationFieldPredicate.of(TestEngine.Random.Double.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            try {
                TestEngine.Random.Double annotation =
                        field.getAnnotation(TestEngine.Random.Double.class);

                double value =
                        RandomGenerator.nextDouble(annotation.minimum(), annotation.maximum());

                field.setAccessible(true);
                if (field.getType().equals(String.class)) {
                    field.set(testInstance, String.valueOf(value));
                } else {
                    field.set(testInstance, value);
                }
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to prepare @TestEngine.Random.BigInteger annotations
     *
     * @param testInstance testInstance
     * @param throwableContext throwableContext
     */
    private void setBigIntegerFields(Object testInstance, ThrowableContext throwableContext) {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        AnnotationFieldPredicate.of(TestEngine.Random.BigInteger.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            try {
                TestEngine.Random.BigInteger annotation =
                        field.getAnnotation(TestEngine.Random.BigInteger.class);

                BigInteger value =
                        RandomGenerator.nextBigInteger(annotation.minimum(), annotation.maximum());

                field.setAccessible(true);
                if (field.getType().equals(String.class)) {
                    field.set(testInstance, value.toString());
                } else {
                    field.set(testInstance, value);
                }
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to prepare @TestEngine.Random.BigDecimal annotations
     *
     * @param testInstance testInstance
     * @param throwableContext throwableContext
     */
    private void setBigDecimalFields(Object testInstance, ThrowableContext throwableContext) {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        AnnotationFieldPredicate.of(TestEngine.Random.BigDecimal.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            try {
                TestEngine.Random.BigDecimal annotation =
                        field.getAnnotation(TestEngine.Random.BigDecimal.class);

                BigDecimal value =
                        RandomGenerator.nextBigDecimal(annotation.minimum(), annotation.maximum());

                field.setAccessible(true);
                if (field.getType().equals(String.class)) {
                    field.set(testInstance, value.toString());
                } else {
                    field.set(testInstance, value);
                }
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to prepare @TestEngine.Random.UUID annotations
     *
     * @param testInstance testInstance
     * @param throwableContext throwableContext
     */
    private void setUUIDFields(Object testInstance, ThrowableContext throwableContext) {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        AnnotationFieldPredicate.of(TestEngine.Random.UUID.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            try {
                UUID uuid = UUID.randomUUID();

                field.setAccessible(true);
                if (field.getType().equals(String.class)) {
                    field.set(testInstance, uuid.toString());
                } else {
                    field.set(testInstance, uuid);
                }
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /** Class to hold the singleton instance */
    private static final class SingletonHolder {

        /** The singleton instance */
        private static final RandomAnnotationProcessor INSTANCE = new RandomAnnotationProcessor();
    }
}
