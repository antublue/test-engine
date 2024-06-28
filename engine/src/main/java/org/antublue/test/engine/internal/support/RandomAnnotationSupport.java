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

package org.antublue.test.engine.internal.support;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.util.Predicates;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.util.Preconditions;

/** Class to process @TestEngine.Random.X annotations */
public class RandomAnnotationSupport {

    private static final Predicate<Field> HAS_RANDOM_ANNOTATION =
            field ->
                    field.isAnnotationPresent(TestEngine.Random.Boolean.class)
                            || field.isAnnotationPresent(TestEngine.Random.Byte.class)
                            || field.isAnnotationPresent(TestEngine.Random.Character.class)
                            || field.isAnnotationPresent(TestEngine.Random.Short.class)
                            || field.isAnnotationPresent(TestEngine.Random.Integer.class)
                            || field.isAnnotationPresent(TestEngine.Random.Long.class)
                            || field.isAnnotationPresent(TestEngine.Random.Float.class)
                            || field.isAnnotationPresent(TestEngine.Random.Double.class)
                            || field.isAnnotationPresent(TestEngine.Random.BigInteger.class)
                            || field.isAnnotationPresent(TestEngine.Random.BigDecimal.class)
                            || field.isAnnotationPresent(TestEngine.Random.UUID.class);

    /** Constructor */
    private RandomAnnotationSupport() {
        // DO NOTHING
    }

    /**
     * Method to inject random values into static member fields
     *
     * @param testClass testClass
     * @throws Throwable Throwable
     */
    public static void setRandomFields(Class<?> testClass) throws Throwable {
        Preconditions.notNull(testClass, "testClass is null");
        setRandomFields(testClass, null, Predicates.STATIC_FIELD);
    }

    /**
     * Method to inject random values into member fields
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    public static void setRandomFields(Object testInstance) throws Throwable {
        Preconditions.notNull(testInstance, "testInstance is null");
        setRandomFields(testInstance.getClass(), testInstance, Predicates.FIELD);
    }

    /**
     * Method to clear random values in static member fields
     *
     * @param testClass testClass
     * @throws Throwable Throwable
     */
    public static void clearRandomFields(Class<?> testClass) throws Throwable {
        Preconditions.notNull(testClass, "testClass is null");
        clearRandomFields(testClass, null, Predicates.STATIC_FIELD);
    }

    /**
     * Method to clear random values in member fields
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    public static void clearRandomFields(Object testInstance) throws Throwable {
        Preconditions.notNull(testInstance, "testInstance is null");
        clearRandomFields(testInstance.getClass(), testInstance, Predicates.FIELD);
    }

    /**
     * Method to inject random values in to member fields
     *
     * @param testClass testClass
     * @param testInstance testInstance
     * @param fieldPredicate fieldPredicate
     * @throws Throwable Throwable
     */
    private static void setRandomFields(
            Class<?> testClass, Object testInstance, Predicate<Field> fieldPredicate)
            throws Throwable {
        List<Field> fields =
                FieldSupport.findFields(
                        testClass,
                        field -> fieldPredicate.test(field) && HAS_RANDOM_ANNOTATION.test(field),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            if (field.isAnnotationPresent(TestEngine.Random.Boolean.class)) {
                FieldSupport.setField(testInstance, field, RandomSupport.randomBoolean());
            } else if (field.isAnnotationPresent(TestEngine.Random.Byte.class)) {
                TestEngine.Random.Byte annotation =
                        field.getAnnotation(TestEngine.Random.Byte.class);
                byte minimum = annotation.minimum();
                byte maximum = annotation.maximum();
                byte random = (byte) RandomSupport.randomInt(minimum, maximum);
                FieldSupport.setField(testInstance, field, random);
            } else if (field.isAnnotationPresent(TestEngine.Random.Character.class)) {
                TestEngine.Random.Character annotation =
                        field.getAnnotation(TestEngine.Random.Character.class);
                char minimum = annotation.minimum();
                char maximum = annotation.maximum();
                char random = (char) RandomSupport.randomInt(minimum, maximum);
                FieldSupport.setField(testInstance, field, random);
            } else if (field.isAnnotationPresent(TestEngine.Random.Short.class)) {
                TestEngine.Random.Short annotation =
                        field.getAnnotation(TestEngine.Random.Short.class);
                short minimum = annotation.minimum();
                short maximum = annotation.maximum();
                short random = (short) RandomSupport.randomInt(minimum, maximum);
                FieldSupport.setField(testInstance, field, random);
            } else if (field.isAnnotationPresent(TestEngine.Random.Integer.class)) {
                TestEngine.Random.Integer annotation =
                        field.getAnnotation(TestEngine.Random.Integer.class);
                int minimum = annotation.minimum();
                int maximum = annotation.maximum();
                int random = RandomSupport.randomInt(minimum, maximum);
                FieldSupport.setField(testInstance, field, random);
            } else if (field.isAnnotationPresent(TestEngine.Random.Long.class)) {
                TestEngine.Random.Long annotation =
                        field.getAnnotation(TestEngine.Random.Long.class);
                long minimum = annotation.minimum();
                long maximum = annotation.maximum();
                long random = RandomSupport.randomLong(minimum, maximum);
                FieldSupport.setField(testInstance, field, random);
            } else if (field.isAnnotationPresent(TestEngine.Random.Float.class)) {
                TestEngine.Random.Float annotation =
                        field.getAnnotation(TestEngine.Random.Float.class);
                float minimum = annotation.minimum();
                float maximum = annotation.maximum();
                float random = RandomSupport.randomFloat(minimum, maximum);
                FieldSupport.setField(testInstance, field, random);
            } else if (field.isAnnotationPresent(TestEngine.Random.Double.class)) {
                TestEngine.Random.Double annotation =
                        field.getAnnotation(TestEngine.Random.Double.class);
                double minimum = annotation.minimum();
                double maximum = annotation.maximum();
                double random = RandomSupport.randomDouble(minimum, maximum);
                FieldSupport.setField(testInstance, field, random);
            } else if (field.isAnnotationPresent(TestEngine.Random.BigInteger.class)) {
                TestEngine.Random.BigInteger annotation =
                        field.getAnnotation(TestEngine.Random.BigInteger.class);
                String minimum = annotation.minimum();
                String maximum = annotation.maximum();
                BigInteger random = RandomSupport.randomBigInteger(minimum, maximum);
                FieldSupport.setField(testInstance, field, random);
            } else if (field.isAnnotationPresent(TestEngine.Random.BigDecimal.class)) {
                TestEngine.Random.BigDecimal annotation =
                        field.getAnnotation(TestEngine.Random.BigDecimal.class);
                String minimum = annotation.minimum();
                String maximum = annotation.maximum();
                BigDecimal random = RandomSupport.randomBigDecimal(minimum, maximum);
                FieldSupport.setField(testInstance, field, random);
            } else if (field.isAnnotationPresent(TestEngine.Random.UUID.class)) {
                UUID random = UUID.randomUUID();
                FieldSupport.setField(testInstance, field, random);
            }
        }
    }

    /**
     * Method to clear random values in to member fields
     *
     * @param testClass testClass
     * @param testInstance testInstance
     * @param fieldPredicate fieldPredicate
     * @throws Throwable Throwable
     */
    private static void clearRandomFields(
            Class<?> testClass, Object testInstance, Predicate<Field> fieldPredicate)
            throws Throwable {
        List<Field> fields =
                FieldSupport.findFields(
                        testClass,
                        field -> fieldPredicate.test(field) && HAS_RANDOM_ANNOTATION.test(field),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            if (field.isAnnotationPresent(TestEngine.Random.Boolean.class)) {
                FieldSupport.setField(testInstance, field, Boolean.FALSE);
            } else if (field.isAnnotationPresent(TestEngine.Random.Byte.class)) {
                FieldSupport.setField(testInstance, field, (byte) 0);
            } else if (field.isAnnotationPresent(TestEngine.Random.Character.class)) {
                FieldSupport.setField(testInstance, field, (char) 0);
            } else if (field.isAnnotationPresent(TestEngine.Random.Short.class)) {
                FieldSupport.setField(testInstance, field, (short) 0);
            } else if (field.isAnnotationPresent(TestEngine.Random.Integer.class)) {
                FieldSupport.setField(testInstance, field, 0);
            } else if (field.isAnnotationPresent(TestEngine.Random.Long.class)) {
                FieldSupport.setField(testInstance, field, 0L);
            } else if (field.isAnnotationPresent(TestEngine.Random.Float.class)) {
                FieldSupport.setField(testInstance, field, 0F);
            } else if (field.isAnnotationPresent(TestEngine.Random.Double.class)) {
                FieldSupport.setField(testInstance, field, 0D);
            } else if (field.isAnnotationPresent(TestEngine.Random.BigInteger.class)) {
                FieldSupport.setField(testInstance, field, null);
            } else if (field.isAnnotationPresent(TestEngine.Random.BigDecimal.class)) {
                FieldSupport.setField(testInstance, field, null);
            } else if (field.isAnnotationPresent(TestEngine.Random.UUID.class)) {
                FieldSupport.setField(testInstance, field, null);
            }
        }
    }
}
