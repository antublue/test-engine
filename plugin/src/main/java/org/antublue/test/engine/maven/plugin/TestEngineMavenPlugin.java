/*
 * Copyright 2023 Douglas Hoard
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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Class to implement a Maven plugin to run the AntuBLUE Test Engine
 */
@Mojo(name = "test", threadSafe = true)
@SuppressWarnings("unused")
public class TestEngineMavenPlugin extends AbstractMojo {

    @Parameter(property ="project",required = true, readonly = true)
    protected MavenProject mavenProject;

    @Parameter(property="java")
    protected Java java;

    /**
     * Method to execute the plugin
     *
     * @throws MojoFailureException tests failed
     * @throws MojoExecutionException general exception
     */
    public void execute() throws MojoFailureException, MojoExecutionException {
        try {
            Log log = getLog();

            boolean hasConsole = System.console() != null;
            log.debug(String.format("hasConsole [%s]", hasConsole));

            String javaBinary = java.getBinary();
            if ((javaBinary == null) || (javaBinary.trim().isEmpty())) {
                String operatingSystem = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
                if (operatingSystem.contains("win")) {
                    javaBinary = "java.exe";
                } else {
                    javaBinary = "java";
                }
            }

            log.debug(String.format("java.binary [%s]", javaBinary.trim()));

            Set<String> artifactPathSet = new LinkedHashSet<>();

            Artifact projectArtifact = mavenProject.getArtifact();
            artifactPathSet.add(projectArtifact.getFile().getAbsolutePath());

            for (Artifact artifact : mavenProject.getArtifacts()) {
                String path = artifact.getFile().getAbsolutePath();
                log.debug(String.format("artifact [%s]", path));
                artifactPathSet.add(path);
            }

            for (Artifact artifact : mavenProject.getAttachedArtifacts()) {
                String path = artifact.getFile().getAbsolutePath();
                log.debug(String.format("artifact [%s]", path));
                artifactPathSet.add(path);
            }

            List<String> classpathElements = mavenProject.getCompileClasspathElements();
            for (String classpathElement : classpathElements) {
                String path = new File(classpathElement).getAbsolutePath();
                log.debug(String.format("artifact [%s]", path));
                artifactPathSet.add(path);
            }

            classpathElements = mavenProject.getRuntimeClasspathElements();
            for (String classpathElement : classpathElements) {
                String path = new File(classpathElement).getAbsolutePath();
                log.debug(String.format("artifact [%s]", path));
                artifactPathSet.add(path);
            }

            classpathElements = mavenProject.getTestClasspathElements();
            for (String classpathElement : classpathElements) {
                String path = new File(classpathElement).getAbsolutePath();
                log.debug(String.format("artifact [%s]", path));
                artifactPathSet.add(path);
            }

            for (Artifact artifact : mavenProject.getDependencyArtifacts()) {
                String path = artifact.getFile().getAbsolutePath();
                artifactPathSet.add(path);
            }

            StringBuilder classPathStringBuilder = new StringBuilder();
            for (String artifactPath : artifactPathSet) {
                log.debug(String.format("artifact [%s]", artifactPath));
                classPathStringBuilder.append(artifactPath).append(File.pathSeparator);
            }

            String classPath = classPathStringBuilder.toString();
            if (classPath.endsWith(File.pathSeparator)) {
                classPath = classPath.substring(0, classPath.length() - File.pathSeparator.length());
            }

            log.debug(String.format("classPath [%s]", classPath));

            List<String> systemPropertyList = new ArrayList<>();
            SystemProperty[] systemProperties = java.getSystemProperties();

            if (systemProperties != null) {
                for (SystemProperty systemProperty : systemProperties) {
                    String key = systemProperty.getKey();
                    String value = systemProperty.getValue();

                    if ((key != null) && !key.trim().isEmpty()) {
                        if ((value != null) && !value.trim().isEmpty()) {
                            log.debug(String.format("java [-D%s] = [%s]", key, value));
                            systemPropertyList.add("-D" + key + "=" + value);
                        } else {
                            log.debug(String.format("java [-D%s]", key));
                            systemPropertyList.add("-D" + key);
                        }
                    }
                }
            }

            List<String> commandList = new ArrayList<>();
            commandList.add(javaBinary);
            commandList.addAll(systemPropertyList);
            commandList.add("-cp");
            commandList.add(classPath);
            commandList.add("org.antublue.test.engine.TestEngine");

            StringBuilder stringBuilder = new StringBuilder();
            for (String command : commandList) {
                if (command.indexOf("*") > 0) {
                    command = "\"" + command + "\"";
                }
                stringBuilder.append(command);
                stringBuilder.append(" ");
            }

            log.debug(String.format("command [%s]", stringBuilder.toString().trim()));

            String[] commands = commandList.toArray(new String[0]);

            ProcessBuilder processBuilder = new ProcessBuilder().command(commands).inheritIO();

            if (hasConsole) {
                processBuilder.environment().put("__ANTUBLUE_TEST_ENGINE_HAS_CONSOLE__", String.valueOf(hasConsole));
            }

            int exitCode = processBuilder.start().waitFor();

            if (exitCode != 0) {
                throw new MojoFailureException("Failed tests");
            }
        } catch (MojoFailureException e) {
            throw e;
        } catch (Throwable t) {
            t.printStackTrace();
            throw new MojoExecutionException("General AntuBLUE Test Engine Maven Plugin Exception", t);
        }
    }
}
