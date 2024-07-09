/*
 * Copyright (C) 2024 The AntuBLUE test-engine project authors
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

package org.antublue.test.engine.internal.descriptor;

// package org.junit.platform.engine.support.descriptor;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;

@SuppressWarnings("unchecked")
public abstract class AbstractTestDescriptor implements TestDescriptor {
    private UniqueId uniqueId;
    private final String displayName;
    private final TestSource source;
    private TestDescriptor parent;
    protected final Set<TestDescriptor> children;

    protected AbstractTestDescriptor(UniqueId uniqueId, String displayName) {
        this(uniqueId, displayName, null);
    }

    protected AbstractTestDescriptor(UniqueId uniqueId, String displayName, TestSource source) {
        this.children = Collections.synchronizedSet(new LinkedHashSet<>(16));
        this.uniqueId = Preconditions.notNull(uniqueId, "UniqueId must not be null");
        this.displayName =
                Preconditions.notBlank(displayName, "displayName must not be null or blank");
        this.source = source;
    }

    public void setUniqueId(UniqueId uniqueId) {
        this.uniqueId = uniqueId;
    }

    public final UniqueId getUniqueId() {
        return this.uniqueId;
    }

    public final String getDisplayName() {
        return this.displayName;
    }

    public Set<TestTag> getTags() {
        return Collections.emptySet();
    }

    public Optional<TestSource> getSource() {
        return Optional.ofNullable(this.source);
    }

    public final Optional<TestDescriptor> getParent() {
        return Optional.ofNullable(this.parent);
    }

    public final void setParent(TestDescriptor parent) {
        this.parent = parent;
    }

    public final Set<? extends TestDescriptor> getChildren() {
        return Collections.unmodifiableSet(this.children);
    }

    public void addChild(TestDescriptor child) {
        Preconditions.notNull(child, "child must not be null");
        child.setParent(this);
        this.children.add(child);
    }

    public void removeChild(TestDescriptor child) {
        Preconditions.notNull(child, "child must not be null");
        this.children.remove(child);
        child.setParent(null);
    }

    public void removeFromHierarchy() {
        Preconditions.condition(!this.isRoot(), "cannot remove the root of a hierarchy");
        this.parent.removeChild(this);
        this.children.forEach((child) -> child.setParent(null));
        this.children.clear();
    }

    public Optional<? extends TestDescriptor> findByUniqueId(UniqueId uniqueId) {
        Preconditions.notNull(uniqueId, "UniqueId must not be null");
        return this.getUniqueId().equals(uniqueId)
                ? Optional.of(this)
                : this.children.stream()
                        .map((child) -> child.findByUniqueId(uniqueId))
                        .filter(Optional::isPresent)
                        .findAny()
                        .orElse(Optional.empty());
    }

    public final int hashCode() {
        return this.uniqueId.hashCode();
    }

    public final boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (this.getClass() != other.getClass()) {
            return false;
        } else {
            TestDescriptor that = (TestDescriptor) other;
            return this.getUniqueId().equals(that.getUniqueId());
        }
    }

    public String toString() {
        return this.getClass().getSimpleName() + ": " + this.getUniqueId();
    }
}
