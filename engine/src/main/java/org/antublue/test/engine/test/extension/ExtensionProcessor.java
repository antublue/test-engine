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
import org.antublue.test.engine.util.ThrowableUtils;

/** Class to implement an ExtensionProcessor */
@SuppressWarnings({"unchecked", "PMD.UnusedPrivateMethod"})
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
     * Method to run postCreateTestInstance extension methods
     *
     * @param testClass testClass
     * @param testInstance testInstance
     * @param throwableContext throwableContext
     */
    public void postCreateTestInstance(
            Class<?> testClass, Object testInstance, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testClass)) {
            try {
                testExtension.postCreateTestInstance(testInstance);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run postPrepare extension methods
     *
     * @param testClass testClass
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void postPrepare(
            Class<?> testClass, Object testInstance, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testClass)) {
            try {
                testExtension.postPrepare(testInstance);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run postBeforeAll extension methods
     *
     * @param testClass testClass
     * @param testArgument testArgument
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void postBeforeAll(
            Class<?> testClass,
            Argument testArgument,
            Object testInstance,
            ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testClass)) {
            try {
                testExtension.postBeforeAll(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run postBeforeEach extension methods
     *
     * @param testClass testClass
     * @param testArgument testArgument
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void postBeforeEach(
            Class<?> testClass,
            Argument testArgument,
            Object testInstance,
            ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testClass)) {
            try {
                testExtension.postBeforeEach(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run preTest extension methods
     *
     * @param testClass testClass
     * @param testArgument testArgument
     * @param testMethod testMethod
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void preTest(
            Class<?> testClass,
            Argument testArgument,
            Method testMethod,
            Object testInstance,
            ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testClass)) {
            try {
                testExtension.preTest(testInstance, testArgument, testMethod);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run postTest extension methods
     *
     * @param testClass testClass
     * @param testArgument testArgument
     * @param testMethod testMethod
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void postTest(
            Class<?> testClass,
            Argument testArgument,
            Method testMethod,
            Object testInstance,
            ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testClass)) {
            try {
                testExtension.postTest(testInstance, testArgument, testMethod);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run postAfterEach extension methods
     *
     * @param testClass testClass
     * @param testArgument testArgument
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void postAfterEach(
            Class<?> testClass,
            Argument testArgument,
            Object testInstance,
            ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testClass)) {
            try {
                testExtension.postAfterEach(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run postAfterAll extensions
     *
     * @param testClass testClass
     * @param testArgument testArgument
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void postAfterAll(
            Class<?> testClass,
            Argument testArgument,
            Object testInstance,
            ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testClass)) {
            try {
                testExtension.postAfterAll(testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run postConclude extension methods
     *
     * @param testClass testClass
     * @param testInstance testInstance
     * @param throwableContext throwableCollector
     */
    public void postConclude(
            Class<?> testClass, Object testInstance, ThrowableContext throwableContext) {
        for (Extension testExtension : getTestExtensions(testClass)) {
            try {
                testExtension.postConclude(testInstance);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                StandardStreams.flush();
            }
        }
    }

    /**
     * Method to run VALIDATE extension methods
     *
     * @param testClass testClass
     * @param testInstance testInstance
     */
    public void VALIDATE(Class<?> testClass, Object testInstance) {
        for (Extension testExtension : getTestExtensions(testClass)) {
            try {
                List<Method> methods =
                        REFLECTION_UTILS.findMethods(
                                testExtension.getClass(),
                                method ->
                                        REFLECTION_UTILS.hasReturnType(method, Void.class)
                                                && !REFLECTION_UTILS.isStatic(method)
                                                && (REFLECTION_UTILS.isProtected(method)
                                                        || REFLECTION_UTILS.isPublic(method))
                                                && method.getName().equals("VALIDATE")
                                                && REFLECTION_UTILS.acceptsArguments(
                                                        method, Class.class, Object.class));
                for (Method method : methods) {
                    try {
                        method.invoke(testExtension, testClass, testInstance);
                    } catch (Throwable t) {
                        Throwable prunedThrowable = ThrowableUtils.prune(testClass, t);
                        prunedThrowable.printStackTrace();
                    }
                }
            } catch (Throwable t) {
                Throwable prunedThrowable = ThrowableUtils.prune(testClass, t);
                prunedThrowable.printStackTrace();
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
