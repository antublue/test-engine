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

package org.antublue.test.engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.logger.Logger;
import org.antublue.test.engine.logger.LoggerFactory;
import org.antublue.test.engine.util.ReflectionUtils;

/** Class to implement an ExtensionManager */
public class ExtensionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionManager.class);

    private static final ExtensionManager SINGLETON = new ExtensionManager();

    private final Map<String, Extension> extensionMap;

    /** Constructor */
    private ExtensionManager() {
        extensionMap = new LinkedHashMap<>();
    }

    /**
     * Method to load configured global extensions
     *
     * @throws Throwable Throwable
     */
    public void initialize() throws Throwable {
        LOGGER.trace("initialize()");

        ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

        Optional<String> optional = Configuration.singleton().get(Constants.EXTENSIONS);
        if (optional.isPresent() && !optional.get().trim().isEmpty()) {
            String[] classNames = optional.get().split("\\s+");
            for (String className : classNames) {
                LOGGER.trace("loading extension [%s]", className);
                if (!extensionMap.containsKey(className)) {
                    Object object = reflectionUtils.newInstance(className);
                    if (object instanceof Extension) {
                        extensionMap.putIfAbsent(className, (Extension) object);
                    }
                }
            }
        }

        LOGGER.trace("extension count [%d]", extensionMap.size());
    }

    /**
     * Method to get a list of extensions
     *
     * @return
     */
    public List<Extension> getExtensions() {
        return new ArrayList<>(extensionMap.values());
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static ExtensionManager singleton() {
        return SINGLETON;
    }
}
