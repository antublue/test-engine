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
@SuppressWarnings({"unchecked", "PMD.UnusedPrivateMethod"})
public class ExtensionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionManager.class);

    private static final ReflectionUtils REFLECTION_UTILS = Singleton.get(ReflectionUtils.class);

    private static final Configuration CONFIGURATION = Singleton.get(Configuration.class);

    private static final List<Extension> EMPT_EXTENSION_LIST = new ArrayList<>();

    private List<Extension> globalExtensions;
    private List<Extension> globalExtensionsReversed;

    private final Map<Class<?>, List<Extension>> testExtensionsMap;
    private final Map<Class<?>, List<Extension>> testExtensionsReversedMap;

    /** Constructor */
    public ExtensionManager() {
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
     * Method to run postCreateTestInstance extension methods
     *
     * @param testInstance testInstance
     * @param throwableContext throwableContext
     */
    public void postCreateTestInstance(Object testInstance, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testInstance.getClass())) {
            try {
                testExtension.postCreateTestInstance(testInstance);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run prepare extension methods
     *
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void prepare(Object testInstance, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testInstance.getClass())) {
            try {
                testExtension.prepare(testInstance);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run beforeAll extension methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param throwableContext throwableCollector
     */
    public void beforeAll(
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testInstance.getClass())) {
            try {
                testExtension.beforeAll(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run beforeEach extension methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param throwableContext throwableCollector
     */
    public void beforeEach(
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testInstance.getClass())) {
            try {
                testExtension.beforeEach(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run beforeTest extension methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param testMethod testMethod
     * @param throwableContext throwableCollector
     */
    public void beforeTest(
            Object testInstance,
            Argument testArgument,
            Method testMethod,
            ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testInstance.getClass())) {
            try {
                testExtension.beforeTest(testInstance, testArgument, testMethod);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run afterTest extension methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param testMethod testMethod
     * @param throwableContext throwableCollector
     */
    public void afterTest(
            Object testInstance,
            Argument testArgument,
            Method testMethod,
            ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testInstance.getClass())) {
            try {
                testExtension.afterTest(testInstance, testArgument, testMethod);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run afterEach extension methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param throwableContext throwableCollector
     */
    public void afterEach(
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testInstance.getClass())) {
            try {
                testExtension.afterEach(testInstance, testArgument);
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
    public void afterAll(
            Object testInstance, Argument testArgument, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testInstance.getClass())) {
            try {
                testExtension.afterAll(testInstance, testArgument);
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
    public void conclude(Object testInstance, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testInstance.getClass())) {
            try {
                testExtension.conclude(testInstance);
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
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
