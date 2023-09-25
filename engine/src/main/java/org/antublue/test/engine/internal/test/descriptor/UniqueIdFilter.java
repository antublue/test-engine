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

package org.antublue.test.engine.internal.test.descriptor;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.platform.engine.UniqueId;

public class UniqueIdFilter implements Predicate<UniqueId> {

    private final Set<UniqueId> uniqueIds;

    public UniqueIdFilter() {
        uniqueIds = new LinkedHashSet<>();
    }

    public UniqueIdFilter addUniqueId(UniqueId uniqueId) {
        uniqueIds.add(uniqueId);
        return this;
    }

    @Override
    public boolean test(UniqueId uniqueId) {
        for (UniqueId u : uniqueIds) {
            if (uniqueId.hasPrefix(u)) {
                return true;
            }
        }
        return false;
    }
}
