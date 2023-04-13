/*
 * Copyright 2022-2023 Douglas Hoard
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

import org.antublue.test.engine.internal.TestEngineConfigurationParameters;
import org.antublue.test.engine.internal.TestEngineEngineDiscoveryRequest;
import org.antublue.test.engine.internal.TestEngineExecutor;
import org.antublue.test.engine.internal.TestEngineInformation;
import org.antublue.test.engine.internal.TestEngineReflectionUtils;
import org.antublue.test.engine.internal.TestEngineSummaryEngineExecutionListener;
import org.antublue.test.engine.internal.discovery.TestEngineDiscoveryRequestResolver;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.HumanReadableTime;
import org.antublue.test.engine.internal.util.Timer;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

/**
 * Class to implement a TestEngine
 */
public class TestEngine implements org.junit.platform.engine.TestEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngine.class);

    public static final String ENGINE_ID = "antublue-test-engine";
    public static final String GROUP_ID = "org.antublue";
    public static final String ARTIFACT_ID = "test-engine";
    public static final String VERSION = TestEngineInformation.getVersion();

    static {
        /*
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("antublue-test-engine.log");
            DelegatingOutputStream delegatingOutputStream = new DelegatingOutputStream(System.out, fileOutputStream);
            System.setOut(new PrintStream(delegatingOutputStream));
            delegatingOutputStream = new DelegatingOutputStream(System.err, fileOutputStream);
            System.setErr(new PrintStream(delegatingOutputStream));
        } catch (IOException ioe) {
            // DO NOTHING
        }
        */
    }

    @Override
    public String getId() {
        return ENGINE_ID;
    }

    @Override
    public Optional<String> getGroupId() {
        return Optional.of(GROUP_ID);
    }

    @Override
    public Optional<String> getArtifactId() {
        return Optional.of(ARTIFACT_ID);
    }

    @Override
    public Optional<String> getVersion() {
        return Optional.of(VERSION);
    }

    /**
     * Method to discover test classes
     *
     * @param engineDiscoveryRequest
     * @param uniqueId
     * @return
     */
    @Override
    public TestDescriptor discover(EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId) {
        LOGGER.trace("discover(EngineDiscoveryRequest, UniqueId)");

        // Wrap the discovery request
        TestEngineEngineDiscoveryRequest testEngineDiscoveryRequest =
                new TestEngineEngineDiscoveryRequest(
                        engineDiscoveryRequest,
                        TestEngineConfigurationParameters.getInstance());

        // Create a EngineDescriptor as the target
        EngineDescriptor engineDescriptor = new EngineDescriptor(UniqueId.forEngine(getId()), getId());

        // Create a TestEngineDiscoverySelectorResolver and
        // resolve selectors, adding them to the engine descriptor
        new TestEngineDiscoveryRequestResolver().resolve(testEngineDiscoveryRequest, engineDescriptor);

        // Return the engine descriptor with all child test descriptors
        return engineDescriptor;
    }

    /**
     * Method to execute th ExecutionRequest
     *
     * @param executionRequest
     */
    @Override
    public void execute(ExecutionRequest executionRequest) {
        LOGGER.trace("execute(ExecutionRequest)");

        new TestEngineExecutor().execute(executionRequest);
    }

    /**
     * Method to run the TestEngine as a console application
     *
     * @param args
     */
    public static void main(String[] args) {
        long startTimeMilliseconds = System.currentTimeMillis();

        boolean hasConsole = "true".equals(System.getenv("__ANTUBLUE_TEST_ENGINE_HAS_CONSOLE__"));
        TestEngineConfigurationParameters.getInstance().put(TestEngineConstants.CONSOLE_OUTPUT, "true");

        PrintStream printStream = null;
        boolean failed = false;

        try {
            printStream = System.out;

            String banner = "AntuBLUE Test Engine " + VERSION;

            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < banner.length(); i++) {
                stringBuilder.append("-");
            }

            String separator = stringBuilder.toString();

            LOGGER.info(separator);
            LOGGER.info(banner);
            LOGGER.info(separator);

            Set<Path> classPathRoots =
                    new TreeSet<>(Comparator.comparing(o -> o.toAbsolutePath().toFile().getAbsolutePath()));

            // Add the jar containing the test engine to the class path to search for tests
            File file =
                    new File(
                            TestEngine.class
                                    .getProtectionDomain().getCodeSource().getLocation().getPath());

            classPathRoots.add(file.getAbsoluteFile().toPath());

            // Add all jars in the class path to search for tests
            String classPath = System.getProperty("java.class.path");
            String[] jars = classPath.split(File.pathSeparator);
            for (String jar : jars) {
                classPathRoots.add(new File(jar).getAbsoluteFile().toPath());
            }

            for (Path path : classPathRoots) {
                LOGGER.trace("jar [%s]", path.toAbsolutePath());
            }

            LauncherDiscoveryRequest launcherDiscoveryRequest =
                    LauncherDiscoveryRequestBuilder.request()
                            .selectors(DiscoverySelectors.selectClasspathRoots(classPathRoots))
                            .filters(includeClassNamePatterns(".*"))
                            .configurationParameters(new HashMap<>())
                            .build();

            TestEngine testEngine = new TestEngine();

            Timer timer = new Timer();

            TestDescriptor testDescriptor =
                    testEngine.discover(launcherDiscoveryRequest, UniqueId.root("/", "/"));

            LOGGER.trace("Test class/method discovery/resolution time [%d] ms", timer.stop().duration().toMillis());
            LOGGER.trace(separator);

            if (testDescriptor.getChildren().size() == 0) {
                long endTimeMilliseconds = System.currentTimeMillis();
                LOGGER.info("");
                LOGGER.info(separator);
                LOGGER.info("ERROR / NO TESTS FOUND");
                LOGGER.info(separator);
                LOGGER.info("Total Time  : " + HumanReadableTime.toHumanReadable(endTimeMilliseconds - startTimeMilliseconds, false));
                LOGGER.info("Finished At : " + HumanReadableTime.now());
                LOGGER.info(separator);
                System.exit(-2);
            }

            TestPlan testPlan =
                    TestEngineReflectionUtils.createTestPlan(
                            testDescriptor,
                            TestEngineConfigurationParameters.getInstance());

            TestEngineSummaryEngineExecutionListener summaryEngineExecutionListener = new TestEngineSummaryEngineExecutionListener(testPlan);

            testEngine.execute(
                    ExecutionRequest.create(
                            testDescriptor,
                            summaryEngineExecutionListener,
                            launcherDiscoveryRequest.getConfigurationParameters()));

            long endTimeMilliseconds = System.currentTimeMillis();

            TestExecutionSummary testExecutionSummary = summaryEngineExecutionListener.getSummary();

            banner = "AntuBLUE Test Engine " + VERSION + " Summary";

            stringBuilder.setLength(0);
            for (int i = 0; i < banner.length(); i++) {
                stringBuilder.append("-");
            }

            separator = stringBuilder.toString();

            LOGGER.info(separator);
            LOGGER.info(banner);
            LOGGER.info(separator);
            LOGGER.info("");
            LOGGER.info(
                    "TESTS : "
                            + (testExecutionSummary.getTestsFoundCount() + testExecutionSummary.getContainersFailedCount())
                            + ", "
                            + "PASSED"
                            + " : "
                            + (testExecutionSummary.getTestsSucceededCount() - testExecutionSummary.getContainersFailedCount())
                            + ", "
                            + "FAILED"
                            + " : "
                            + (testExecutionSummary.getTestsFailedCount() + testExecutionSummary.getContainersFailedCount())
                            + ", "
                            + "SKIPPED"
                            + " : "
                            + testExecutionSummary.getTestsSkippedCount());

            LOGGER.info("");
            LOGGER.info(separator);

            failed = (testExecutionSummary.getTestsFailedCount() + testExecutionSummary.getContainersFailedCount()) > 0;

            if (failed) {
                LOGGER.info("FAILED");
            } else {
                LOGGER.info("PASSED");
            }

            LOGGER.info(separator);
            LOGGER.info("Total Time  : " + HumanReadableTime.toHumanReadable(endTimeMilliseconds - startTimeMilliseconds, false));
            LOGGER.info("Finished At : " + HumanReadableTime.now());
            LOGGER.info(separator);
        } catch (Throwable t) {
            failed = true;
            LOGGER.error("Internal Error occurred.");
            t.printStackTrace();
        } finally {
            if (printStream != null) {
                try {
                    printStream.close();
                } catch (Throwable t) {
                    // DO NOTHING
                }
            }

            if (failed) {
                System.exit(1);
            } else {
                System.exit(0);
            }
        }
    }
}
