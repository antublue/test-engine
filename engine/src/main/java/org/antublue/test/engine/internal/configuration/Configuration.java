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

package org.antublue.test.engine.internal.configuration;

import static java.lang.String.format;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import org.antublue.test.engine.internal.logger.Level;

/** Class to implement Configuration */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Configuration {

    /** Configuration constant */
    public static final String ANTUBLUE_TEST_ENGINE_CONFIGURATION_TRACE =
            "ANTUBLUE_TEST_ENGINE_CONFIGURATION_TRACE";

    private static final String ANTUBLUE_TEST_ENGINE_PROPERTIES_ENVIRONMENT_VARIABLE =
            "ANTUBLUE_TEST_ENGINE_PROPERTIES";

    private static final String ANTUBLUE_TEST_ENGINE_PROPERTIES_SYSTEM_PROPERTY =
            "antublue.test.engine.properties";

    private static final String ANTUBLUE_TEST_ENGINE_PROPERTIES_FILENAME_1 =
            "antublue-test-engine.properties";

    private static final String ANTUBLUE_TEST_ENGINE_PROPERTIES_FILENAME_2 =
            ".antublue-test-engine.properties";

    private static final boolean IS_TRACE_ENABLED =
            "true".equalsIgnoreCase(System.getenv(ANTUBLUE_TEST_ENGINE_CONFIGURATION_TRACE));

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd | HH:mm:ss.SSS", Locale.getDefault());

    private String propertiesFilename;

    private final Properties properties;

    private Set<String> keySet;

    /** Constructor */
    private Configuration() {
        trace("ConfigurationImpl()");

        properties = new Properties();

        String value = System.getenv(ANTUBLUE_TEST_ENGINE_PROPERTIES_ENVIRONMENT_VARIABLE);
        if (value != null && !value.trim().isEmpty()) {
            propertiesFilename = value.trim();
        }

        if (propertiesFilename == null) {
            value = System.getProperty(ANTUBLUE_TEST_ENGINE_PROPERTIES_SYSTEM_PROPERTY);
            if (value != null && !value.trim().isEmpty()) {
                propertiesFilename = value.trim();
            }
        }

        File propertiesFile = find(Paths.get("."), ANTUBLUE_TEST_ENGINE_PROPERTIES_FILENAME_1);
        if (propertiesFile == null) {
            propertiesFile = find(Paths.get("."), ANTUBLUE_TEST_ENGINE_PROPERTIES_FILENAME_2);
        }

        if (propertiesFile != null) {
            propertiesFilename = propertiesFile.getAbsolutePath();
            trace("loading properties [%s]", propertiesFilename);
            try (Reader reader = new FileReader(propertiesFile)) {
                properties.load(reader);

                Map<String, Boolean> map = new TreeMap<>();
                for (Object key : properties.keySet()) {
                    map.put((String) key, true);
                }
                keySet = map.keySet();
                trace("properties loaded [%s]", propertiesFilename);
            } catch (IOException e) {
                // TODO ?
            }
        }

        if (keySet == null) {
            keySet = new LinkedHashSet<>();
        }
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static Configuration getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Method to get a property
     *
     * @param key key
     * @return an Optional
     */
    public Optional<String> getProperty(String key) {
        String value = properties.getProperty(key);
        trace("getProperty [%s] value [%s]", key, value);
        return Optional.ofNullable(value);
    }

    public <T> Optional<T> getProperty(String key, Function<String, T> transformer) {
        Optional<String> optional = getProperty(key);
        if (!optional.isPresent()) {
            return Optional.empty();
        }
        String value = optional.get();
        return Optional.ofNullable(transformer.apply(value));
    }

    /**
     * Method to get a Set of configuration keys
     *
     * @return a Set of configuration keys
     */
    public Set<String> getPropertyNames() {
        return keySet;
    }

    /**
     * Method to a set of property keys, filtered by a Predicate
     *
     * @param predicate predicate
     * @return a Set of property keys filtered by a Predicate
     */
    public Set<String> getPropertyNames(Predicate<String> predicate) {
        if (predicate == null) {
            return getPropertyNames();
        }

        Set<String> keySet = new HashSet<>();
        for (String key : this.keySet) {
            if (predicate.test(key)) {
                keySet.add(key);
            }
        }

        return keySet;
    }

    /**
     * Method to the properties filename
     *
     * @return the properties filename
     */
    public String getPropertiesFilename() {
        return propertiesFilename;
    }

    /**
     * Method to get the number of configuration properties
     *
     * @return the number of configuration properties
     */
    public int size() {
        return properties.size();
    }

    /**
     * Method to load tracing information
     *
     * @param format format
     * @param objects objects
     */
    private void trace(String format, Object... objects) {
        if (IS_TRACE_ENABLED) {
            System.out.println(createMessage(Level.TRACE, format(format, objects)));
            System.out.flush();
        }
    }

    /**
     * Method to create a log message
     *
     * @param level level
     * @param message message
     * @return the return value
     */
    private String createMessage(Level level, String message) {
        String dateTime;

        synchronized (SIMPLE_DATE_FORMAT) {
            dateTime = SIMPLE_DATE_FORMAT.format(new Date());
        }

        return dateTime
                + " | "
                + Thread.currentThread().getName()
                + " | "
                + level.toString()
                + " | "
                + Configuration.class.getName()
                + " | "
                + message
                + " ";
    }

    /**
     * Method to find a properties file, searching the working directory, then parent directories
     * toward the root
     *
     * @param path path
     * @param filename filename
     * @return a File
     */
    private File find(Path path, String filename) {
        Path currentPath = path.toAbsolutePath().normalize();

        while (true) {
            trace("searching path [%s]", currentPath);

            File propertiesFile =
                    new File(currentPath.toAbsolutePath() + File.separator + filename);
            if (propertiesFile.exists() && propertiesFile.isFile() && propertiesFile.canRead()) {
                return propertiesFile;
            }

            currentPath = currentPath.getParent();
            if (currentPath == null) {
                break;
            }

            currentPath = currentPath.toAbsolutePath().normalize();
        }

        return null;
    }

    /** Class to hold the singleton instance */
    private static final class SingletonHolder {

        /** The singleton instance */
        private static final Configuration INSTANCE = new Configuration();
    }
}
