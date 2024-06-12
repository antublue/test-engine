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

package org.antublue.test.engine.internal.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import org.antublue.test.engine.api.RandomGenerator;
import org.antublue.test.engine.api.TestEngine;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to process @TestEngine.Random.X annotations */
@SuppressWarnings("PMD.AvoidAccessibilityAlteration")
public class RandomAnnotationUtils {

    /** Constructor */
    private RandomAnnotationUtils() {
        // DO NOTHING
    }

    public static void injectRandomFields(Class<?> testClass) throws Throwable {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Boolean.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            field.set(null, RandomGenerator.nextBoolean());
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.Byte.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Byte annotation = field.getAnnotation(TestEngine.Random.Byte.class);
            byte minimum = annotation.minimum();
            byte maximum = annotation.maximum();
            byte random = (byte) RandomGenerator.nextInteger(minimum, maximum);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(null, String.valueOf(random));
            } else {
                field.set(null, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Character.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Character annotation =
                    field.getAnnotation(TestEngine.Random.Character.class);
            char minimum = annotation.minimum();
            char maximum = annotation.maximum();
            char random = (char) RandomGenerator.nextInteger(minimum, maximum);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(null, String.valueOf(random));
            } else {
                field.set(null, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.Short.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Short annotation = field.getAnnotation(TestEngine.Random.Short.class);
            short minimum = annotation.minimum();
            short maximum = annotation.maximum();
            short random = (short) RandomGenerator.nextInteger(minimum, maximum);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(null, String.valueOf(random));
            } else {
                field.set(null, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Integer.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Integer annotation =
                    field.getAnnotation(TestEngine.Random.Integer.class);
            int minimum = annotation.minimum();
            int maximum = annotation.maximum();
            int random = RandomGenerator.nextInteger(minimum, maximum);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(null, String.valueOf(random));
            } else {
                field.set(null, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.Long.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Long annotation = field.getAnnotation(TestEngine.Random.Long.class);
            long minimum = annotation.minimum();
            long maximum = annotation.maximum();
            long random = RandomGenerator.nextLong(minimum, maximum);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(null, String.valueOf(random));
            } else {
                field.set(null, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.Float.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Float annotation = field.getAnnotation(TestEngine.Random.Float.class);
            float minimum = annotation.minimum();
            float maximum = annotation.maximum();
            float random = RandomGenerator.nextFloat(minimum, maximum);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(null, String.valueOf(random));
            } else {
                field.set(null, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Double.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Double annotation =
                    field.getAnnotation(TestEngine.Random.Double.class);
            double minimum = annotation.minimum();
            double maximum = annotation.maximum();
            double random = RandomGenerator.nextDouble(minimum, maximum);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(null, String.valueOf(random));
            } else {
                field.set(null, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.BigInteger.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.BigInteger annotation =
                    field.getAnnotation(TestEngine.Random.BigInteger.class);
            String minimum = annotation.minimum();
            String maximum = annotation.maximum();
            BigInteger random = RandomGenerator.nextBigInteger(minimum, maximum);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(null, String.valueOf(random));
            } else {
                field.set(null, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.BigDecimal.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.BigDecimal annotation =
                    field.getAnnotation(TestEngine.Random.BigDecimal.class);
            String minimum = annotation.minimum();
            String maximum = annotation.maximum();
            BigDecimal random = RandomGenerator.nextBigDecimal(minimum, maximum);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(null, String.valueOf(random));
            } else {
                field.set(null, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.UUID.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            UUID random = UUID.randomUUID();
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(null, String.valueOf(random));
            } else {
                field.set(null, random);
            }
        }
    }

    public static void clearRandomFields(Class<?> testClass) throws Throwable {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Boolean.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType() == boolean.class) {
                field.set(null, false);
            } else {
                field.set(null, null);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.Byte.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType() == byte.class) {
                field.set(null, 0);
            } else {
                field.set(null, null);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Character.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType() == char.class) {
                field.set(null, 0);
            } else {
                field.set(null, null);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.Short.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType() == short.class) {
                field.set(null, 0);
            } else {
                field.set(null, null);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Integer.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType() == int.class) {
                field.set(null, 0);
            } else {
                field.set(null, null);
            }
        }
        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.Long.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType() == long.class) {
                field.set(null, 0);
            } else {
                field.set(null, null);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.Float.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType() == float.class) {
                field.set(null, 0F);
            } else {
                field.set(null, null);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Double.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType() == double.class) {
                field.set(null, 0D);
            } else {
                field.set(null, null);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.BigInteger.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            field.set(null, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.BigDecimal.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            field.set(null, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.UUID.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            field.set(null, null);
        }
    }

    public static void injectRandomFields(Object testInstance) throws Throwable {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Boolean.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            boolean random = RandomGenerator.nextBoolean();
            if (field.getType().equals(String.class)) {
                field.set(testInstance, String.valueOf(random));
            } else {
                field.set(testInstance, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.Byte.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Byte annotation = field.getAnnotation(TestEngine.Random.Byte.class);
            byte minimum = annotation.minimum();
            byte maximum = annotation.maximum();
            byte random = (byte) RandomGenerator.nextInteger(minimum, maximum);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(testInstance, String.valueOf(random));
            } else {
                field.set(testInstance, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Character.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Character annotation =
                    field.getAnnotation(TestEngine.Random.Character.class);
            char minimum = annotation.minimum();
            char maximum = annotation.maximum();
            char random = (char) RandomGenerator.nextInteger(minimum, maximum);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(testInstance, String.valueOf(random));
            } else {
                field.set(testInstance, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.Short.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Short annotation = field.getAnnotation(TestEngine.Random.Short.class);
            short minimum = annotation.minimum();
            short maximum = annotation.maximum();
            short random = (short) RandomGenerator.nextInteger(minimum, maximum);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(testInstance, String.valueOf(random));
            } else {
                field.set(testInstance, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Integer.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Integer annotation =
                    field.getAnnotation(TestEngine.Random.Integer.class);
            int minimum = annotation.minimum();
            int maximum = annotation.maximum();
            int random = RandomGenerator.nextInteger(minimum, maximum);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(testInstance, String.valueOf(random));
            } else {
                field.set(testInstance, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.Long.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Long annotation = field.getAnnotation(TestEngine.Random.Long.class);
            long minimum = annotation.minimum();
            long maximum = annotation.maximum();
            long random = RandomGenerator.nextLong(minimum, maximum);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(testInstance, String.valueOf(random));
            } else {
                field.set(testInstance, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.Float.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Float annotation = field.getAnnotation(TestEngine.Random.Float.class);
            float minimum = annotation.minimum();
            float maximum = annotation.maximum();
            float random = RandomGenerator.nextFloat(minimum, maximum);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(testInstance, String.valueOf(random));
            } else {
                field.set(testInstance, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Double.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.Double annotation =
                    field.getAnnotation(TestEngine.Random.Double.class);
            double minimum = annotation.minimum();
            double maximum = annotation.maximum();
            double random = RandomGenerator.nextDouble(minimum, maximum);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(testInstance, String.valueOf(random));
            } else {
                field.set(testInstance, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.BigInteger.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.BigInteger annotation =
                    field.getAnnotation(TestEngine.Random.BigInteger.class);
            String minimum = annotation.minimum();
            String maximum = annotation.maximum();
            BigInteger random = RandomGenerator.nextBigInteger(minimum, maximum);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(testInstance, String.valueOf(random));
            } else {
                field.set(testInstance, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.BigDecimal.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            TestEngine.Random.BigDecimal annotation =
                    field.getAnnotation(TestEngine.Random.BigDecimal.class);
            String minimum = annotation.minimum();
            String maximum = annotation.maximum();
            BigDecimal random = RandomGenerator.nextBigDecimal(minimum, maximum);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(testInstance, String.valueOf(random));
            } else {
                field.set(testInstance, random);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.UUID.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            UUID random = UUID.randomUUID();
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(testInstance, String.valueOf(random));
            } else {
                field.set(testInstance, random);
            }
        }
    }

    public static void clearRandomFields(Object testInstance) throws Throwable {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Boolean.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType().equals(boolean.class)) {
                field.set(testInstance, false);
            } else {
                field.set(testInstance, null);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.Byte.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType().equals(byte.class)) {
                field.set(testInstance, 0);
            } else {
                field.set(testInstance, null);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Character.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType().equals(char.class)) {
                field.set(testInstance, 0);
            } else {
                field.set(testInstance, null);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.Short.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType().equals(short.class)) {
                field.set(testInstance, 0);
            } else {
                field.set(testInstance, null);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Integer.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType() == int.class) {
                field.set(testInstance, 0);
            } else {
                field.set(testInstance, null);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.Long.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType() == long.class) {
                field.set(testInstance, 0);
            } else {
                field.set(testInstance, null);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.Float.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType() == float.class) {
                field.set(testInstance, 0);
            } else {
                field.set(testInstance, null);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.Double.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType() == double.class) {
                field.set(testInstance, 0);
            } else {
                field.set(testInstance, null);
            }
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.BigInteger.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            field.set(testInstance, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(
                                                TestEngine.Random.BigDecimal.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            field.set(testInstance, null);
        }

        fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Random.UUID.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            field.set(testInstance, null);
        }
    }
}
