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

import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.ArgumentMap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Class to create a Stream of Argument from a CSV file with a header line
 */
public final class CsvSource {

    /**
     * Constructor
     */
    private CsvSource() {
        // DO NOTHING
    }

    /**
     * Method to create a Stream of Arguments from a CSV file
     *
     * @param file
     * @param charset
     * @return
     * @throws IOException
     */
    public static Stream<Argument> of(File file, Charset charset) throws IOException {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            return of(inputStream, charset);
        }
    }

    /**
     * Method to create a Stream of Argument from an InputStream formatted as CSV
     *
     * @param reader
     * @return
     * @throws IOException
     */
    public static Stream<Argument> of(Reader reader) throws IOException {
        CsvParserSettings parserSettings = new CsvParserSettings();
        parserSettings.setLineSeparatorDetectionEnabled(true);
        RowListProcessor rowListProcessor = new RowListProcessor();
        parserSettings.setProcessor(rowListProcessor);
        parserSettings.setHeaderExtractionEnabled(true);
        CsvParser parser = new CsvParser(parserSettings);
        parser.parse(reader);
        return process(rowListProcessor);
    }

    /**
     * Method to create a Stream of Argument from a Reader formatted as CSV
     *
     * @param inputStream
     * @param charset
     * @return
     * @throws IOException
     */
    public static Stream<Argument> of(InputStream inputStream, Charset charset) throws IOException {
        CsvParserSettings parserSettings = new CsvParserSettings();
        parserSettings.setLineSeparatorDetectionEnabled(true);
        RowListProcessor rowListProcessor = new RowListProcessor();
        parserSettings.setProcessor(rowListProcessor);
        parserSettings.setHeaderExtractionEnabled(true);
        CsvParser parser = new CsvParser(parserSettings);
        parser.parse(inputStream, charset);
        return process(rowListProcessor);
    }

    private static Stream<Argument> process(RowListProcessor rowListProcessor) {
        List<Argument> list = new ArrayList<>();

        String[] headers = rowListProcessor.getHeaders();
        List<String[]> rows = rowListProcessor.getRows();
        for (int i = 0; i < rows.size(); i++){
            ArgumentMap argumentMap = new ArgumentMap();
            String[] row = rows.get(i);
            for (int j = 0; j < row.length; j++) {
                // TODO clean up by checking the header length against the row length
                String header = null;
                try {
                    header = headers[j];
                    if (header.trim().isEmpty()) {
                        header = "column[" + (j+1) + "]";
                    }
                } catch (Throwable t) {
                    header = "column[" + (j+1) + "]";
                }

                argumentMap.put(header, row[j]);
            }
            list.add(Argument.of("row[" + (i + 1) + "]", argumentMap));
        }

        return list.stream();
    }
}
