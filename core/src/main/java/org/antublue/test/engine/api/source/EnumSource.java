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

import org.antublue.test.engine.api.Argument;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

/**
 * Class to create a Stream of Arguments from an Enum
 */
@SuppressWarnings("unchecked")
public final class EnumSource {

    /**
     * Constructor
     */
    private EnumSource() {
        // DO NOTHING
    }

    /**
     * Method to create a Stream of Arguments from an Enum
     *
     * @param e
     * @return
     */
    public static Stream<Argument> of(Class<? extends Enum> e) {
        final List<Argument> list = new ArrayList<>();

        EnumSet.allOf(e)
                .forEach(o -> {
                    Enum ee = (Enum) o;
                    list.add(Argument.of(ee.name(), ee.toString()));
                });

        return list.stream();
    }
}
