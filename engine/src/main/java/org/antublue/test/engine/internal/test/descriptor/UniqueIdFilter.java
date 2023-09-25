package org.antublue.test.engine.internal.test.descriptor;

import org.junit.platform.engine.UniqueId;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

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
