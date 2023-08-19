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

package org.antublue.test.engine.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to implement methods to get test class fields / methods, caching the results */
@SuppressWarnings({
    "unchecked",
    "PMD.NPathComplexity",
    "PMD.AvoidAccessibilityAlteration",
    "PMD.EmptyCatchBlock"
})
public final class ReflectionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtils.class);

    private static final Class<?>[] NO_CLASS_ARGS = null;

    private static final Object[] NO_OBJECT_ARGS = null;

    private static final ReflectionUtils SINGLETON = new ReflectionUtils();

    private enum Scope {
        STATIC,
        NON_STATIC
    }

    private final Map<Class<?>, Method> ARGUMENT_SUPPLIER_METHOD_CACHE = new HashMap<>();
    private final Map<Class<?>, Optional<Field>> ARGUMENT_FIELD_CACHE = new HashMap<>();
    private final Map<Class<?>, List<Field>> AUTO_CLOSE_FIELD_CACHE = new HashMap<>();
    private final Map<Class<?>, List<Method>> PREPARE_METHOD_CACHE = new HashMap<>();
    private final Map<Class<?>, List<Method>> BEFORE_ALL_METHOD_CACHE = new HashMap<>();
    private final Map<Class<?>, List<Method>> BEFORE_EACH_METHOD_CACHE = new HashMap<>();
    private final Map<Class<?>, List<Method>> TEST_METHOD_CACHE = new HashMap<>();
    private final Map<Class<?>, List<Method>> AFTER_EACH_METHOD_CACHE = new HashMap<>();
    private final Map<Class<?>, List<Method>> AFTER_ALL_METHOD_CACHE = new HashMap<>();
    private final Map<Class<?>, List<Method>> CONCLUDE_METHOD_CACHE = new HashMap<>();

    /** Constructor */
    private ReflectionUtils() {
        // DO NOTHING
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static ReflectionUtils singleton() {
        return SINGLETON;
    }

    /**
     * Method to find all classes for a URI
     *
     * @param uri uri
     * @return the return value
     */
    public List<Class<?>> findAllClasses(URI uri) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("findAllClasses uri [%s]", uri.toASCIIString());
        }

        List<Class<?>> classes =
                org.junit.platform.commons.util.ReflectionUtils.findAllClassesInClasspathRoot(
                        uri, classFilter -> true, classNameFilter -> true);

        classes = new ArrayList<>(classes);
        sortClasses(classes);
        validateDistinctOrder(classes);

        return classes;
    }

    /**
     * Method to find all classes with a package name
     *
     * @param packageName packageName
     * @return the return value
     */
    public List<Class<?>> findAllClasses(String packageName) {
        LOGGER.trace("findAllClasses package name [%s]", packageName);

        List<Class<?>> classes =
                ReflectionSupport.findAllClassesInPackage(
                        packageName, classFilter -> true, classNameFilter -> true);

        classes = new ArrayList<>(classes);
        sortClasses(classes);
        validateDistinctOrder(classes);

        return classes;
    }

    /**
     * Method to return if a Method accepts an Argument
     *
     * @param method method
     * @param argument argument
     * @return the return value
     */
    public boolean acceptsArgument(Method method, Argument argument) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        return parameterTypes.length == 1
                && parameterTypes[0].isAssignableFrom(argument.getClass());
    }

    /**
     * Method to get a @TestEngine.ArgumentSupplier Method
     *
     * @param clazz class to inspect
     * @return Method the return value
     */
    public Method getArgumentSupplierMethod(Class<?> clazz) {
        synchronized (ARGUMENT_SUPPLIER_METHOD_CACHE) {
            LOGGER.trace("getArgumentSupplierMethod class [%s]", clazz.getName());

            if (ARGUMENT_SUPPLIER_METHOD_CACHE.containsKey(clazz)) {
                return ARGUMENT_SUPPLIER_METHOD_CACHE.get(clazz);
            }

            List<Method> methodList =
                    getMethodsSubclassFirst(
                            clazz,
                            TestEngine.ArgumentSupplier.class,
                            Scope.STATIC,
                            Stream.class,
                            NO_CLASS_ARGS);

            if (methodList.isEmpty()) {
                methodList =
                        getMethodsSuperclassFirst(
                                clazz,
                                TestEngine.ArgumentSupplier.class,
                                Scope.STATIC,
                                Iterable.class,
                                NO_CLASS_ARGS);
            }

            LOGGER.trace(
                    "@TestEngine.ArgumentSupplier class [%s] method count [%d]",
                    clazz.getName(), methodList.size());

            if (methodList.size() != 1) {
                throw new TestClassConfigurationException(
                        String.format(
                                "Test class [%s] must define exactly 1 @TestEngine.ArgumentSupplier"
                                        + " method, %d methods were found",
                                clazz.getName(), methodList.size()));
            }

            Method method = methodList.get(0);
            ARGUMENT_SUPPLIER_METHOD_CACHE.put(clazz, method);

            return method;
        }
    }

    /**
     * Method to get a List of Arguments for a Class
     *
     * @param clazz class to inspect
     * @return list of Arguments
     */
    public List<Argument> getArgumentsList(Class<?> clazz) {
        LOGGER.trace("getArgumentsList class [%s]", clazz.getName());

        try {
            Method method = getArgumentSupplierMethod(clazz);
            Object object = method.invoke(null, NO_OBJECT_ARGS);
            if (object instanceof Stream) {
                List<Argument> arguments = ((Stream<Argument>) object).collect(Collectors.toList());
                LOGGER.trace("class [%s] argument count [%d]", clazz.getName(), arguments.size());
                return arguments;
            } else if (object instanceof Iterable) {
                List<Argument> arguments = new ArrayList<>();
                ((Iterable<Argument>) object).forEach(arguments::add);
                LOGGER.trace("class [%s] argument count [%d]", clazz.getName(), arguments.size());
                return arguments;
            } else {
                throw new TestClassConfigurationException(
                        String.format(
                                "Test class [%s] must define one @TestEngine.ArgumentSupplier"
                                        + " method",
                                clazz.getName()));
            }
        } catch (TestClassConfigurationException e) {
            throw e;
        } catch (Throwable t) {
            throw new TestClassConfigurationException(
                    String.format(
                            "Can't get Stream<Argument> or Iterable<Argument> from test class [%s]",
                            clazz.getName()),
                    t);
        }
    }

    /**
     * Method to get a List of @TestEngine.Prepare Methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public List<Method> getPrepareMethods(Class<?> clazz) {
        synchronized (PREPARE_METHOD_CACHE) {
            LOGGER.trace("getPrepareMethods class [%s]", clazz.getName());

            if (PREPARE_METHOD_CACHE.containsKey(clazz)) {
                return PREPARE_METHOD_CACHE.get(clazz);
            }

            List<Method> methods =
                    getMethodsSuperclassFirst(
                                    clazz,
                                    TestEngine.Prepare.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    NO_CLASS_ARGS)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList());

            LOGGER.trace(
                    "@TestEngine.Prepare class [%s] method count [%d]",
                    clazz.getName(), methods.size());

            methods = Collections.unmodifiableList(methods);
            PREPARE_METHOD_CACHE.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get @TestEngine.Argument Field
     *
     * @param clazz class to inspect
     * @return Optional that contains an @TestEngine.Argument annotated Field if it exists or is
     *     empty
     */
    public Optional<Field> getArgumentField(Class<?> clazz) {
        synchronized (ARGUMENT_FIELD_CACHE) {
            LOGGER.trace("getArgumentField class [%s]", clazz.getName());

            if (ARGUMENT_FIELD_CACHE.containsKey(clazz)) {
                return ARGUMENT_FIELD_CACHE.get(clazz);
            }

            List<Field> argumentFields =
                    getFields(clazz, TestEngine.Argument.class, Argument.class);

            LOGGER.trace(
                    "@TestEngine.Argument class [%s] field count [%d]",
                    clazz.getName(), argumentFields.size());

            Field field;
            Optional<Field> optionalField;

            if (argumentFields.isEmpty()) {
                optionalField = Optional.empty();
                ARGUMENT_FIELD_CACHE.put(clazz, optionalField);
            } else {
                field = argumentFields.get(0);
                optionalField = Optional.of(field);
                ARGUMENT_FIELD_CACHE.put(clazz, optionalField);
            }

            return optionalField;
        }
    }

    /**
     * Method to get a List of @TestEngine.AutoClose Fields
     *
     * @param clazz class to inspect
     * @return List of Fields
     */
    public List<Field> getAutoCloseFields(Class<?> clazz) {
        synchronized (AUTO_CLOSE_FIELD_CACHE) {
            LOGGER.trace("getAutoCloseFields class [%s]", clazz.getName());

            if (AUTO_CLOSE_FIELD_CACHE.containsKey(clazz)) {
                return AUTO_CLOSE_FIELD_CACHE.get(clazz);
            }

            List<Field> autoCloseFields =
                    getFields(clazz, TestEngine.AutoClose.class, Object.class);

            LOGGER.trace(
                    "@TestEngine.AutoClose class [%s] field count [%d]",
                    clazz.getName(), autoCloseFields.size());

            AUTO_CLOSE_FIELD_CACHE.put(clazz, autoCloseFields);

            return autoCloseFields;
        }
    }

    /**
     * Method to get a List of @TestEngine.BeforeAll Methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public List<Method> getBeforeAllMethods(Class<?> clazz) {
        synchronized (BEFORE_ALL_METHOD_CACHE) {
            LOGGER.trace("getBeforeAllMethods class [%s]", clazz.getName());

            if (BEFORE_ALL_METHOD_CACHE.containsKey(clazz)) {
                return BEFORE_ALL_METHOD_CACHE.get(clazz);
            }

            List<Method> methods =
                    getMethodsSuperclassFirst(
                                    clazz,
                                    TestEngine.BeforeAll.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    NO_CLASS_ARGS)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList());

            methods.addAll(
                    getMethodsSuperclassFirst(
                                    clazz,
                                    TestEngine.BeforeAll.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    Argument.class)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList()));

            LOGGER.trace(
                    "@TestEngine.BeforeAll class [%s] method count [%d]",
                    clazz.getName(), methods.size());

            methods = Collections.unmodifiableList(methods);
            BEFORE_ALL_METHOD_CACHE.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a List of @TestEngine.BeforeEach Methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public List<Method> getBeforeEachMethods(Class<?> clazz) {
        synchronized (BEFORE_EACH_METHOD_CACHE) {
            LOGGER.trace("getBeforeEachMethods class [%s]", clazz.getName());

            if (BEFORE_EACH_METHOD_CACHE.containsKey(clazz)) {
                return BEFORE_EACH_METHOD_CACHE.get(clazz);
            }

            List<Method> methods =
                    getMethodsSuperclassFirst(
                                    clazz,
                                    TestEngine.BeforeEach.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    NO_CLASS_ARGS)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList());

            methods.addAll(
                    getMethodsSuperclassFirst(
                                    clazz,
                                    TestEngine.BeforeEach.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    Argument.class)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList()));

            LOGGER.trace(
                    "@TestEngine.BeforeEach class [%s] method count [%d]",
                    clazz.getName(), methods.size());

            methods = Collections.unmodifiableList(methods);
            BEFORE_EACH_METHOD_CACHE.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a List of @TestEngine.Test Methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public List<Method> getTestMethods(Class<?> clazz) {
        synchronized (TEST_METHOD_CACHE) {
            LOGGER.trace("getTestMethods class [%s]", clazz.getName());

            if (TEST_METHOD_CACHE.containsKey(clazz)) {
                return new ArrayList<>(TEST_METHOD_CACHE.get(clazz));
            }

            List<Method> methods =
                    getMethodsSuperclassFirst(
                                    clazz,
                                    TestEngine.Test.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    NO_CLASS_ARGS)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList());

            methods.addAll(
                    getMethodsSuperclassFirst(
                                    clazz,
                                    TestEngine.Test.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    Argument.class)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList()));

            LOGGER.trace(
                    "@TestEngine.Test class [%s] method count [%d]",
                    clazz.getName(), methods.size());

            methods = Collections.unmodifiableList(methods);

            TEST_METHOD_CACHE.put(clazz, methods);

            return new ArrayList<>(methods);
        }
    }

    /**
     * Method to get a List of @TestEngine.AfterEach Methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public List<Method> getAfterEachMethods(Class<?> clazz) {
        synchronized (AFTER_EACH_METHOD_CACHE) {
            LOGGER.trace("getAfterEachMethods class [%s]", clazz.getName());

            if (AFTER_EACH_METHOD_CACHE.containsKey(clazz)) {
                return AFTER_EACH_METHOD_CACHE.get(clazz);
            }

            List<Method> methods =
                    getMethodsSubclassFirst(
                                    clazz,
                                    TestEngine.AfterEach.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    NO_CLASS_ARGS)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList());

            methods.addAll(
                    getMethodsSubclassFirst(
                                    clazz,
                                    TestEngine.AfterEach.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    Argument.class)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList()));

            LOGGER.trace(
                    "@TestEngine.AfterEach class [%s] method count [%d]",
                    clazz.getName(), methods.size());

            methods = Collections.unmodifiableList(methods);
            AFTER_EACH_METHOD_CACHE.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a List of @TestEngine.AfterAll Methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public List<Method> getAfterAllMethods(Class<?> clazz) {
        synchronized (AFTER_ALL_METHOD_CACHE) {
            LOGGER.trace("getAfterAllMethods class [%s]", clazz.getName());

            if (AFTER_ALL_METHOD_CACHE.containsKey(clazz)) {
                return AFTER_ALL_METHOD_CACHE.get(clazz);
            }

            List<Method> methods =
                    getMethodsSubclassFirst(
                                    clazz,
                                    TestEngine.AfterAll.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    NO_CLASS_ARGS)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList());

            methods.addAll(
                    getMethodsSubclassFirst(
                                    clazz,
                                    TestEngine.AfterAll.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    Argument.class)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList()));

            LOGGER.trace(
                    "@TestEngine.AfterAll class [%s] method count [%d]",
                    clazz.getName(), methods.size());

            methods = Collections.unmodifiableList(methods);
            AFTER_ALL_METHOD_CACHE.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a @TestEngine.Conclude Methods
     *
     * @param clazz class to inspect
     * @return Method the return value
     */
    public List<Method> getConcludeMethods(Class<?> clazz) {
        synchronized (CONCLUDE_METHOD_CACHE) {
            LOGGER.trace("getConcludeMethods class [%s]", clazz.getName());

            if (CONCLUDE_METHOD_CACHE.containsKey(clazz)) {
                return CONCLUDE_METHOD_CACHE.get(clazz);
            }

            List<Method> methods =
                    getMethodsSubclassFirst(
                                    clazz,
                                    TestEngine.Conclude.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    NO_CLASS_ARGS)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList());

            LOGGER.trace(
                    "@TestEngine.Conclude class [%s] method count [%d]",
                    clazz.getName(), methods.size());

            methods = Collections.unmodifiableList(methods);
            CONCLUDE_METHOD_CACHE.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a test method display name
     *
     * @param method method to get the display name for
     * @return the display name
     */
    public String getDisplayName(Method method) {
        String displayName = method.getName();

        TestEngine.DisplayName annotation = method.getAnnotation(TestEngine.DisplayName.class);
        if (annotation != null) {
            String name = annotation.name();
            if (name != null && !name.trim().isEmpty()) {
                displayName = name.trim();
            }
        }

        LOGGER.trace("getDisplayName method [%s] display name [%s]", method.getName(), displayName);

        return displayName;
    }

    /**
     * Method to get a test method display name
     *
     * @param clazz class to get the display name for
     * @return the display name
     */
    public String getDisplayName(Class<?> clazz) {
        String displayName = clazz.getName();

        TestEngine.DisplayName annotation = clazz.getAnnotation(TestEngine.DisplayName.class);
        if (annotation != null) {
            String name = annotation.name();
            if (name != null && !name.trim().isEmpty()) {
                displayName = name.trim();
            }
        }

        LOGGER.trace("getDisplayName class [%s] display name [%s]", clazz.getName(), displayName);

        return displayName;
    }

    /**
     * Method to get a List of all fields from a Class and super Classes
     *
     * @param clazz class to inspect
     * @param annotation annotation that is required
     * @param fieldType field type that is required
     * @return list of Fields
     */
    private List<Field> getFields(
            Class<?> clazz, Class<? extends Annotation> annotation, Class<?> fieldType) {
        LOGGER.trace(
                "getFields class [%s] annotation [%s] fieldType [%s]",
                clazz.getName(), annotation.getName(), fieldType.getName());

        Set<Field> fieldSet = new LinkedHashSet<>();
        resolveFields(clazz, annotation, fieldType, fieldSet);
        List<Field> fields = new ArrayList<>(fieldSet);
        fields.sort(Comparator.comparing(Field::getName));

        LOGGER.trace("class [%s] argument field count [%d]", clazz.getName(), fieldSet.size());

        return fields;
    }

    /**
     * Method to recursively resolve Fields
     *
     * @param clazz class to inspect
     * @param annotation annotation that is required
     * @param fieldType field type that is required
     * @param fieldSet set of Fields
     */
    private void resolveFields(
            Class<?> clazz,
            Class<? extends Annotation> annotation,
            Class<?> fieldType,
            Set<Field> fieldSet) {
        LOGGER.trace(
                "resolveFields class [%s] annotation [%s] fieldType [%s]",
                clazz.getName(), annotation.getName(), fieldType.getName());

        List<Field> fields =
                Stream.of(clazz.getDeclaredFields())
                        .filter(
                                field -> {
                                    int modifiers = field.getModifiers();
                                    return !Modifier.isFinal(modifiers)
                                            && !Modifier.isStatic(modifiers)
                                            && field.isAnnotationPresent(annotation)
                                            && fieldType.isAssignableFrom(field.getType());
                                })
                        .collect(Collectors.toList());

        for (Field field : fields) {
            field.setAccessible(true);
            fieldSet.add(field);
        }

        Class<?> declaringClass = clazz.getSuperclass();
        if (declaringClass != null && !declaringClass.equals(Object.class)) {
            resolveFields(declaringClass, annotation, fieldType, fieldSet);
        }
    }

    /**
     * Method to get a List methods (superclass first)
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    private List<Method> getMethodsSuperclassFirst(
            Class<?> clazz,
            Class<? extends Annotation> annotation,
            Scope scope,
            Class<?> returnType,
            Class<?>... parameterTypes) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "getMethodsSuperclassFirst annotation [%s] class [%s] parameterTypes"
                            + " [%s] returnType [%s]",
                    annotation.getName(),
                    clazz.getName(),
                    toString(parameterTypes),
                    returnType.getName());
        }

        Set<Class<?>> classes = new LinkedHashSet<>();
        buildClassSetSuperclassFirst(clazz, classes);

        Map<String, Method> methods = new LinkedHashMap<>();
        for (Class<?> inspectClass : classes) {
            resolveMethodsSuperclassFirst(
                    inspectClass, annotation, scope, returnType, parameterTypes, methods);
        }

        return new ArrayList<>(methods.values());
    }

    /**
     * Method to get a List classes (superclass first)
     *
     * @param clazz class to inspect
     * @param classes set of Classes
     */
    private void buildClassSetSuperclassFirst(Class<?> clazz, Set<Class<?>> classes) {
        LOGGER.trace("buildClassSetSuperclassFirst class [%s]", clazz.getName());

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && !superclass.equals(Object.class)) {
            buildClassSetSuperclassFirst(superclass, classes);
        }

        classes.add(clazz);
    }

    /**
     * Method to get a List of all methods (subclass first)
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    private List<Method> getMethodsSubclassFirst(
            Class<?> clazz,
            Class<? extends Annotation> annotation,
            Scope scope,
            Class<?> returnType,
            Class<?>... parameterTypes) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "getMethodsSubclassFirst annotation [%s] class [%s] parameterTypes [%s]"
                            + " returnType [%s]",
                    annotation.getName(),
                    clazz.getName(),
                    toString(parameterTypes),
                    returnType.getName());
        }

        Set<Class<?>> classes = new LinkedHashSet<>();
        buildClassSetSubclassFirst(clazz, classes);

        Map<String, Method> methods = new LinkedHashMap<>();
        for (Class<?> inspectClass : classes) {
            resolveMethodsSuperclassFirst(
                    inspectClass, annotation, scope, returnType, parameterTypes, methods);
        }

        return new ArrayList<>(methods.values());
    }

    /**
     * Method to build a set of classes subclass before superclass
     *
     * @param clazz class to inspect
     * @param classes set of Classes
     */
    private void buildClassSetSubclassFirst(Class<?> clazz, Set<Class<?>> classes) {
        LOGGER.trace("buildClassSetSubclassFirst class [%s]", clazz.getName());

        classes.add(clazz);

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && !superclass.equals(Object.class)) {
            buildClassSetSuperclassFirst(superclass, classes);
        }
    }

    /**
     * Method to recursively resolve Methods (superclass first)
     *
     * @param clazz class to inspect
     * @param annotation annotation that is required
     * @param scope method scope that is required
     * @param returnType method return type that is required
     * @param parameterTypes parameter types that are required
     * @param methods Set of methods
     */
    private void resolveMethodsSuperclassFirst(
            Class<?> clazz,
            Class<? extends Annotation> annotation,
            Scope scope,
            Class<?> returnType,
            Class<?>[] parameterTypes,
            Map<String, Method> methods) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "resolveMethodsSuperclassFirst annotation [%s] class [%s]"
                            + " parameterTypes [%s] returnType [%s]",
                    annotation.getName(),
                    clazz.getName(),
                    toString(parameterTypes),
                    returnType.getName());
        }

        try {
            List<Method> methodList =
                    Stream.of(clazz.getDeclaredMethods())
                            .filter(method -> method.isAnnotationPresent(annotation))
                            .filter(
                                    method -> {
                                        int modifiers = method.getModifiers();
                                        return Modifier.isPublic(modifiers)
                                                || Modifier.isProtected(modifiers);
                                    })
                            .filter(
                                    method -> {
                                        int modifiers = method.getModifiers();
                                        if (scope == Scope.STATIC
                                                && !Modifier.isStatic(modifiers)) {
                                            throw new TestClassConfigurationException(
                                                    String.format(
                                                            "%s method [%s] must be declared"
                                                                    + " static",
                                                            getAnnotationDisplayName(annotation),
                                                            method.getName()));
                                        } else if (scope != Scope.STATIC
                                                && Modifier.isStatic(modifiers)) {
                                            throw new TestClassConfigurationException(
                                                    String.format(
                                                            "%s method [%s] must be not be declared"
                                                                    + " static",
                                                            getAnnotationDisplayName(annotation),
                                                            method.getName()));
                                        } else {
                                            return true;
                                        }
                                    })
                            .filter(
                                    method -> {
                                        if (parameterTypes == null) {
                                            return method.getParameterTypes().length == 0;
                                        }
                                        if (parameterTypes.length != method.getParameterCount()) {
                                            return false;
                                        }
                                        Class<?>[] methodParameterTypes =
                                                method.getParameterTypes();
                                        for (int i = 0; i < parameterTypes.length; i++) {
                                            if (!parameterTypes[i].isAssignableFrom(
                                                    methodParameterTypes[i])) {
                                                return false;
                                            }
                                        }
                                        return true;
                                    })
                            .filter(
                                    method -> {
                                        if (returnType == Void.class) {
                                            return method.getReturnType().getName().equals("void");
                                        } else {
                                            return returnType.isAssignableFrom(
                                                    method.getReturnType());
                                        }
                                    })
                            .collect(Collectors.toList());

            for (Method method : methodList) {
                method.setAccessible(true);
            }

            validateDistinctOrder(clazz, methodList);
            sortMethods(methodList);

            for (Method method : methodList) {
                methods.putIfAbsent(method.getName(), method);
            }
        } catch (NoClassDefFoundError e) {
            // DO NOTHING
        } catch (TestEngineException e) {
            throw e;
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            System.out.flush();
            throw new TestEngineException(
                    String.format("Exception resolving methods class [%s]", clazz.getName()), t);
        }
    }

    /**
     * Method to sort a List of classes first by @TestEngine.Order annotation, then by display name
     * / class name
     *
     * @param classes list of Classes to sort
     */
    private void sortClasses(List<Class<?>> classes) {
        LOGGER.trace("sortClasses count [%s]", classes.size());

        classes.sort(
                (o1, o2) -> {
                    int o1Order = Integer.MAX_VALUE;
                    TestEngine.Order o1Annotation = o1.getAnnotation(TestEngine.Order.class);
                    if (o1Annotation != null) {
                        o1Order = o1Annotation.order();
                    }

                    int o2Order = Integer.MAX_VALUE;
                    TestEngine.Order o2Annotation = o2.getAnnotation(TestEngine.Order.class);
                    if (o2Annotation != null) {
                        o2Order = o2Annotation.order();
                    }

                    if (o1Order != o2Order) {
                        return Integer.compare(o1Order, o2Order);
                    }

                    // Order by display name which is either
                    // the name declared by @TestEngine.DisplayName
                    // or the real method name
                    String o1DisplayName = getDisplayName(o1);
                    String o2DisplayName = getDisplayName(o2);

                    return o1DisplayName.compareTo(o2DisplayName);
                });
    }

    /**
     * Method to validate @TestEngine.Order is unique on classes
     *
     * @param classes classes
     */
    private void validateDistinctOrder(List<Class<?>> classes) {
        LOGGER.trace("validateDistinctOrder count [%s]", classes.size());

        Map<Integer, Class<?>> orderToClassMap = new LinkedHashMap<>();

        for (Class<?> clazz : classes) {
            if (!clazz.isAnnotationPresent(TestEngine.BaseClass.class)
                    && !Modifier.isAbstract(clazz.getModifiers())
                    && clazz.isAnnotationPresent(TestEngine.Order.class)) {
                int order = clazz.getAnnotation(TestEngine.Order.class).order();
                LOGGER.trace("clazz [%s] order [%d]", clazz.getName(), order);
                if (orderToClassMap.containsKey(order)) {
                    Class<?> existingClass = orderToClassMap.get(order);
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] (or superclass) and test class [%s]"
                                            + " (or superclass) contain duplicate"
                                            + " @TestEngine.Order(%d) class annotation",
                                    existingClass.getName(), clazz.getName(), order));
                } else {
                    orderToClassMap.put(order, clazz);
                }
            }
        }
    }

    /**
     * Method to sort a List of methods first by @TestEngine.Order annotation, then alphabetically
     *
     * @param methods list of Methods to sort
     */
    private void sortMethods(List<Method> methods) {
        LOGGER.trace("sortMethods count [%s]", methods.size());

        methods.sort(
                (o1, o2) -> {
                    int o1Order = Integer.MAX_VALUE;
                    TestEngine.Order o1Annotation = o1.getAnnotation(TestEngine.Order.class);
                    if (o1Annotation != null) {
                        o1Order = o1Annotation.order();
                    }

                    int o2Order = Integer.MAX_VALUE;
                    TestEngine.Order o2Annotation = o2.getAnnotation(TestEngine.Order.class);
                    if (o2Annotation != null) {
                        o2Order = o2Annotation.order();
                    }

                    if (o1Order != o2Order) {
                        return Integer.compare(o1Order, o2Order);
                    }

                    // Order by display name which is either
                    // the name declared by @TestEngine.DisplayName
                    // or the real method name
                    String o1DisplayName = getDisplayName(o1);
                    String o2DisplayName = getDisplayName(o2);

                    return o1DisplayName.compareTo(o2DisplayName);
                });
    }

    /**
     * Method to validate @TestEngine.Order is unique on methods with the same annotation
     *
     * @param clazz clazz
     * @param methods methods
     */
    private void validateDistinctOrder(Class<?> clazz, List<Method> methods) {
        LOGGER.trace(
                "validateDistinctOrder class [%s] count [%s]", clazz.getName(), methods.size());

        Set<Integer> integers = new HashSet<>();

        for (Method method : methods) {
            TestEngine.Order annotation = method.getAnnotation(TestEngine.Order.class);
            if (annotation != null) {
                int value = annotation.order();
                if (integers.contains(value)) {
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] (or superclass) contains a duplicate"
                                            + " @TestEngine.Order(%d) method annotation",
                                    clazz.getName(), value));
                } else {
                    integers.add(value);
                }
            }
        }
    }

    /**
     * Method to get a display name for an Annotation
     *
     * @param annotation to look for
     * @return the display name
     */
    private String getAnnotationDisplayName(Class<? extends Annotation> annotation) {
        String displayName =
                annotation.getDeclaringClass().getSimpleName() + "." + annotation.getSimpleName();

        LOGGER.trace(
                "getAnnotationDisplayName annotation [%s] display name [%s]",
                annotation.getName(), displayName);

        return displayName;
    }

    /**
     * Method to convert an array of parameter types to a loggable string
     *
     * @param parameterTypes the parameter types
     * @return parameters the parameter types as a loggable string
     */
    private String toString(Class<?>[] parameterTypes) {
        StringBuilder stringBuilder = new StringBuilder();

        if (parameterTypes != null) {
            stringBuilder.append("[");

            for (int i = 0; i < parameterTypes.length; i++) {
                if (i > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(parameterTypes[i].getName());
            }

            stringBuilder.append("]");
        } else {
            stringBuilder.append("null");
        }

        return stringBuilder.toString();
    }
}
