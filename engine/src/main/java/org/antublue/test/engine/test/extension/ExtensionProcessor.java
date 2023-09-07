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
import org.antublue.test.engine.util.Singleton;
import org.antublue.test.engine.util.StandardStreams;

/** Class to implement an ExtensionProcessor */
@SuppressWarnings("unchecked")
public class ExtensionProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionProcessor.class);

    private static final ReflectionUtils REFLECTION_UTILS = Singleton.get(ReflectionUtils.class);

    private static final Configuration CONFIGURATION = Singleton.get(Configuration.class);

    private static final List<Extension> EMPT_EXTENSION_LIST = new ArrayList<>();

    private List<Extension> globalExtensions;
    private List<Extension> globalExtensionsReversed;

    private final Map<Class<?>, List<Extension>> testExtensionsMap;
    private final Map<Class<?>, List<Extension>> testExtensionsReversedMap;

    /** Constructor */
    public ExtensionProcessor() {
        globalExtensions = new ArrayList<>();
        globalExtensionsReversed = new ArrayList<>();
        testExtensionsMap = new HashMap<>();
        testExtensionsReversedMap = new HashMap<>();
    }

    /**
     * Method to load configured global extensions
     *
     * @throws Throwable Throwable
     */
    public void initialize() throws Throwable {
        LOGGER.trace("initialize()");

        synchronized (this) {
            if (globalExtensions == null) {
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
                    globalExtensions = new ArrayList<>(extensionMap.values());
                    globalExtensionsReversed = new ArrayList<>(globalExtensions);
                    Collections.reverse(globalExtensionsReversed);
                }
            }
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
                testExtensions = buildTestExtensionList(globalExtensions, testClass);
                testExtensionsMap.put(testClass, testExtensions);
                List<Extension> testExtensionReversed = new ArrayList<>(testExtensions);
                Collections.reverse(testExtensionReversed);
                testExtensionsReversedMap.put(testClass, testExtensionReversed);
            }
        }
    }

    /**
     * Method to process instantiatedCallback methods
     *
     * @param testClass testClass
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void instantiateCallbacks(
            Class<?> testClass, Object testInstance, ThrowableContext throwableContext) {
        for (Extension testExtensions : getTestExtensions(testClass)) {
            try {
                testExtensions.instantiatedCallback(testInstance);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to process prepareCallback methods
     *
     * @param testClass testClass
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void prepareCallbacks(
            Class<?> testClass, Object testInstance, ThrowableContext throwableContext) {
        for (Extension testExtensions : getTestExtensions(testClass)) {
            try {
                testExtensions.prepareCallback(testInstance);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to process beforeAllCallback methods
     *
     * @param testClass testClass
     * @param testArgument testArgument
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void beforeAllCallbacks(
            Class<?> testClass,
            Argument testArgument,
            Object testInstance,
            ThrowableContext throwableContext) {
        for (Extension testExtensions : getTestExtensions(testClass)) {
            try {
                testExtensions.beforeAllCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to process beforeEachCallback methods
     *
     * @param testClass testClass
     * @param testArgument testArgument
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void beforeEachCallbackMethods(
            Class<?> testClass,
            Argument testArgument,
            Object testInstance,
            ThrowableContext throwableContext) {
        for (Extension testExtensions : getTestExtensions(testClass)) {
            try {
                testExtensions.beforeEachCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to process beforeTestCallback methods
     *
     * @param testClass testClass
     * @param testArgument testArgument
     * @param testMethod testMethod
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void beforeTestCallbacks(
            Class<?> testClass,
            Argument testArgument,
            Method testMethod,
            Object testInstance,
            ThrowableContext throwableContext) {
        for (Extension testExtensions : getTestExtensions(testClass)) {
            try {
                testExtensions.beforeTestCallback(testInstance, testArgument, testMethod);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to process afterTestCallback methods
     *
     * @param testClass testClass
     * @param testArgument testArgument
     * @param testMethod testMethod
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void afterTestCallbacks(
            Class<?> testClass,
            Argument testArgument,
            Method testMethod,
            Object testInstance,
            ThrowableContext throwableContext) {
        for (Extension testExtensions : getTestExtensions(testClass)) {
            try {
                testExtensions.afterTestCallback(testInstance, testArgument, testMethod);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to process afterEachCallback methods
     *
     * @param testClass testClass
     * @param testArgument testArgument
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void afterEachCallbacks(
            Class<?> testClass,
            Argument testArgument,
            Object testInstance,
            ThrowableContext throwableContext) {
        for (Extension testExtensions : getTestExtensions(testClass)) {
            try {
                testExtensions.afterEachCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to process afterAllCallback methods
     *
     * @param testClass testClass
     * @param testArgument testArgument
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void afterAllCallbacks(
            Class<?> testClass,
            Argument testArgument,
            Object testInstance,
            ThrowableContext throwableContext) {
        for (Extension testExtensions : getTestExtensions(testClass)) {
            try {
                testExtensions.afterAllCallback(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to process afterAllCallback methods
     *
     * @param testClass testClass
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void concludeCallbacks(
            Class<?> testClass, Object testInstance, ThrowableContext throwableContext) {
        for (Extension testExtensions : getTestExtensions(testClass)) {
            try {
                testExtensions.concludeCallback(testInstance);
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
     * @param globalExtensions globalExtensions
     * @param testClass testClass
     * @return a list of extensions
     * @throws Throwable Throwable
     */
    private List<Extension> buildTestExtensionList(
            List<Extension> globalExtensions, Class<?> testClass) throws Throwable {
        LOGGER.trace("buildTestExtensionList() testClass [%s]", testClass.getName());

        List<Extension> testExtensions = new ArrayList<>(globalExtensions);
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
        return testExtensionsMap.getOrDefault(testClass, EMPT_EXTENSION_LIST);
    }

    private List<Extension> getTestExtensionsReversed(Class<?> testClass) {
        return testExtensionsReversedMap.getOrDefault(testClass, EMPT_EXTENSION_LIST);
    }
}
