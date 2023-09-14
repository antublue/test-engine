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

package org.antublue.test.engine.internal.test.extension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.antublue.test.engine.Constants;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.exception.TestClassDefinitionException;
import org.antublue.test.engine.internal.configuration.Configuration;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.test.util.ReflectionUtils;
import org.antublue.test.engine.internal.test.util.ThrowableContext;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to implement an ExtensionProcessor */
@SuppressWarnings({"unchecked", "PMD.UnusedPrivateMethod"})
public class ExtensionManager {

    private static final ExtensionManager SINGLETON = new ExtensionManager();

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionManager.class);

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.getSingleton();

    private static final Configuration CONFIGURATION = Configuration.getSingleton();

    private static final List<Extension> EMPTY_EXTENSION_LIST = new ArrayList<>();

    private final List<Extension> globalExtensions;
    private final List<Extension> globalExtensionsReversed;
    private final Map<Class<?>, List<Extension>> testExtensionsMap;
    private final Map<Class<?>, List<Extension>> testExtensionsReversedMap;

    private boolean initialized;

    /** Constructor */
    private ExtensionManager() {
        globalExtensions = new ArrayList<>();
        globalExtensionsReversed = new ArrayList<>();
        testExtensionsMap = new HashMap<>();
        testExtensionsReversedMap = new HashMap<>();
    }

    public static ExtensionManager getSingleton() {
        return SINGLETON;
    }

    /**
     * Method to load configured global extensions
     *
     * @throws Throwable Throwable
     */
    public void initialize() throws Throwable {
        LOGGER.trace("initialize()");
        synchronized (this) {
            if (initialized) {
                return;
            }
            Map<String, Extension> extensionMap = new LinkedHashMap<>();
            Optional<String> optional = CONFIGURATION.get(Constants.EXTENSIONS);
            if (optional.isPresent() && !optional.get().trim().isEmpty()) {
                String[] classNames = optional.get().split("\\s+");
                for (String className : classNames) {
                    LOGGER.trace("loading extension [%s]", className);
                    if (!extensionMap.containsKey(className)) {
                        Object object = REFLECTION_UTILS.newInstance(className);
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
    }

    /**
     * Method to initialize extensions for a test class
     *
     * @param testClass testClass
     * @throws Throwable Throwable
     */
    public void initialize(Class<?> testClass) throws Throwable {
        LOGGER.trace("initialize() test class [%s]", testClass.getName());
        synchronized (this) {
            List<Extension> testExtensions = testExtensionsMap.get(testClass);
            if (testExtensions == null) {
                testExtensions = new ArrayList<>(globalExtensions);
                testExtensions.addAll(buildTestExtensionList(testClass));
                List<Extension> testExtensionReversed = new ArrayList<>(testExtensions);
                Collections.reverse(testExtensionReversed);
                testExtensionsMap.put(testClass, testExtensions);
                testExtensionsReversedMap.put(testClass, testExtensionReversed);
            }
        }
    }

    public void postTestMethodDiscovery(
            Class<?> testClass, List<Method> testMethods, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testClass)) {
            try {
                testExtension.postTestMethodDiscovery(testClass, testMethods);
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

    public void postFieldCallback(
            Field testField, Object testInstance, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postFieldCallback(testField, testInstance);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

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

    public void preBeforeAllMethodsCallback(
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
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
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postBeforeAllMethodsCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    public void preBeforeEachMethodsCallback(
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
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
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
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
            Argument testArgument,
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
            Argument testArgument,
            ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postTestMethodsCallback(testMethod, testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    public void preAfterEachMethodsCallback(
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
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
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postAfterEachMethodsCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    public void preAfterAllMethodsCallback(
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testInstance.getClass())) {
            try {
                testExtension.preAfterAllMethodsCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    /**
     * Method to run afterAll extension methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param throwableContext throwableCollector
     */
    public void postAfterAllMethodsCallback(
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postAfterAllMethodsCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

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
     * @param optionalTestInstance optionalTestInstance
     * @param throwableContext throwableContext
     */
    public void preDestroyCallback(
            Class<?> testClass,
            Optional<Object> optionalTestInstance,
            ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testClass)) {
            try {
                testExtension.preDestroyCallback(testClass, optionalTestInstance);
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
                        testClass,
                        ExtensionFilters.EXTENSION_SUPPLIER_METHOD,
                        HierarchyTraversalMode.TOP_DOWN);

        for (Method method : extensionSupplierMethods) {
            Object object = method.invoke(null, (Object[]) null);
            if (object instanceof Stream) {
                Stream<Extension> stream = (Stream<Extension>) object;
                stream.forEach(testExtensions::add);
            } else if (object instanceof Iterable) {
                ((Iterable<Extension>) object).forEach(testExtensions::add);
            } else {
                throw new TestClassDefinitionException(
                        String.format(
                                "Exception getting extensions for test class [%s]",
                                testClass.getName()));
            }
        }
        return testExtensions;
    }

    private List<Extension> getTestExtensions(Class<?> testClass) {
        return testExtensionsMap.getOrDefault(testClass, EMPTY_EXTENSION_LIST);
    }

    private List<Extension> getTestExtensionsReversed(Class<?> testClass) {
        return testExtensionsReversedMap.getOrDefault(testClass, EMPTY_EXTENSION_LIST);
    }
}
