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
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.util.Predicates;
import org.antublue.test.engine.internal.util.RandomUtils;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

// TODO refactor
/** Class to process @TestEngine.Random.X annotations */
@SuppressWarnings("PMD.AvoidAccessibilityAlteration")
public class RandomAnnotationSupport {

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
    public static void injectRandomFields(Class<?> testClass) throws Throwable {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Boolean.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(null, field, RandomUtils.randomBoolean());
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.Byte.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Byte annotation = field.getAnnotation(TestEngine.Random.Byte.class);
            byte minimum = annotation.minimum();
            byte maximum = annotation.maximum();
            byte random = (byte) RandomUtils.randomInt(minimum, maximum);
            FieldSupport.setField(null, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Character.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Character annotation =
                    field.getAnnotation(TestEngine.Random.Character.class);
            char minimum = annotation.minimum();
            char maximum = annotation.maximum();
            char random = (char) RandomUtils.randomInt(minimum, maximum);
            FieldSupport.setField(null, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.Short.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Short annotation = field.getAnnotation(TestEngine.Random.Short.class);
            short minimum = annotation.minimum();
            short maximum = annotation.maximum();
            short random = (short) RandomUtils.randomInt(minimum, maximum);
            FieldSupport.setField(null, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Integer.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Integer annotation =
                    field.getAnnotation(TestEngine.Random.Integer.class);
            int minimum = annotation.minimum();
            int maximum = annotation.maximum();
            int random = RandomUtils.randomInt(minimum, maximum);
            FieldSupport.setField(null, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.Long.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Long annotation = field.getAnnotation(TestEngine.Random.Long.class);
            long minimum = annotation.minimum();
            long maximum = annotation.maximum();
            long random = RandomUtils.randomLong(minimum, maximum);
            FieldSupport.setField(null, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.Float.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Float annotation = field.getAnnotation(TestEngine.Random.Float.class);
            float minimum = annotation.minimum();
            float maximum = annotation.maximum();
            float random = RandomUtils.randomFloat(minimum, maximum);
            FieldSupport.setField(null, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Double.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Double annotation =
                    field.getAnnotation(TestEngine.Random.Double.class);
            double minimum = annotation.minimum();
            double maximum = annotation.maximum();
            double random = RandomUtils.randomDouble(minimum, maximum);
            FieldSupport.setField(null, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.BigInteger.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.BigInteger annotation =
                    field.getAnnotation(TestEngine.Random.BigInteger.class);
            String minimum = annotation.minimum();
            String maximum = annotation.maximum();
            BigInteger random = RandomUtils.randomBigInteger(minimum, maximum);
            FieldSupport.setField(null, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.BigDecimal.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.BigDecimal annotation =
                    field.getAnnotation(TestEngine.Random.BigDecimal.class);
            String minimum = annotation.minimum();
            String maximum = annotation.maximum();
            BigDecimal random = RandomUtils.randomBigDecimal(minimum, maximum);
            FieldSupport.setField(null, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.UUID.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            UUID random = UUID.randomUUID();
            FieldSupport.setField(null, field, random);
        }
    }

    /**
     * Method to clear random values in static member fields
     *
     * @param testClass testClass
     * @throws Throwable Throwable
     */
    public static void clearRandomFields(Class<?> testClass) throws Throwable {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Boolean.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(null, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.Byte.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(null, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Character.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(null, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.Short.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(null, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Integer.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(null, field, null);
        }
        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.Long.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(null, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.Float.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(null, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Double.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(null, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.BigInteger.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(null, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.BigDecimal.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(null, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Predicates.GENERIC_STATIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.UUID.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(null, field, null);
        }
    }

    /**
     * Method to inject member variable fields
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    public static void injectRandomFields(Object testInstance) throws Throwable {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Boolean.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            boolean random = RandomUtils.randomBoolean();
            FieldSupport.setField(testInstance, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.Byte.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Byte annotation = field.getAnnotation(TestEngine.Random.Byte.class);
            byte minimum = annotation.minimum();
            byte maximum = annotation.maximum();
            byte random = (byte) RandomUtils.randomInt(minimum, maximum);
            FieldSupport.setField(testInstance, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Character.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Character annotation =
                    field.getAnnotation(TestEngine.Random.Character.class);
            char minimum = annotation.minimum();
            char maximum = annotation.maximum();
            char random = (char) RandomUtils.randomInt(minimum, maximum);
            FieldSupport.setField(testInstance, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.Short.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Short annotation = field.getAnnotation(TestEngine.Random.Short.class);
            short minimum = annotation.minimum();
            short maximum = annotation.maximum();
            short random = (short) RandomUtils.randomInt(minimum, maximum);
            FieldSupport.setField(testInstance, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Integer.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Integer annotation =
                    field.getAnnotation(TestEngine.Random.Integer.class);
            int minimum = annotation.minimum();
            int maximum = annotation.maximum();
            int random = RandomUtils.randomInt(minimum, maximum);
            FieldSupport.setField(testInstance, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.Long.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Long annotation = field.getAnnotation(TestEngine.Random.Long.class);
            long minimum = annotation.minimum();
            long maximum = annotation.maximum();
            long random = RandomUtils.randomLong(minimum, maximum);
            FieldSupport.setField(testInstance, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.Float.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Float annotation = field.getAnnotation(TestEngine.Random.Float.class);
            float minimum = annotation.minimum();
            float maximum = annotation.maximum();
            float random = RandomUtils.randomFloat(minimum, maximum);
            FieldSupport.setField(testInstance, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Double.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Double annotation =
                    field.getAnnotation(TestEngine.Random.Double.class);
            double minimum = annotation.minimum();
            double maximum = annotation.maximum();
            double random = RandomUtils.randomDouble(minimum, maximum);
            FieldSupport.setField(testInstance, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.BigInteger.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.BigInteger annotation =
                    field.getAnnotation(TestEngine.Random.BigInteger.class);
            String minimum = annotation.minimum();
            String maximum = annotation.maximum();
            BigInteger random = RandomUtils.randomBigInteger(minimum, maximum);
            FieldSupport.setField(testInstance, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.BigDecimal.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.BigDecimal annotation =
                    field.getAnnotation(TestEngine.Random.BigDecimal.class);
            String minimum = annotation.minimum();
            String maximum = annotation.maximum();
            BigDecimal random = RandomUtils.randomBigDecimal(minimum, maximum);
            FieldSupport.setField(testInstance, field, random);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.UUID.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            UUID random = UUID.randomUUID();
            FieldSupport.setField(testInstance, field, random);
        }
    }

    /**
     * Method to clear random member fields
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    public static void clearRandomFields(Object testInstance) throws Throwable {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Boolean.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(testInstance, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.Byte.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(testInstance, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Character.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(testInstance, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.Short.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(testInstance, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Integer.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(testInstance, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.Long.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(testInstance, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.Float.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(testInstance, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Double.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(testInstance, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.BigInteger.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(testInstance, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.BigDecimal.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(testInstance, field, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                Predicates.GENERIC_FIELD.test(field)
                                        && field.isAnnotationPresent(TestEngine.Random.UUID.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            FieldSupport.setField(testInstance, field, null);
        }
    }
}
