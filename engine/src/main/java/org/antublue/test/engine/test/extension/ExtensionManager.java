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

package org.antublue.test.engine.test.extension;

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
import org.antublue.test.engine.api.extension.Extension;
import org.antublue.test.engine.configuration.Configuration;
import org.antublue.test.engine.logger.Logger;
import org.antublue.test.engine.logger.LoggerFactory;
import org.antublue.test.engine.test.ThrowableContext;
import org.antublue.test.engine.util.ReflectionUtils;
import org.antublue.test.engine.util.StandardStreams;

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
            } finally {
                StandardStreams.flush();
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
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run postPrepareCallback extension methods
     *
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void postPrepareCallback(Object testInstance, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postPrepareCallback(testInstance);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            } finally {
                StandardStreams.flush();
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
    public void postBeforeAllCallback(
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postBeforeAllCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            } finally {
                StandardStreams.flush();
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
    public void postBeforeEachCallback(
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postBeforeEachCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run preTestCallback extension methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param testMethod testMethod
     * @param throwableContext throwableCollector
     */
    public void preTestCallback(
            Object testInstance,
            Argument testArgument,
            Method testMethod,
            ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testInstance.getClass())) {
            try {
                testExtension.preTestCallback(testInstance, testArgument, testMethod);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run postAfterTestCallback extension methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param testMethod testMethod
     * @param throwableContext throwableCollector
     */
    public void postAfterTestCallback(
            Object testInstance,
            Argument testArgument,
            Method testMethod,
            ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postTestCallback(testInstance, testArgument, testMethod);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            } finally {
                StandardStreams.flush();
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
    public void postAfterEachCallback(
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postAfterEachCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            } finally {
                StandardStreams.flush();
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
    public void postAfterAllCallback(
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postAfterAllCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run conclude extension methods
     *
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void postConcludeCallback(Object testInstance, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensionsReversed(testInstance.getClass())) {
            try {
                testExtension.postConcludeCallback(testInstance);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            } finally {
                StandardStreams.flush();
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
            } finally {
                StandardStreams.flush();
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
                REFLECTION_UTILS.findMethods(testClass, ExtensionFilters.EXTENSION_SUPPLIER_METHOD);
        for (Method method : extensionSupplierMethods) {
            Object object = method.invoke(null, (Object[]) null);
            if (object instanceof Stream) {
                Stream<Extension> stream = (Stream<Extension>) object;
                stream.forEach(testExtensions::add);
            } else if (object instanceof Iterable) {
                ((Iterable<Extension>) object).forEach(testExtensions::add);
            } else {
                throw new RuntimeException(
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
