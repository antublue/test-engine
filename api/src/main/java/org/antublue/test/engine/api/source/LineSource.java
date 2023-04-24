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

package org.antublue.test.engine.api.source;

import org.antublue.test.engine.api.Parameter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Class to create a Stream of Parameters where each Parameter value is a line,
 * <br>
 * Skips lines that start with a "#", but accepts empty lines
 */
public final class LineSource {

    /**
     * Constructor
     */
    private LineSource() {
        // DO NOTHING
    }

    /**
     * Method to get a Stream of Parameters from a File
     *
     * @param file
     * @param charset
     * @return
     * @throws IOException
     */
    public static Stream<Parameter> of(File file, Charset charset) throws IOException {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            return of(inputStream, charset);
        }
    }

    /**
     * Method to get a Stream of Parameters from a Reader
     *
     * @param reader
     * @return
     * @throws IOException
     */
    public static Stream<Parameter> of(Reader reader) throws IOException {
        List<Parameter> list = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            long index = 1;
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }

                list.add(Parameter.of("line [" + index + "]", line));
                index++;
            }
        }

        return list.stream();
    }

    /**
     * Method to get a Stream of Parameters from an InputStream
     *
     * @param inputStream
     * @param charset
     * @return
     * @throws IOException
     */
    public static Stream<Parameter> of(InputStream inputStream, Charset charset) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, charset)) {
            return of(reader);
        }
    }
}
