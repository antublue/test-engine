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

package org.antublue.test.engine.maven.plugin;

import org.antublue.test.engine.internal.TestEngineConsoleTestExecutionListener;
import org.antublue.test.engine.internal.util.AnsiColor;
import org.antublue.test.engine.internal.util.AnsiColorString;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.antublue.test.engine.TestEngine.ANTUBLUE_TEST_ENGINE_MAVEN_BATCH_MODE;
import static org.antublue.test.engine.TestEngine.ANTUBLUE_TEST_ENGINE_MAVEN_PLUGIN;
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

/**
 * Class to implement a Maven plugin to run the AntuBLUE Test Engine
 */
@SuppressWarnings({"unused", "deprecation"})
@Mojo(name = "test", threadSafe = true, requiresDependencyResolution = ResolutionScope.TEST)
public class TestEngineMavenPlugin extends AbstractMojo {

    private static final String GROUP_ID = "org.antublue";
    private static final String ARTIFACT_ID = "test-engine-maven-plugin";
    private static final String VERSION = TestEngineMavenPluginInformation.getVersion();

    private static final String BANNER =
            new AnsiColorString()
                    .color(AnsiColor.WHITE_BRIGHT)
                    .append("Antu")
                    .color(AnsiColor.BLUE_BOLD_BRIGHT)
                    .append("BLUE")
                    .color(AnsiColor.WHITE_BRIGHT)
                    .append(" Test Engine Maven Plugin ")
                    .append(VERSION)
                    .toString();

    private static final String SEPARATOR =
            AnsiColor.WHITE_BRIGHT.apply(
                    "------------------------------------------------------------------------");

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    private MavenSession mavenSession;

    @Parameter(property ="project", required = true, readonly = true)
    protected MavenProject mavenProject;

    @Parameter(property = "properties")
    protected Map<String, String> properties;

    private Log log;

    /**
     * Method to execute the plugin
     *
     * @throws MojoExecutionException execution exception
     */
    public void execute() throws MojoExecutionException {
        debug(SEPARATOR);
        debug(BANNER);
        debug(SEPARATOR);

        System.setProperty(ANTUBLUE_TEST_ENGINE_MAVEN_PLUGIN, "true");
        debug("system property [" + ANTUBLUE_TEST_ENGINE_MAVEN_PLUGIN + "] = [%s]", "true");

        if (!mavenSession.getRequest().isInteractiveMode()) {
            System.setProperty(ANTUBLUE_TEST_ENGINE_MAVEN_BATCH_MODE, "true");
            debug("system property [" + ANTUBLUE_TEST_ENGINE_MAVEN_BATCH_MODE + "] = [%s]", "true");
        }

        Optional.ofNullable(properties)
                .ifPresent(map ->
                        map.forEach((key, value) -> {
                            if (key != null && value != null) {
                                System.setProperty(key, value);
                                debug("system property [%s] = [%s]", key, value);
                            }
                        }));

        try {
            Set<Path> artifactPaths = new LinkedHashSet<>();

            Optional.ofNullable(mavenProject.getCompileClasspathElements())
                    .ifPresent(
                            strings -> strings.forEach(string -> artifactPaths.add(new File(string).toPath())));

            Optional.ofNullable(mavenProject.getRuntimeClasspathElements())
                    .ifPresent(
                            strings -> strings.forEach(string -> artifactPaths.add(new File(string).toPath())));

            Optional.ofNullable(mavenProject.getTestClasspathElements())
                    .ifPresent(
                            strings -> strings.forEach(string -> artifactPaths.add(new File(string).toPath())));

            Optional.ofNullable(mavenProject.getArtifact())
                    .ifPresent(
                            artifact -> artifactPaths.add(artifact.getFile().toPath()));

            Optional.ofNullable(mavenProject.getDependencyArtifacts())
                    .ifPresent(
                            artifacts -> artifacts.forEach(artifact -> artifactPaths.add(artifact.getFile().toPath())));

            Optional.ofNullable(mavenProject.getAttachedArtifacts())
                    .ifPresent(
                            artifacts -> artifacts.forEach(artifact -> artifactPaths.add(artifact.getFile().toPath())));

            Set<URL> urls = new LinkedHashSet<>();
            for (Path path : artifactPaths) {
                URL url = path.toUri().toURL();
                urls.add(url);
                debug("classpath entry [%s]", url);
            }

            // Build a classloader for subsequent calls
            ClassLoader classLoader =
                    new URLClassLoader(
                            urls.toArray(new URL[urls.size()]),
                            Thread.currentThread().getContextClassLoader());

            Thread.currentThread().setContextClassLoader(classLoader);

            TestEngineConsoleTestExecutionListener testEngineConsoleTestExecutionListener =
                    new TestEngineConsoleTestExecutionListener();

            LauncherConfig launcherConfig =
                    LauncherConfig
                            .builder()
                            .build();

            LauncherDiscoveryRequest launcherDiscoveryRequest =
                    LauncherDiscoveryRequestBuilder.request()
                            .selectors(DiscoverySelectors.selectClasspathRoots(artifactPaths))
                            .filters(includeClassNamePatterns(".*"))
                            .configurationParameters(Collections.emptyMap())
                            .build();

            try (LauncherSession launcherSession = LauncherFactory.openSession(launcherConfig)) {
                Launcher launcher = launcherSession.getLauncher();
                launcher.registerTestExecutionListeners(testEngineConsoleTestExecutionListener);
                launcher.execute(launcherDiscoveryRequest);
            }

            if (testEngineConsoleTestExecutionListener.hasFailures()) {
                throw new SuppressedStackTraceMojoExecutionException("Test failures");
            }
        } catch (SuppressedStackTraceMojoExecutionException e) {
            throw e;
        } catch (JUnitException e) {
            String message = e.getMessage();

            if (e.getCause() != null) {
                message = e.getCause().getMessage();
            }

            StringBuilder stringBuilder =
                    new StringBuilder()
                            .append(System.lineSeparator())
                            .append(System.lineSeparator())
                            .append(message)
                            .append(System.lineSeparator());

            throw new SuppressedStackTraceMojoExecutionException(stringBuilder.toString());
        } catch (Throwable t) {
            t.printStackTrace();
            throw new MojoExecutionException("General AntuBLUE Test Engine Maven Plugin Exception", t);
        }
    }

    /**
     * Method to set the plugin Log
     *
     * @param log log
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * Method to log a DEBUG message
     *
     * @param format format
     * @param object object
     */
    private void debug(String format, Object object) {
        if (log.isDebugEnabled()) {
            debug(format, new Object[]{object});
        }
    }

    /**
     * Method to log a DEBUG message
     *
     * @param format format
     * @param objects objects
     */
    private void debug(String format, Object ... objects) {
        if (log.isDebugEnabled()) {
            debug(String.format(format, objects));
        }
    }

    /**
     * Method to log a DEBUG message
     *
     * @param message message
     */
    private void debug(String message) {
        if (log.isDebugEnabled()) {
            log.debug(message);
        }
    }

    /**
     * Method to log an empty INFO message
     */
    private void info() {
        info("");
    }

    /**
     * Method to log an INFO message
     *
     * @param format format
     * @param objects objects
     */
    private void info(String format, Object ... objects) {
        info(String.format(format, objects));
    }

    /**
     * Method to log an INFO message
     *
     * @param message message
     */
    private void info(String message) {
        System.out.println(
                new AnsiColorString()
                        .append("[")
                        .color(AnsiColor.BLUE_BOLD)
                        .append("INFO")
                        .color(AnsiColor.RESET)
                        .append("] ")
                        .append(message));
    }
}
