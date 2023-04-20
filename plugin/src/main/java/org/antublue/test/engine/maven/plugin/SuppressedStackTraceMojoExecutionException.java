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

import org.apache.maven.plugin.MojoExecutionException;

import java.io.PrintStream;

/**
 * Class to implement a MojoExecutionException that doesn't print a stack trace
 */
public class SuppressedStackTraceMojoExecutionException extends MojoExecutionException {

    /**
     * Constructor
     *
     * @param message
     */
    public SuppressedStackTraceMojoExecutionException(String message) {
        super(message);
    }

    @Override
    public void printStackTrace() {
        // DO NOTHING
    }

    @Override
    public void printStackTrace(PrintStream printStream) {
        // DO NOTHING
    }
}
