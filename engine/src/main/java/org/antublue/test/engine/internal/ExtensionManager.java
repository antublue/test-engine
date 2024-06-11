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

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.antublue.test.engine.Constants;
import org.antublue.test.engine.api.Configuration;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestClassDefinitionException;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.ReflectionUtils;
import org.antublue.test.engine.internal.util.ThrowableContext;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to implement an ExtensionProcessor */
@SuppressWarnings({"unchecked", "PMD.UnusedPrivateMethod"})
public class ExtensionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionManager.class);

    private static final Configuration CONFIGURATION = ContextImpl.getInstance().getConfiguration();

    private static final List<Extension> EMPTY_EXTENSION_LIST = new ArrayList<>();

    private final List<Extension> globalExtensions;
    private final List<Extension> globalExtensionsReversed;
    private final Map<Class<?>, List<Extension>> testExtensionsMap;
    private final Map<Class<?>, List<Extension>> testExtensionsReversedMap;
    private final ReentrantLock lock;

    private boolean initialized;

    /** Predicate to test of a method is an extension supplier method */
    public static final Predicate<Method> EXTENSION_SUPPLIER_METHOD =
            method ->
                    method.isAnnotationPresent(TestEngine.ExtensionSupplier.class)
                            && !method.isAnnotationPresent(TestEngine.Disabled.class)
                            && ReflectionUtils.isStatic(method)
                            && (ReflectionUtils.isPublic(method)
                                    || ReflectionUtils.isProtected(method))
                            && ReflectionUtils.hasParameterCount(method, 0)
                            && (ReflectionUtils.hasReturnType(method, Stream.class)
                                    || ReflectionUtils.hasReturnType(method, Iterable.class));

    /** Constructor */
    private ExtensionManager() {
        globalExtensions = new ArrayList<>();
        globalExtensionsReversed = new ArrayList<>();
        testExtensionsMap = new HashMap<>();
        testExtensionsReversedMap = new HashMap<>();
        lock = new ReentrantLock(true);
    }

    /**
     * Method to get the singleton extension manager
     *
     * @return the singleton extension manager
     */
    public static ExtensionManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Method to load configured global extensions
     *
     * @throws Throwable Throwable
     */
    private void initialize() throws Throwable {
        LOGGER.trace("initialize()");
        try {
            lock.lock();
            if (!initialized) {
                Map<String, Extension> extensionMap = new LinkedHashMap<>();
                Optional<String> optional = CONFIGURATION.getProperty(Constants.EXTENSIONS);
                if (optional.isPresent() && !optional.get().trim().isEmpty()) {
                    String[] classNames = optional.get().split("\\s+");
                    for (String className : classNames) {
                        LOGGER.trace("loading extension [%s]", className);
                        if (!extensionMap.containsKey(className)) {
                            Class<?> clazz =
                                    Thread.currentThread()
                                            .getContextClassLoader()
                                            .loadClass(className);
                            Object object = ReflectionUtils.newInstance(clazz);
                            if (object instanceof Extension) {
                                extensionMap.put(className, (Extension) object);
                            }
                        }
                    }
                    globalExtensions.addAll(extensionMap.values());
                    globalExtensionsReversed.addAll(globalExtensions);
                    Collections.reverse(globalExtensionsReversed);
                }
                initialized = true;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Method to initialize extensions for a test class
     *
     * @param testClass testClass
     */
    private void initialize(Class<?> testClass) {
        LOGGER.trace("initialize() test class [%s]", testClass.getName());

        try {
            lock.lock();
            initialize();
            List<Extension> testExtensions = testExtensionsMap.get(testClass);
            if (testExtensions == null) {
                testExtensions = new ArrayList<>(globalExtensions);
                testExtensions.addAll(buildTestExtensionList(testClass));
                List<Extension> testExtensionReversed = new ArrayList<>(testExtensions);
                Collections.reverse(testExtensionReversed);
                testExtensionsMap.put(testClass, testExtensions);
                testExtensionsReversedMap.put(testClass, testExtensionReversed);
            }
        } catch (Throwable t) {
            throw new TestEngineException(
                    format(
                            "Exception initializing extensions for test class [%s]",
                            testClass.getName()),
                    t);
        } finally {
            lock.unlock();
        }
    }

    public void postTestArgumentDiscoveryCallback(
            Class<?> testClass, List<Named<?>> testArguments, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testClass)) {
            try {
                testExtension.postTestArgumentDiscoveryCallback(testClass, testArguments);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            }
        }
    }

    /**
     * Method to run postTestMethodDiscoveryCallback extension methods
     *
     * @param testClass testClass
     * @param throwableContext throwableContext
     */
    public void postTestMethodDiscoveryCallback(
            Class<?> testClass, List<Method> testMethods, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testClass)) {
            try {
                testExtension.postTestMethodDiscoveryCallback(testClass, testMethods);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            }
        }
    }

    /**
     * Method to run preInstantiateCallback extension methods
     *
     * @param testClass testClass
     * @param throwableContext throwableContext
     */
    public void preInstantiateCallback(Class<?> testClass, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testClass)) {
            try {
                testExtension.preInstantiateCallback(testClass);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            }
        }
    }

    /**
     * Method to run postInstantiateCallback extension methods
     *
     * @param testInstance testInstance
     * @param throwableContext throwableContext
     */
    public void postInstantiateCallback(Object testInstance, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postInstantiateCallback(testInstance);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to run prePrepareMethodsCallback extension methods
     *
     * @param testInstance testInstance
     * @param throwableContext throwableContext
     */
    public void prePrepareMethodsCallback(Object testInstance, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testInstance.getClass())) {
            try {
                testExtension.prePrepareMethodsCallback(testInstance);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to run postPrepareCallback extension methods
     *
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void postPrepareMethodsCallback(Object testInstance, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postPrepareMethodsCallback(testInstance);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to run preBeforeAllMethodsCallback extension methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param throwableContext throwableContext
     */
    public void preBeforeAllMethodsCallback(
            Object testInstance, Named<?> testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testInstance.getClass())) {
            try {
                testExtension.preBeforeAllMethodsCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to run postBeforeAllCallback extension methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param throwableContext throwableCollector
     */
    public void postBeforeAllMethodsCallback(
            Object testInstance, Named<?> testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postBeforeAllMethodsCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to run preBeforeEachMethodsCallback extension methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param throwableContext throwableCollector
     */
    public void preBeforeEachMethodsCallback(
            Object testInstance, Named<?> testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testInstance.getClass())) {
            try {
                testExtension.preBeforeEachMethodsCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to run postBeforeEachCallback extension methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param throwableContext throwableCollector
     */
    public void postBeforeEachMethodsCallback(
            Object testInstance, Named<?> testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postBeforeEachMethodsCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to run preTestCallback extension methods
     *
     * @param testMethod testMethod
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param throwableContext throwableCollector
     */
    public void preTestMethodsCallback(
            Method testMethod,
            Object testInstance,
            Named<?> testArgument,
            ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testInstance.getClass())) {
            try {
                testExtension.preTestMethodsCallback(testMethod, testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to run postAfterTestCallback extension methods
     *
     * @param testMethod testMethod
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param throwableContext throwableCollector
     */
    public void postTestMethodsCallback(
            Method testMethod,
            Object testInstance,
            Named<?> testArgument,
            ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postTestMethodsCallback(testMethod, testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to run preAfterEachMethodsCallback extension methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param throwableContext throwableCollector
     */
    public void preAfterEachMethodsCallback(
            Object testInstance, Named<?> testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testInstance.getClass())) {
            try {
                testExtension.preAfterEachMethodsCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to run postAfterEachCallback extension methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param throwableContext throwableCollector
     */
    public void postAfterEachMethodsCallback(
            Object testInstance, Named<?> testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postAfterEachMethodsCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to run preAfterAllMethodsCallback extension methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param throwableContext throwableCollector
     */
    public void preAfterAllMethodsCallback(
            Object testInstance, Named<?> testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testInstance.getClass())) {
            try {
                testExtension.preAfterAllMethodsCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to run postAfterAllMethodsCallback extension methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param throwableContext throwableCollector
     */
    public void postAfterAllMethodsCallback(
            Object testInstance, Named<?> testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postAfterAllMethodsCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to run preConcludeMethodsCallback extension methods
     *
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void preConcludeMethodsCallback(Object testInstance, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testInstance.getClass())) {
            try {
                testExtension.preConcludeMethodsCallback(testInstance);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to run conclude extension methods
     *
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void postConcludeMethodsCallback(
            Object testInstance, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postConcludeMethodsCallback(testInstance);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to run preDestroy extension methods
     *
     * @param testClass testClass
     * @param testInstance testInstance
     * @param throwableContext throwableContext
     */
    public void preDestroyCallback(
            Class<?> testClass, Object testInstance, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testClass)) {
            try {
                testExtension.preDestroyCallback(testClass, testInstance);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            }
        }
    }

    /**
     * Method to build a list of extensions for a test class
     *
     * @param testClass testClass
     * @return a list of extensions
     * @throws Throwable Throwable
     */
    private List<Extension> buildTestExtensionList(Class<?> testClass) throws Throwable {
        LOGGER.trace("buildTestExtensionList() testClass [%s]", testClass.getName());

        List<Extension> testExtensions = new ArrayList<>();

        List<Method> extensionSupplierMethods =
                ReflectionSupport.findMethods(
                        testClass, EXTENSION_SUPPLIER_METHOD, HierarchyTraversalMode.TOP_DOWN);

        for (Method method : extensionSupplierMethods) {
            Object object = method.invoke(null, (Object[]) null);
            if (object instanceof Stream) {
                Stream<Extension> stream = (Stream<Extension>) object;
                stream.forEach(testExtensions::add);
            } else if (object instanceof Iterable) {
                ((Iterable<Extension>) object).forEach(testExtensions::add);
            } else {
                throw new TestClassDefinitionException(
                        format(
                                "Exception getting extensions for test class [%s]",
                                testClass.getName()));
            }
        }
        return testExtensions;
    }

    private List<Extension> getTestExtensions(Class<?> testClass) {
        initialize(testClass);
        return testExtensionsMap.getOrDefault(testClass, EMPTY_EXTENSION_LIST);
    }

    private List<Extension> getTestExtensionsReversed(Class<?> testClass) {
        initialize(testClass);
        return testExtensionsReversedMap.getOrDefault(testClass, EMPTY_EXTENSION_LIST);
    }

    /** Class to hold the singleton instance */
    private static final class SingletonHolder {

        /** The singleton instance */
        private static final ExtensionManager INSTANCE = new ExtensionManager();
    }
}