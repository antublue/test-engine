/*
 * Copyright (C) 2015-2023 The AntuBLUE test-engine project authors
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

import static java.util.Collections.emptySet;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;

/**
 * Abstract base implementation of {@link TestDescriptor} that may be used by custom {@link
 * org.junit.platform.engine.TestEngine TestEngines}.
 *
 * <p>Subclasses should provide a {@link TestSource} in their constructor, if possible, and override
 * {@link #getTags()}, if appropriate.
 *
 * @since 1.0
 */
// @API(status = STABLE, since = "1.0")
public abstract class AbstractTestDescriptor implements TestDescriptor {

    // private final UniqueId uniqueId;
    private UniqueId uniqueId;

    // private final String displayName;
    private String displayName;

    // private final TestSource source;
    private TestSource source;

    private TestDescriptor parent;

    /**
     * The synchronized set of children associated with this {@code TestDescriptor}.
     *
     * <p>This set is used in methods such as {@link #addChild(TestDescriptor)}, {@link
     * #removeChild(TestDescriptor)}, {@link #removeFromHierarchy()}, and {@link
     * #findByUniqueId(UniqueId)}, and an immutable copy of this set is returned by {@link
     * #getChildren()}.
     *
     * <p>If a subclass overrides any of the methods related to children, this set should be used
     * instead of a set local to the subclass.
     */
    protected final Set<TestDescriptor> children =
            Collections.synchronizedSet(new LinkedHashSet<>(16));

    /**
     * Create a new {@code AbstractTestDescriptor} with the supplied {@link UniqueId} and display
     * name.
     *
     * @param uniqueId the unique ID of this {@code TestDescriptor}; never {@code null}
     * @param displayName the display name for this {@code TestDescriptor}; never {@code null} or
     *     blank
     * @see #AbstractTestDescriptor(UniqueId, String, TestSource)
     */
    protected AbstractTestDescriptor(UniqueId uniqueId, String displayName) {
        this(uniqueId, displayName, null);
    }

    /**
     * Create a new {@code AbstractTestDescriptor} with the supplied {@link UniqueId}, display name,
     * and source.
     *
     * @param uniqueId the unique ID of this {@code TestDescriptor}; never {@code null}
     * @param displayName the display name for this {@code TestDescriptor}; never {@code null} or
     *     blank
     * @param source the source of the test or container described by this {@code TestDescriptor};
     *     can be {@code null}
     * @see #AbstractTestDescriptor(UniqueId, String)
     */
    protected AbstractTestDescriptor(UniqueId uniqueId, String displayName, TestSource source) {
        this.uniqueId = Preconditions.notNull(uniqueId, "UniqueId must not be null");
        this.displayName =
                Preconditions.notBlank(displayName, "displayName must not be null or blank");
        this.source = source;
    }

    /** Create a new {@code AbstractTestDescriptor} */
    protected AbstractTestDescriptor() {
        // DO NOTHING
    }

    /**
     * Set the unique identifier (UID) for this descriptor.
     *
     * <p>Uniqueness must be guaranteed across an entire test plan, regardless of how many engines
     * are used behind the scenes.
     *
     * @param uniqueId the UniqueId for this descriptor; never {@code null}
     */
    protected final void setUniqueId(UniqueId uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public final UniqueId getUniqueId() {
        return this.uniqueId;
    }

    /**
     * Set the <em>display name</em> for this descriptor.
     *
     * @param displayName the <em>display name</em> for this descriptor
     */
    protected final void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public final String getDisplayName() {
        Preconditions.notNull(displayName, "displayName is null for " + getClass().getName());
        return this.displayName;
    }

    @Override
    public Set<TestTag> getTags() {
        return emptySet();
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.ofNullable(this.source);
    }

    @Override
    public final Optional<TestDescriptor> getParent() {
        return Optional.ofNullable(this.parent);
    }

    @Override
    public final void setParent(TestDescriptor parent) {
        this.parent = parent;
    }

    @Override
    public final Set<? extends TestDescriptor> getChildren() {
        return Collections.unmodifiableSet(this.children);
    }

    @Override
    public void addChild(TestDescriptor child) {
        Preconditions.notNull(child, "child must not be null");
        child.setParent(this);
        this.children.add(child);
    }

    @Override
    public void removeChild(TestDescriptor child) {
        Preconditions.notNull(child, "child must not be null");
        this.children.remove(child);
        child.setParent(null);
    }

    @Override
    public void removeFromHierarchy() {
        Preconditions.condition(!isRoot(), "cannot remove the root of a hierarchy");
        this.parent.removeChild(this);
        this.children.forEach(child -> child.setParent(null));
        this.children.clear();
    }

    @Override
    public Optional<? extends TestDescriptor> findByUniqueId(UniqueId uniqueId) {
        Preconditions.notNull(uniqueId, "UniqueId must not be null");
        if (getUniqueId().equals(uniqueId)) {
            return Optional.of(this);
        }
        // @formatter:off
        return this.children.stream()
                .map(child -> child.findByUniqueId(uniqueId))
                .filter(Optional::isPresent)
                .findAny()
                .orElse(Optional.empty());
        // @formatter:on
    }

    @Override
    public final int hashCode() {
        return this.uniqueId.hashCode();
    }

    @Override
    public final boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this.getClass() != other.getClass()) {
            return false;
        }
        TestDescriptor that = (TestDescriptor) other;
        return this.getUniqueId().equals(that.getUniqueId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getUniqueId();
    }
}
