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

package example.extension.annotation;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.api.TestEngine;

/** Example test */
public class RandomStringAnnotationTest {

    @RandomStringAnnotation.Random.String.AlphaNumeric(minimumLength = -1, maximumLength = 20)
    public String randomAlphaNumericString;

    @TestEngine.ExtensionSupplier
    public static List<Extension> extensions() {
        List<Extension> list = new ArrayList<>();
        list.add(new RandomStringExtension());
        return list;
    }

    @TestEngine.Test
    public void test1() {
        System.out.println(
                format("test1() randomAlphaNumericString [%s]", randomAlphaNumericString));
    }

    @TestEngine.Test
    public void test2() {
        System.out.println(
                format("test2() randomAlphaNumericString [%s]", randomAlphaNumericString));
    }

    @TestEngine.Test
    public void test3() {
        System.out.println(
                format("test2() randomAlphaNumericString [%s]", randomAlphaNumericString));
    }

    @TestEngine.Test
    public void test4() {
        System.out.println(
                format("test3() randomAlphaNumericString [%s]", randomAlphaNumericString));
    }

    @TestEngine.Test
    public void test5() {
        System.out.println(
                format("test4() randomAlphaNumericString [%s]", randomAlphaNumericString));
    }
}
